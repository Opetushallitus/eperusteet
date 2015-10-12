/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.yl.*;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.*;
import fi.vm.sade.eperusteet.repository.*;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.event.PerusteUpdatedEvent;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.OppiaineLockContext;
import fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static fi.vm.sade.eperusteet.domain.yl.Oppiaine.inLukioPeruste;
import static fi.vm.sade.eperusteet.service.util.OptionalUtil.found;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 *
 * @author jhyoty
 */
@Service
@Transactional(readOnly = true)
public class OppiaineServiceImpl implements OppiaineService {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private OppiaineRepository oppiaineRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private OppiaineenVuosiluokkakokonaisuusRepository vuosiluokkakokonaisuusRepository;

    @Autowired
    private PerusopetuksenPerusteenSisaltoRepository perusOpetuksenSisaltoRepository;

    @Autowired
    private OpetuksenKohdeAlueRepository kohdeAlueRepository;

    @Autowired
    @LockCtx(OppiaineLockContext.class)
    private LockService<OppiaineLockContext> lockService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private static final Logger LOG = LoggerFactory.getLogger(OppiaineServiceImpl.class);

    @Override
    @Transactional(readOnly = false)
    public OppiaineDto addOppiaine(Long perusteId, OppiaineDto dto, OppiaineOpetuksenSisaltoTyyppi tyyppi) {
        AbstractOppiaineOpetuksenSisalto sisalto = tyyppi.getLockedByPerusteId(applicationContext, perusteId);
        if (sisalto != null) {
            Oppiaine oppiaine = saveOppiaine(dto);
            sisalto.addOppiaine(oppiaine);
            return mapper.map(oppiaine, OppiaineDto.class);
        }
        throw new BusinessRuleViolationException("Perustetta ei ole");
    }

    private Oppiaine saveOppiaine(OppiaineDto dto) {
        Oppiaine oppiaine = mapper.map(dto, Oppiaine.class);
        oppiaine = oppiaineRepository.save(oppiaine);
        final Set<OppiaineenVuosiluokkaKokonaisuusDto> vuosiluokkakokonaisuudet = dto.getVuosiluokkakokonaisuudet();
        if (vuosiluokkakokonaisuudet != null) {
            for (OppiaineenVuosiluokkaKokonaisuusDto v : vuosiluokkakokonaisuudet) {
                addOppiaineenVuosiluokkaKokonaisuus(oppiaine, v);
            }
        }
        return oppiaine;
    }

