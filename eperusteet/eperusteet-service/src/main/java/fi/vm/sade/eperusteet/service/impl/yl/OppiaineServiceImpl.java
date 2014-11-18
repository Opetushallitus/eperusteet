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

import fi.vm.sade.eperusteet.domain.yl.OpetuksenTavoite;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.domain.yl.OppiaineenVuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.yl.OpetuksenKohdealueDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineenVuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.OppiaineRepository;
import fi.vm.sade.eperusteet.repository.OppiaineenVuosiluokkakokonaisuusRepository;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.OppiaineLockContext;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private OppiaineenVuosiluokkakokonaisuusRepository vuosiluokkakokonaisuusRepository;

    @Autowired
    private PerusopetuksenPerusteenSisaltoRepository sisaltoRepository;

    @Autowired
    @LockCtx(OppiaineLockContext.class)
    private LockService<OppiaineLockContext> lockService;

    private static final Logger LOG = LoggerFactory.getLogger(OppiaineServiceImpl.class);

    @Override
    @Transactional(readOnly = false)
    public OppiaineDto addOppiaine(Long perusteId, OppiaineDto dto) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
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
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
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
    public void deleteOppiaine(Long perusteId, Long oppiaineId) {
        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        if (sisalto == null || !sisalto.containsOppiaine(aine)) {
            throw new BusinessRuleViolationException("Oppiainetta ei ole tai se ei kuulu tähän perusteeseen");
        }
        final OppiaineLockContext ctx = OppiaineLockContext.of(perusteId, oppiaineId, null);
        oppiaineRepository.lock(aine);
        lockService.lock(ctx);
        try {
            for (OppiaineenVuosiluokkaKokonaisuus k : aine.getVuosiluokkakokonaisuudet()) {
                deleteOppiaineenVuosiluokkaKokonaisuus(perusteId, oppiaineId, k.getId());
            }

            if (aine.isKoosteinen()) {
                for (Oppiaine m : aine.getOppimaarat()) {
                    final OppiaineLockContext vkctx = OppiaineLockContext.of(perusteId, oppiaineId, null);
                    lockService.lock(vkctx);
                    deleteOppiaine(perusteId, m.getId());
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
        OppiaineenVuosiluokkaKokonaisuus vk = vuosiluokkakokonaisuusRepository.findOne(vuosiluokkaKokonaisuusId);
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        if (sisalto == null || vk == null || !sisalto.containsOppiaine(vk.getOppiaine())) {
            throw new BusinessRuleViolationException("Virheellinen vuosiluokkakokonaisuus");
        }
        OppiaineLockContext ctx = OppiaineLockContext.of(perusteId, oppiaineId, vk.getId());
        lockService.lock(ctx);
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
    public OppiaineDto getOppiaine(long perusteId, long oppiaineId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);

        if (sisalto != null && sisalto.containsOppiaine(aine)) {
            return mapper.map(aine, OppiaineDto.class);
        } else {
            throw new BusinessRuleViolationException("Pyydettyä oppiainetta ei ole");
        }
    }

    @Override
    public OppiaineDto getOppiaine(long perusteId, long oppiaineId, int revisio) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        sisalto = sisaltoRepository.findRevision(sisalto.getId(), revisio);
        Oppiaine aine = oppiaineRepository.findRevision(oppiaineId, revisio);
        if (sisalto != null && sisalto.containsOppiaine(aine)) {
            return mapper.map(aine, OppiaineDto.class);
        } else {
            throw new BusinessRuleViolationException("Pyydettyä oppiainetta ei ole");
        }
    }

    @Override
    public List<Revision> getOppiaineRevisions(long perusteId, long oppiaineId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        sisalto = sisaltoRepository.findOne(sisalto.getId());
        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);
        if (sisalto != null && sisalto.containsOppiaine(aine)) {
            return oppiaineRepository.getRevisions(oppiaineId);
        } else {
            throw new BusinessRuleViolationException("Pyydettyä oppiainetta ei ole");
        }
    }

    @Override
    public OppiaineDto revertOppiaine(long perusteId, long oppiaineId, int revisio) {

        //TODO. ei toimi jos palautettava versio viittaa poistettuihin entiteetteihin
        //(keskeinensisältöalue, vuosiluokkakokonaisuus, laaja-alainen osaaminen)
        //sama ongelma on toki updatessakin (ei voi luoda uusia sisältöalueta ja viitata niihin tavoitteista)

        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        sisalto = sisaltoRepository.findRevision(sisalto.getId(), revisio);
        Oppiaine aine = oppiaineRepository.findRevision(oppiaineId, revisio);
        if (sisalto != null && sisalto.containsOppiaine(aine)) {
            OppiaineDto dto = mapper.map(aine, OppiaineDto.class);
            return updateOppiaine(perusteId, dto);
        } else {
            throw new BusinessRuleViolationException("Pyydettyä oppiainetta ei ole");
        }
    }

    @Override
    public OppiaineenVuosiluokkaKokonaisuusDto getOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, Long vuosiluokkaKokonaisuusId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        OppiaineenVuosiluokkaKokonaisuus vk = sisalto == null ? null
            : vuosiluokkakokonaisuusRepository.findByIdAndOppiaineId(vuosiluokkaKokonaisuusId, oppiaineId);
        if (sisalto != null && vk != null && sisalto.containsOppiaine(vk.getOppiaine())) {
            return mapper.map(vk, OppiaineenVuosiluokkaKokonaisuusDto.class);
        } else {
            throw new BusinessRuleViolationException("Pyydettyä vuosiluokkakokonaisuutta ei ole");
        }
    }

    @Override
    public List<OppiaineSuppeaDto> getOppimaarat(Long perusteId, Long oppiaineId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        Oppiaine oa = oppiaineRepository.findOne(oppiaineId);
        if (sisalto.containsOppiaine(oa)) {
            return mapper.mapAsList(oa.getOppimaarat(), OppiaineSuppeaDto.class);
        }
        throw new BusinessRuleViolationException("Pyydettyä oppiainetta ei ole");
    }

    @Override
    @Transactional(readOnly = false)
    public OppiaineDto updateOppiaine(Long perusteId, OppiaineDto dto) {
        Oppiaine aine = oppiaineRepository.findOne(dto.getId());
        if (aine == null) {
            throw new BusinessRuleViolationException("Oppiainetta ei ole");
        }
        lockService.assertLock(OppiaineLockContext.of(perusteId, dto.getId(), null));
        oppiaineRepository.lock(aine);

        mapper.map(dto, aine);
        final Set<OppiaineenVuosiluokkaKokonaisuusDto> vuosiluokkakokonaisuudet = dto.getVuosiluokkakokonaisuudet();
        if (vuosiluokkakokonaisuudet != null) {
            for (OppiaineenVuosiluokkaKokonaisuusDto v : vuosiluokkakokonaisuudet) {
                if (v.getId() == null) {
                    addOppiaineenVuosiluokkaKokonaisuus(aine, v);
                } else {
                    final OppiaineLockContext vkctx = OppiaineLockContext.of(perusteId, dto.getId(), v.getId());
                    try {
                        lockService.lock(vkctx);
                        doUpdateOppiaineenVuosiluokkaKokonaisuus(perusteId, aine.getId(), v, false);
                    } finally {
                        lockService.unlock(vkctx);
                    }
                }
                //TODO poisto -- vai pitäikö jättää "ominaisuudeksi" että vuosiluokkakokonaisuuksia ei voi poistaa tätä kautta.
            }
        }
        aine = oppiaineRepository.save(aine);
        return mapper.map(aine, OppiaineDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public OppiaineenVuosiluokkaKokonaisuusDto updateOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, OppiaineenVuosiluokkaKokonaisuusDto dto) {
        return mapper.map(doUpdateOppiaineenVuosiluokkaKokonaisuus(perusteId, oppiaineId, dto, true), OppiaineenVuosiluokkaKokonaisuusDto.class);
    }

    private OppiaineenVuosiluokkaKokonaisuus doUpdateOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, OppiaineenVuosiluokkaKokonaisuusDto dto, boolean lock) {
        OppiaineenVuosiluokkaKokonaisuus ovk = vuosiluokkakokonaisuusRepository.findOne(dto.getId());
        if (ovk == null || !ovk.getOppiaine().getId().equals(oppiaineId)) {
            throw new BusinessRuleViolationException("Vuosiluokkakokonaisuus ei kuulu tähän oppiaineeseen");
        }
        lockService.assertLock(OppiaineLockContext.of(perusteId, oppiaineId, dto.getId()));
        if (lock) {
            oppiaineRepository.lock(ovk.getOppiaine());
        }
        mapper.map(dto, ovk);
        ovk = vuosiluokkakokonaisuusRepository.save(ovk);
        ovk.getOppiaine().muokattu();
        return ovk;
    }

    @Override
    @Transactional(readOnly = false)
    public Set<OpetuksenKohdealueDto> updateKohdealueet(Long perusteId, Long oppiaineId, Set<OpetuksenKohdealueDto> kohdealueet) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);
        if (sisalto == null || !sisalto.containsOppiaine(aine)) {
            throw new BusinessRuleViolationException("oppiaine ei kuulu tähän perusteeseen");
        }
        oppiaineRepository.lock(aine);

        OppiaineDto tmp = new OppiaineDto();
        tmp.setId(oppiaineId);
        tmp.setKohdealueet(kohdealueet);
        mapper.map(tmp, aine);
        aine = oppiaineRepository.save(aine);

        return mapper.mapToCollection(aine.getKohdealueet(), new HashSet<OpetuksenKohdealueDto>(), OpetuksenKohdealueDto.class);

    }

}