    @Override
    @Transactional(readOnly = false)
    public OppiaineenVuosiluokkaKokonaisuusDto addOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, OppiaineenVuosiluokkaKokonaisuusDto dto) {
        PerusopetuksenPerusteenSisalto sisalto =  perusOpetuksenSisaltoRepository.findByPerusteId(perusteId);
        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);
        if (sisalto.containsOppiaine(aine)) {
            oppiaineRepository.lock(aine);
            return mapper.map(addOppiaineenVuosiluokkaKokonaisuus(aine, dto), OppiaineenVuosiluokkaKokonaisuusDto.class);
        } else {
            throw new BusinessRuleViolationException("oppiaine ei kuulu tähän perusteeseen");
        }
    }

    private OppiaineenVuosiluokkaKokonaisuus addOppiaineenVuosiluokkaKokonaisuus(Oppiaine aine, OppiaineenVuosiluokkaKokonaisuusDto dto) {
        OppiaineenVuosiluokkaKokonaisuus ovk = mapper.map(dto, OppiaineenVuosiluokkaKokonaisuus.class);
        ovk.setId(null);
        aine.addVuosiluokkaKokonaisuus(ovk);
        ovk = vuosiluokkakokonaisuusRepository.save(ovk);
        return ovk;
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteOppiaine(Long perusteId, Long oppiaineId, OppiaineOpetuksenSisaltoTyyppi tyyppi) {
        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);
        AbstractOppiaineOpetuksenSisalto sisalto = tyyppi.getRepository(applicationContext).findByPerusteId(perusteId);
        if (sisalto == null || !sisalto.containsOppiaine(aine)) {
            throw new BusinessRuleViolationException("Oppiainetta ei ole tai se ei kuulu tähän perusteeseen");
        }
        final OppiaineLockContext ctx = OppiaineLockContext.of(tyyppi, perusteId, oppiaineId, null);
        oppiaineRepository.lock(aine);
        lockService.lock(ctx);
        try {
            for (OppiaineenVuosiluokkaKokonaisuus k : aine.getVuosiluokkakokonaisuudet()) {
                deleteOppiaineenVuosiluokkaKokonaisuus(perusteId, oppiaineId, k.getId(), false);
            }

            if (aine.isKoosteinen()) {
                for (Oppiaine m : aine.getOppimaarat()) {
                    final OppiaineLockContext vkctx = OppiaineLockContext.of(tyyppi, perusteId, oppiaineId, null);
                    lockService.lock(vkctx);
                    deleteOppiaine(perusteId, m.getId(), tyyppi);
                }
            }
        } finally {
            lockService.unlock(ctx);
        }
        if (aine.getOppiaine() != null) {
            aine.getOppiaine().removeOppimaara(aine);
        } else {
            sisalto.removeOppiaine(aine);
        }
        oppiaineRepository.delete(aine);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, Long vuosiluokkaKokonaisuusId) {
        deleteOppiaineenVuosiluokkaKokonaisuus(perusteId, oppiaineId, vuosiluokkaKokonaisuusId, true);
    }

    private void deleteOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, Long vuosiluokkaKokonaisuusId, boolean lockOppiaine) {
        OppiaineenVuosiluokkaKokonaisuus vk = vuosiluokkakokonaisuusRepository.findOne(vuosiluokkaKokonaisuusId);
        PerusopetuksenPerusteenSisalto sisalto = perusOpetuksenSisaltoRepository.findByPerusteId(perusteId);
        if (sisalto == null || vk == null || !sisalto.containsOppiaine(vk.getOppiaine())) {
            throw new BusinessRuleViolationException("Virheellinen vuosiluokkakokonaisuus");
        }
        OppiaineLockContext ctx = OppiaineLockContext.of(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS, perusteId, oppiaineId, vk.getId());

        lockService.lock(ctx);
        if (lockOppiaine) {
            oppiaineRepository.lock(vk.getOppiaine());
        }
        try {
            for (OpetuksenTavoite t : vk.getTavoitteet()) {
                t.setSisaltoalueet(null);
                t.setLaajattavoitteet(null);
                t.setKohdealueet(null);
            }
        } finally {
            lockService.unlock(ctx);
        }
        vk.getOppiaine().removeVuosiluokkaKokonaisuus(vk);
        vuosiluokkakokonaisuusRepository.delete(vk);
    }

    @Override
    public OppiaineDto getOppiaine(long perusteId, long oppiaineId, OppiaineOpetuksenSisaltoTyyppi tyyppi) {
        AbstractOppiaineOpetuksenSisalto sisalto = tyyppi.getRepository(applicationContext).findByPerusteId(perusteId);
        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);

        if (sisalto != null && sisalto.containsOppiaine(aine)) {
            return mapper.map(aine, OppiaineDto.class);
        } else {
            throw new BusinessRuleViolationException(OPPIAINETTA_EI_OLE);
        }
    }


    @Override
    public OppiaineDto getOppiaine(long perusteId, long oppiaineId, int revisio, OppiaineOpetuksenSisaltoTyyppi tyyppi) {
        AbstractOppiaineOpetuksenSisalto sisalto = tyyppi.getRepository(applicationContext).findByPerusteId(perusteId);
        sisalto = perusOpetuksenSisaltoRepository.findRevision(sisalto.getId(), revisio);
        Oppiaine aine = oppiaineRepository.findRevision(oppiaineId, revisio);
        if (sisalto != null && sisalto.containsOppiaine(aine)) {
            return mapper.map(aine, OppiaineDto.class);
        } else {
            throw new BusinessRuleViolationException(OPPIAINETTA_EI_OLE);
        }
    }

    @Override
    public List<Revision> getOppiaineRevisions(long perusteId, long oppiaineId, OppiaineOpetuksenSisaltoTyyppi tyyppi) {
        AbstractOppiaineOpetuksenSisalto sisalto = tyyppi.getRepository(applicationContext).findByPerusteId(perusteId);
        sisalto = perusOpetuksenSisaltoRepository.findOne(sisalto.getId());
        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);
        if (sisalto != null && sisalto.containsOppiaine(aine)) {
            return oppiaineRepository.getRevisions(oppiaineId);
        } else {
            throw new BusinessRuleViolationException(OPPIAINETTA_EI_OLE);
        }
    }

    @Override
    public OppiaineDto revertOppiaine(long perusteId, long oppiaineId, int revisio, OppiaineOpetuksenSisaltoTyyppi tyyppi) {

        //TODO. ei toimi jos palautettava versio viittaa poistettuihin entiteetteihin
        //(keskeinensisältöalue, vuosiluokkakokonaisuus, laaja-alainen osaaminen)
        //sama ongelma on toki updatessakin (ei voi luoda uusia sisältöalueta ja viitata niihin tavoitteista)
        OppiaineSisaltoRepository<?> repository = tyyppi.getRepository(applicationContext);
        AbstractOppiaineOpetuksenSisalto sisalto = repository.findByPerusteId(perusteId);
        sisalto = repository.findRevision(sisalto.getId(), revisio);
        Oppiaine aine = oppiaineRepository.findRevision(oppiaineId, revisio);
        if (sisalto != null && sisalto.containsOppiaine(aine)) {
            OppiaineDto dto = mapper.map(aine, OppiaineDto.class);
            return updateOppiaine(perusteId, new UpdateDto<>(dto), OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        } else {
            throw new BusinessRuleViolationException(OPPIAINETTA_EI_OLE);
        }
    }

    @Override
    public OppiaineenVuosiluokkaKokonaisuusDto getOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, Long vuosiluokkaKokonaisuusId) {
        PerusopetuksenPerusteenSisalto sisalto = perusOpetuksenSisaltoRepository.findByPerusteId(perusteId);
        OppiaineenVuosiluokkaKokonaisuus vk = sisalto == null ? null
            : vuosiluokkakokonaisuusRepository.findByIdAndOppiaineId(vuosiluokkaKokonaisuusId, oppiaineId);
        if (sisalto != null && vk != null && sisalto.containsOppiaine(vk.getOppiaine())) {
            return mapper.map(vk, OppiaineenVuosiluokkaKokonaisuusDto.class);
        } else {
            throw new BusinessRuleViolationException("Pyydettyä vuosiluokkakokonaisuutta ei ole");
        }
    }

    @Override
    public List<OppiaineSuppeaDto> getOppimaarat(Long perusteId, Long oppiaineId, OppiaineOpetuksenSisaltoTyyppi tyyppi) {
        AbstractOppiaineOpetuksenSisalto sisalto = tyyppi.getRepository(applicationContext).findByPerusteId(perusteId);
        Oppiaine oa = oppiaineRepository.findOne(oppiaineId);
        if (sisalto.containsOppiaine(oa)) {
            return mapper.mapAsList(oa.getOppimaarat(), OppiaineSuppeaDto.class);
        }
        throw new BusinessRuleViolationException(OPPIAINETTA_EI_OLE);
    }

    @Override
    @Transactional(readOnly = false)
    public OppiaineDto updateOppiaine(Long perusteId, UpdateDto<OppiaineDto> updateDto, OppiaineOpetuksenSisaltoTyyppi tyyppi) {
        OppiaineDto dto = updateDto.getDto();
        Oppiaine aine = oppiaineRepository.findOne(dto.getId());
        AbstractOppiaineOpetuksenSisalto sisalto = tyyppi.getRepository(applicationContext).findByPerusteId(perusteId);
        if (aine == null || sisalto == null || !sisalto.containsOppiaine(aine)) {
            throw new NotExistsException("Oppiainetta ei ole");
        }
        lockService.assertLock(OppiaineLockContext.of(tyyppi, perusteId, dto.getId(), null));
        oppiaineRepository.lock(aine);

        Integer rev = null;
        if (sisalto.getPeruste().getTila() == PerusteTila.VALMIS) {
            rev = oppiaineRepository.getLatestRevisionId();
        }
        mapper.map(dto, aine);
        final Set<OppiaineenVuosiluokkaKokonaisuusDto> vuosiluokkakokonaisuudet = dto.getVuosiluokkakokonaisuudet();
        if (vuosiluokkakokonaisuudet != null) {
            final boolean valmis = sisalto.getPeruste().getTila() == PerusteTila.VALMIS;
            for (OppiaineenVuosiluokkaKokonaisuusDto v : vuosiluokkakokonaisuudet) {
                if (v.getId() == null) {
                    if ( valmis ) {
                        throw new BusinessRuleViolationException(VAIN_KORJAUKSET_SALLITTU);
                    }
                    addOppiaineenVuosiluokkaKokonaisuus(aine, v);
                } else {
                    final OppiaineLockContext vkctx = OppiaineLockContext.of(tyyppi, perusteId, dto.getId(), v.getId());
                    try {
                        lockService.lock(vkctx);
                        if (sisalto instanceof PerusopetuksenPerusteenSisalto) {
                            doUpdateOppiaineenVuosiluokkaKokonaisuus((PerusopetuksenPerusteenSisalto)sisalto, aine.getId(), v, false);
                        }
                    } finally {
                        lockService.unlock(vkctx);
                    }
                }
                //"ominaisuus": vuosiluokkakokonaisuuksia ei voi poistaa tätä kautta.
            }
        }
        if (rev != null) {
            Oppiaine latest = oppiaineRepository.findRevision(aine.getId(), rev);
            if (!latest.structureEquals(aine)) {
                throw new BusinessRuleViolationException(VAIN_KORJAUKSET_SALLITTU);
            }
        }
        oppiaineRepository.setRevisioKommentti(updateDto.getMetadataOrEmpty().getKommentti());
        aine = oppiaineRepository.save(aine);
        oppiaineRepository.setRevisioKommentti("Muokattu oppiainetta " + aine.getNimi().toString());
        eventPublisher.publishEvent(PerusteUpdatedEvent.of(this, perusteId));
        return mapper.map(aine, OppiaineDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public OppiaineenVuosiluokkaKokonaisuusDto updateOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, UpdateDto<OppiaineenVuosiluokkaKokonaisuusDto> updateDto) {
        PerusopetuksenPerusteenSisalto sisalto = perusOpetuksenSisaltoRepository.findByPerusteId(perusteId);
        OppiaineenVuosiluokkaKokonaisuusDto tmp
            = mapper.map(doUpdateOppiaineenVuosiluokkaKokonaisuus(sisalto, oppiaineId, updateDto.getDto(), true), OppiaineenVuosiluokkaKokonaisuusDto.class);
        vuosiluokkakokonaisuusRepository.setRevisioKommentti(updateDto.getMetadataOrEmpty().getKommentti());
        return tmp;
    }

    private OppiaineenVuosiluokkaKokonaisuus doUpdateOppiaineenVuosiluokkaKokonaisuus(PerusopetuksenPerusteenSisalto sisalto, Long oppiaineId, OppiaineenVuosiluokkaKokonaisuusDto dto, boolean lock) {
        OppiaineenVuosiluokkaKokonaisuus ovk = vuosiluokkakokonaisuusRepository.findByIdAndOppiaineId(dto.getId(), oppiaineId);
        if (ovk == null) {
            throw new BusinessRuleViolationException("Vuosiluokkakokonaisuus ei kuulu tähän oppiaineeseen");
        }
        lockService.assertLock(OppiaineLockContext.of(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS, sisalto.getPeruste().getId(), oppiaineId, dto.getId()));
        if (lock) {
            oppiaineRepository.lock(ovk.getOppiaine());
        }
        mapper.map(dto, ovk);
        if ( sisalto.getPeruste().getTila() == PerusteTila.VALMIS ) {
            Revision rev = vuosiluokkakokonaisuusRepository.getLatestRevisionId(ovk.getId());
            OppiaineenVuosiluokkaKokonaisuus latest = vuosiluokkakokonaisuusRepository.findRevision(ovk.getId(), rev.getNumero());
            if ( !latest.structureEquals(ovk) ) {
                throw new BusinessRuleViolationException(VAIN_KORJAUKSET_SALLITTU);
            }
        }
        ovk = vuosiluokkakokonaisuusRepository.save(ovk);
        ovk.getOppiaine().muokattu();
        oppiaineRepository.setRevisioKommentti("Muokattu oppiaineen vuosiluokkakokonaisuutta");
        eventPublisher.publishEvent(PerusteUpdatedEvent.of(this, sisalto.getPeruste().getId()));
        return ovk;
    }

    private Oppiaine getAndLockOppiaine(Long perusteId, Long oppiaineId) {
        PerusopetuksenPerusteenSisalto sisalto = perusOpetuksenSisaltoRepository.findByPerusteId(perusteId);
        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);
        if (sisalto == null || !sisalto.containsOppiaine(aine)) {
            throw new BusinessRuleViolationException("oppiaine ei kuulu tähän perusteeseen");
        }
        oppiaineRepository.lock(aine);
        return aine;
    }

    @Override
    @Transactional(readOnly = false)
    public OpetuksenKohdealueDto addKohdealue(Long perusteId, Long oppiaineId, OpetuksenKohdealueDto kohdealue) {
        Oppiaine aine = getAndLockOppiaine(perusteId, oppiaineId);
        OpetuksenKohdealue kohde = null;
        if (kohdealue.getId() != null) {
            OpetuksenKohdealue vanhaKohde = kohdeAlueRepository.findOne(kohdealue.getId());
            mapper.map(kohdealue, vanhaKohde);
        } else {
            kohde = mapper.map(kohdealue, OpetuksenKohdealue.class);
            kohde = aine.addKohdealue(kohde);
            kohdeAlueRepository.save(kohde);
        }

        return mapper.map(kohde, OpetuksenKohdealueDto.class);

    }

    @Override
    @Transactional
    public void deleteKohdealue(Long perusteId, Long id, Long kohdealueId) {
        getAndLockOppiaine(perusteId, id);
        OpetuksenKohdealue kohdealue = kohdeAlueRepository.getOne(kohdealueId);
        List<OpetuksenTavoite> tavoitteet = oppiaineRepository.findAllTavoitteetByKohdealue(kohdealue);
        for (OpetuksenTavoite t : tavoitteet) {
            t.getKohdealueet().remove(kohdealue);
        }
        Oppiaine oppiaine = oppiaineRepository.findOne(id);
        oppiaine.removeKohdealue(kohdealue);
    }

    @Override
    @Transactional
    public void jarjestaLukioOppiaineet(long perusteId, List<OppiaineJarjestysDto> oppiaineet) {
        Map<Long, OppiaineJarjestysDto> dtosById = oppiaineet.stream().collect(toMap(OppiaineJarjestysDto::getId, o -> o));
        Set<Long> oppiaineIds = new HashSet<>(dtosById.keySet());
        oppiaineIds.addAll(oppiaineet.stream().filter(oa -> oa.getOppiaineId() != null)
                .map(OppiaineJarjestysDto::getOppiaineId).collect(toSet()));
        Map<Long, Oppiaine> byId = oppiaineRepository.findAll(oppiaineIds).stream().collect(toMap(Oppiaine::getId, o -> o));
        dtosById.values().forEach(dto -> {
            Oppiaine oa = found(byId.get(dto.getId()), inLukioPeruste(perusteId));
            oa.setJnro(dto.getJarjestys());
            if (!oa.isKoosteinen() && dto.getOppiaineId() != null) {
                oa.setOppiaine(found(byId.get(dto.getOppiaineId()),
                        inLukioPeruste(perusteId).and(Oppiaine::isKoosteinen)));
            } else {
                oa.setOppiaine(null);
            }
        });
    }

    private static final String OPPIAINETTA_EI_OLE = "Pyydettyä oppiainetta ei ole";
    private static final String VAIN_KORJAUKSET_SALLITTU = "Vain korjaukset sallittu";
}
