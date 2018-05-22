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
package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.ammattitaitovaatimukset.AmmattitaitovaatimuksenKohdealue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.*;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.KommenttiDto;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektinPerusteenosaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueKokonaanDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueLaajaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaamistavoiteLaajaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.repository.*;
import fi.vm.sade.eperusteet.repository.authorization.PerusteprojektiPermissionRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.KommenttiService;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.event.PerusteUpdatedEvent;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.internal.LockManager;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.*;
import java.util.stream.Collectors;
import fi.vm.sade.eperusteet.service.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jhyoty
 */
@Service
public class PerusteenOsaServiceImpl implements PerusteenOsaService {

    @Autowired
    private KommenttiService kommenttiService;

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepo;

    @Autowired
    private TutkinnonOsaRepository tutkinnonOsaRepo;

    @Autowired
    private OsaAlueRepository osaAlueRepository;

    @Autowired
    private OsaamistavoiteRepository osaamistavoiteRepository;

    @Autowired
    private TutkinnonOsaViiteRepository tutkinnonOsaViiteRepository;

    @Autowired
    private PerusteenOsaViiteRepository perusteenOsaViiteRepository;

    @Autowired
    private PerusteRepository perusteet;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private AmmattitaidonvaatimusRepository ammattitaidonvaatimusRepository;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private LockManager lockManager;

    @Autowired
    private ValmaTelmaSisaltoRepository valmaTelmaSisaltoRepository;

    @Autowired
    private PerusteprojektiPermissionRepository perusteprojektiPermissionRepository;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PerusteenOsaDto.Suppea> getAll() {
        return mapper.mapAsList(perusteenOsaRepo.findAll(), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Suppea.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteenOsaDto.Laaja get(final Long id) {
        return mapper.map(perusteenOsaRepo.findOne(id), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteenOsaDto.Laaja getByViite(final Long viiteId) {
        PerusteenOsaViite viite = perusteenOsaViiteRepository.findOne(viiteId);
        if (viite == null || viite.getPerusteenOsa() == null) {
            throw new BusinessRuleViolationException("virheellinen-viiteid");
        }
        return mapper.map(viite.getPerusteenOsa(), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getLatestRevision(final Long id) {
        return perusteenOsaRepo.getLatestRevisionId(id).getNumero();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerusteenOsaDto.Laaja> getAllByKoodiUri(final String koodiUri) {
        return mapper.mapAsList(tutkinnonOsaRepo.findByKoodiUri(koodiUri), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja.class);
    }

    @Override
    @Transactional(readOnly = true)
    public void onkoTutkinnonOsanKoodiKaytossa(final String koodiUri) {
        List<TutkinnonOsa> tosatByKoodi = tutkinnonOsaRepo.findByKoodiUri(koodiUri);
        for (TutkinnonOsa tosa : tosatByKoodi) {
            if (tosa.getTila() == PerusteTila.VALMIS) {
                throw new BusinessRuleViolationException("tutkinnon-osan-koodi-on-jo-kaytossa");
            }
        }
    }

    @Transactional
    private void tarkistaVoikoMuokata(PerusteenOsa osa) {
        if (osa != null && osa.getTila() == PerusteTila.POISTETTU) {
            throw new BusinessRuleViolationException("arkistoitujen-tutkinnon-osien-muokkaus-on-estetty");
        }
    }

    @Override
    @Transactional
    public <T extends PerusteenOsaDto.Laaja> T update(T perusteenOsaDto) {
        assertExists(perusteenOsaDto.getId());
        lockManager.ensureLockedByAuthenticatedUser(perusteenOsaDto.getId());
        PerusteenOsa current = perusteenOsaRepo.findOne(perusteenOsaDto.getId());
        tarkistaVoikoMuokata(current);

        perusteenOsaDto.setTila(current.getTila());
        PerusteenOsa updated = mapper.map(perusteenOsaDto, current.getType());

        if (perusteenOsaDto.getClass().equals(TutkinnonOsaDto.class)) {
            TutkinnonOsa tutkinnonOsa = (TutkinnonOsa) updated;
            tutkinnonOsa.setOsaAlueet(createOsaAlueIfNotExist(tutkinnonOsa.getOsaAlueet()));
            tutkinnonOsa.setValmaTelmaSisalto( createValmatelmaIfNotExist( tutkinnonOsa.getValmaTelmaSisalto() ) );
        }

        if (current.getTila() == PerusteTila.VALMIS && !current.structureEquals(updated)) {
            throw new BusinessRuleViolationException("vain-korjaukset-sallittu");
        }

        if (perusteenOsaDto.getClass().equals(TutkinnonOsaDto.class)) {
            removeMissingFromCurrent(perusteenOsaRepo.findOne(perusteenOsaDto.getId()), updated);
        }

        current.mergeState(updated);
        current = perusteenOsaRepo.save(current);
        notifyUpdate(current);
        mapper.map(current, perusteenOsaDto);
        return perusteenOsaDto;
    }

    private ValmaTelmaSisalto createValmatelmaIfNotExist(ValmaTelmaSisalto valmaTelmaSisalto) {
        ValmaTelmaSisalto tmp = null;

        if (valmaTelmaSisalto != null) {

            if (valmaTelmaSisalto.getId() == null) {
                tmp = valmaTelmaSisaltoRepository.save(valmaTelmaSisalto);
            }else{
                tmp = valmaTelmaSisalto;
            }
        }
        return tmp;
    }

    private void removeMissingFromCurrent(PerusteenOsa current, PerusteenOsa updated) {

        TutkinnonOsa tutkinnonOsa = (TutkinnonOsa) current;
        TutkinnonOsa updatedTutkinnonOsa = (TutkinnonOsa) updated;

        tutkinnonOsa.getAmmattitaitovaatimuksetLista().removeAll(updatedTutkinnonOsa.getAmmattitaitovaatimuksetLista());
        for (AmmattitaitovaatimuksenKohdealue ammattitaitovaatimuksenKohdealue : tutkinnonOsa.getAmmattitaitovaatimuksetLista()) {
            ammattitaidonvaatimusRepository.delete( ammattitaitovaatimuksenKohdealue.getId() );
        }

    }

    @Override
    @Transactional(readOnly = false)
    public <T extends PerusteenOsaDto.Laaja> T update(UpdateDto<T> perusteenOsaDto) {
        T updated = update(perusteenOsaDto.getDto());
        perusteenOsaRepo.setRevisioKommentti(perusteenOsaDto.getMetadataOrEmpty().getKommentti());
        return updated;
    }

    @Override
    @Transactional(readOnly = false)
    public <T extends PerusteenOsaDto.Laaja> T add(PerusteenOsaViite viite, T perusteenOsaDto) {
        PerusteenOsa perusteenOsa = mapper.map(perusteenOsaDto, PerusteenOsa.class);
        viite.setPerusteenOsa(perusteenOsa);
        perusteenOsa = perusteenOsaRepo.saveAndFlush(perusteenOsa);
        mapper.map(perusteenOsa, perusteenOsaDto);
        return perusteenOsaDto;
    }

    private List<OsaAlue> createOsaAlueIfNotExist(List<OsaAlue> osaAlueet) {

        List<OsaAlue> osaAlueTemp = new ArrayList<>();
        if (osaAlueet != null) {
            for (OsaAlue osaAlue : osaAlueet) {
                if (osaAlue.getId() == null) {
                    osaAlueTemp.add(osaAlueRepository.save(osaAlue));
                } else {
                    osaAlueTemp.add(osaAlue);
                }
            }
        }
        return osaAlueTemp;
    }

    @Override
    @Transactional(readOnly = false)
    public OsaAlueLaajaDto addTutkinnonOsaOsaAlue(Long id, OsaAlueLaajaDto osaAlueDto) {
        assertExists(id);
        lockManager.ensureLockedByAuthenticatedUser(id);
        TutkinnonOsa tutkinnonOsa = tutkinnonOsaRepo.findOne(id);

        if (tutkinnonOsa != null && tutkinnonOsa.getTila() == PerusteTila.POISTETTU) {
            throw new BusinessRuleViolationException("arkistoitujen-tutkinnon-osien-muokkaus-on-estetty");
        }

        OsaAlue osaAlue;
        if (osaAlueDto != null) {
            osaAlue = mapper.map(osaAlueDto, OsaAlue.class);
        } else {
            osaAlue = new OsaAlue();
        }
        osaAlueRepository.save(osaAlue);
        tutkinnonOsa.getOsaAlueet().add(osaAlue);
        tutkinnonOsaRepo.save(tutkinnonOsa);

        return mapper.map(osaAlue, OsaAlueLaajaDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public OsaAlueKokonaanDto getTutkinnonOsaOsaAlue(Long viiteId, Long osaAlueId) {
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(viiteId);
        if (viite == null || viite.getTutkinnonOsa() == null || viite.getTutkinnonOsa().getOsaAlueet() == null) {
            throw new BusinessRuleViolationException("virheellinen-viiteid");
        }

        List<Long> osaAlueIds = new ArrayList<>();
        for (OsaAlue osaAlue : viite.getTutkinnonOsa().getOsaAlueet()) {
            osaAlueIds.add(osaAlue.getId());
        }
        if (!osaAlueIds.contains(osaAlueId)) {
            throw new BusinessRuleViolationException("virheellinen-osaalueid");
        }

        return mapper.map(osaAlueRepository.findOne(osaAlueId), OsaAlueKokonaanDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public OsaAlueKokonaanDto updateTutkinnonOsaOsaAlue(Long viiteId, Long osaAlueId, OsaAlueKokonaanDto osaAlue) {

        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(viiteId);
        if (viite == null || viite.getTutkinnonOsa() == null || viite.getTutkinnonOsa().getOsaAlueet() == null) {
            throw new BusinessRuleViolationException("virheellinen-viiteid");
        }

        Long id = viite.getTutkinnonOsa().getId();
        assertExists(id);
        lockManager.ensureLockedByAuthenticatedUser(id);
        OsaAlue osaAlueEntity = osaAlueRepository.findOne(osaAlueId);
        if (osaAlueEntity == null) {
            throw new EntityNotFoundException("osa-aluetta-ei-loytynyt-idlla",
                    new HashMap<String, Object>(){{ put("osaAlueId", osaAlueId); }});
        }
        List<Osaamistavoite> uudetTavoitteet = tallennaUudetOsaamistavoitteet(osaAlue.getOsaamistavoitteet());

        OsaAlue osaAlueTmp = mapper.map(osaAlue, OsaAlue.class);
        osaAlueEntity.mergeState(osaAlueTmp);
        osaAlueEntity.getOsaamistavoitteet().addAll(uudetTavoitteet);
        osaAlueEntity.setValmaTelmaSisalto( createValmatelmaIfNotExist( osaAlueTmp.getValmaTelmaSisalto() ));
        osaAlueRepository.save(osaAlueEntity);

        aiheutaUusiTutkinnonOsaViiteRevisio(viiteId);
        notifyUpdate(viite.getTutkinnonOsa());
        return mapper.map(osaAlueEntity, OsaAlueKokonaanDto.class);
    }

    private void notifyUpdate(PerusteenOsa osa) {
         // Varmistetaan että tutkinnon osan muokkaus muuttaa valmiiden perusteiden viimeksi muokattu -päivämäärää (ja aiheuttaa uuden version).
        if (osa.getTila() == PerusteTila.VALMIS) {
            Set<Long> perusteIds;
            if ( osa instanceof TutkinnonOsa ) {
                perusteIds = perusteet.findByTutkinnonosaId(osa.getId(), PerusteTila.VALMIS);
            } else {
                final List<Long> roots = perusteenOsaViiteRepository.findRootsByPerusteenOsaId(osa.getId());
                perusteIds = roots.isEmpty() ? Collections.<Long>emptySet() : perusteet.findBySisaltoRoots(roots, PerusteTila.VALMIS);
            }
            for (Long perusteId : perusteIds) {
                eventPublisher.publishEvent(PerusteUpdatedEvent.of(this, perusteId));
            }
        }
    }

    private List<AmmattitaitovaatimuksenKohdealue> connectAmmattitaitovaatimusListToOsaamistavoite(Osaamistavoite tavoite) {
        for (AmmattitaitovaatimuksenKohdealue ammattitaitovaatimuksenKohdealue : tavoite.getAmmattitaitovaatimuksetLista()) {
            ammattitaitovaatimuksenKohdealue.connectAmmattitaitovaatimuksetToKohdealue( ammattitaitovaatimuksenKohdealue );
        }
        return tavoite.getAmmattitaitovaatimuksetLista();
    }

    @Transactional(readOnly = false)
    private List<Osaamistavoite> tallennaUudetOsaamistavoitteet(List<OsaamistavoiteLaajaDto> osaamistavoitteet) {
        List<Osaamistavoite> uudet = new ArrayList<>();

        Long tempId;
        Osaamistavoite tallennettuPakollinenTavoite;
        Iterator<OsaamistavoiteLaajaDto> osaamistavoiteDtoItr = osaamistavoitteet.iterator();
        // Tallennetaan uudet pakolliset osaamistavoitteet ja asetetaan valinnaisten osaamistavoitteiden esitietoid:t oikeiksi kantaId:ksi
        while (osaamistavoiteDtoItr.hasNext()) {
            OsaamistavoiteLaajaDto osaamistavoiteDto = osaamistavoiteDtoItr.next();
            // Jos id >= 0, niin kyseessä oikea tietokanta id. Id:t < 0 ovat generoitu UI päässä.
            if (osaamistavoiteDto.isPakollinen() && (osaamistavoiteDto.getId() == null || osaamistavoiteDto.getId() < 0)) {
                tempId = osaamistavoiteDto.getId();
                osaamistavoiteDto.setId(null);
                tallennettuPakollinenTavoite = mapper.map(osaamistavoiteDto, Osaamistavoite.class);
                connectAmmattitaitovaatimusListToOsaamistavoite(tallennettuPakollinenTavoite);
                tallennettuPakollinenTavoite = osaamistavoiteRepository.save( tallennettuPakollinenTavoite );
                uudet.add(tallennettuPakollinenTavoite);

                // käydään läpi valinnaiset ja asetetaan esitieto id kohdalleen.
                for (OsaamistavoiteLaajaDto osaamistavoiteValinnainenDto : osaamistavoitteet) {
                    if (!osaamistavoiteValinnainenDto.isPakollinen() && osaamistavoiteValinnainenDto.getEsitieto() != null) {
                        if (osaamistavoiteValinnainenDto.getEsitieto().getId().equals(tempId.toString())) {
                            osaamistavoiteValinnainenDto.setEsitieto(new EntityReference(tallennettuPakollinenTavoite.getId()));
                        }
                    }
                }
                osaamistavoiteDtoItr.remove();
            }
        }

        // Käydään vielä läpi valinnaiset osaamistavoitteet ja tallennetaan uudet.
        osaamistavoiteDtoItr = osaamistavoitteet.iterator();
        while (osaamistavoiteDtoItr.hasNext()) {
            OsaamistavoiteLaajaDto osaamistavoiteDto = osaamistavoiteDtoItr.next();
            // Jos id >= 0, niin kyseessä oikea tietokanta id. Id:t < 0 ovat generoitu UI päässä.
            if (!osaamistavoiteDto.isPakollinen() && (osaamistavoiteDto.getId() == null || osaamistavoiteDto.getId() < 0)) {
                osaamistavoiteDto.setId(null);

                tallennettuPakollinenTavoite = mapper.map(osaamistavoiteDto, Osaamistavoite.class);
                connectAmmattitaitovaatimusListToOsaamistavoite(tallennettuPakollinenTavoite);
                tallennettuPakollinenTavoite = osaamistavoiteRepository.save( tallennettuPakollinenTavoite );
                uudet.add(tallennettuPakollinenTavoite);
                osaamistavoiteDtoItr.remove();
            }
        }

        return uudet;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OsaAlueKokonaanDto> getTutkinnonOsaOsaAlueet(Long id) {
        TutkinnonOsa tutkinnonOsa = tutkinnonOsaRepo.findOne(id);
        if (tutkinnonOsa == null) {
            throw new EntityNotFoundException("tutkinnon-osaa-ei-loytynyt-idlla",
                    new HashMap<String, Object>(){{ put("id", id); }});
        }

        return mapper.mapAsList(tutkinnonOsa.getOsaAlueet(), OsaAlueKokonaanDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OsaAlueKokonaanDto> getTutkinnonOsaOsaAlueetVersio(Long id, Integer versioId) {
        TutkinnonOsa t = tutkinnonOsaRepo.findRevision(id, versioId);
        if (t == null) {
            throw new EntityNotFoundException("tutkinnon-osa-id-versiota-ei-löoytynyt-versioidlla",
                    new HashMap<String, Object>(){{ put("id", id); put("versioId", versioId); }});
        }
        return mapper.mapAsList(t.getOsaAlueet(), OsaAlueKokonaanDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public void removeOsaAlue(Long id, Long osaAlueId) {
        assertExists(id);
        lockManager.ensureLockedByAuthenticatedUser(id);
        OsaAlue osaAlue = osaAlueRepository.findOne(osaAlueId);
        if (osaAlue == null) {
            throw new EntityNotFoundException("osa-aluetta-ei-loytynyt-idlla",
                    new HashMap<String, Object>(){{ put("osaAlueId", osaAlueId); }});
        }
        TutkinnonOsa tutkinnonOsa = tutkinnonOsaRepo.findOne(id);

        if (tutkinnonOsa != null && tutkinnonOsa.getTila() == PerusteTila.POISTETTU) {
            throw new BusinessRuleViolationException("arkistoitujen-tutkinnon-osien-muokkaus-on-estetty");
        }

        tutkinnonOsa.getOsaAlueet().remove(osaAlue);
        osaAlueRepository.delete(osaAlue);
    }

    @Override
    @Transactional(readOnly = false)
    public OsaamistavoiteLaajaDto addOsaamistavoite(Long id, Long osaAlueId, OsaamistavoiteLaajaDto osaamistavoiteDto) {
        assertExists(id);
        lockManager.ensureLockedByAuthenticatedUser(id);
        OsaAlue osaAlue = osaAlueRepository.findOne(osaAlueId);
        if (osaAlue == null) {
            throw new EntityNotFoundException("osa-aluetta-ei-loytynyt-idlla",
                    new HashMap<String, Object>(){{ put("osaAlueId", osaAlueId); }});
        }
        Osaamistavoite osaamistavoite;
        if (osaamistavoiteDto != null) {
            osaamistavoite = mapper.map(osaamistavoiteDto, Osaamistavoite.class);
        } else {
            osaamistavoite = new Osaamistavoite();
        }
        osaamistavoiteRepository.save(osaamistavoite);
        osaAlue.getOsaamistavoitteet().add(osaamistavoite);
        osaAlueRepository.save(osaAlue);

        return mapper.map(osaamistavoite, OsaamistavoiteLaajaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OsaamistavoiteLaajaDto> getOsaamistavoitteet(Long id, Long osaAlueId) {
        assertExists(id);
        OsaAlue osaAlue = osaAlueRepository.findOne(osaAlueId);
        if (osaAlue == null) {
            throw new EntityNotFoundException("osa-aluetta-ei-loytynyt-idlla",
                    new HashMap<String, Object>(){{ put("osaAlueId", osaAlueId); }});
        }
        return mapper.mapAsList(osaAlue.getOsaamistavoitteet(), OsaamistavoiteLaajaDto.class);

    }

    @Override
    @Transactional(readOnly = false)
    public void removeOsaamistavoite(Long id, Long osaAlueId, Long osaamistavoiteId) {
        assertExists(id);
        lockManager.ensureLockedByAuthenticatedUser(id);
        PerusteenOsa current = perusteenOsaRepo.findOne(id);
        if (current != null && current.getTila() == PerusteTila.POISTETTU) {
            throw new BusinessRuleViolationException("arkistoitujen-tutkinnon-osien-muokkaus-on-estetty");
        }

        OsaAlue osaAlue = osaAlueRepository.findOne(osaAlueId);
        if (osaAlue == null) {
            throw new EntityNotFoundException("osa-aluetta-ei-loytynyt-idlla",
                    new HashMap<String, Object>(){{ put("osaAlueId", osaAlueId); }});
        }
        Osaamistavoite osaamistavoiteEntity = osaamistavoiteRepository.findOne(osaamistavoiteId);
        if (osaamistavoiteEntity == null) {
            throw new EntityNotFoundException("osaamistavoitetta-ei-loytynyt-idlla",
                    new HashMap<String, Object>(){{ put("osaamistavoiteId", osaamistavoiteId); }});
        }
        osaAlue.getOsaamistavoitteet().remove(osaamistavoiteEntity);
        osaamistavoiteRepository.delete(osaamistavoiteEntity);
    }

    @Override
    @Transactional(readOnly = false)
    public OsaamistavoiteLaajaDto updateOsaamistavoite(Long id, Long osaAlueId, Long osaamistavoiteId, OsaamistavoiteLaajaDto osaamistavoite) {
        assertExists(id);
        PerusteenOsa current = perusteenOsaRepo.findOne(id);
        if (current != null && current.getTila() == PerusteTila.POISTETTU) {
            throw new BusinessRuleViolationException("arkistoitujen-tutkinnon-osien-muokkaus-on-estetty");
        }

        lockManager.ensureLockedByAuthenticatedUser(id);
        OsaAlue osaAlue = osaAlueRepository.findOne(osaAlueId);
        if (osaAlue == null) {
            throw new EntityNotFoundException("osa-aluetta-ei-loytynyt-idlla",
                    new HashMap<String, Object>(){{ put("osaAlueId", osaAlueId); }});
        }
        Osaamistavoite osaamistavoiteEntity = osaamistavoiteRepository.findOne(osaamistavoiteId);
        if (osaamistavoiteEntity == null) {
            throw new EntityNotFoundException("osaamistavoitetta-ei-loytynyt-idlla",
                    new HashMap<String, Object>(){{ put("osaamistavoiteId", osaamistavoiteId); }});
        }
        Osaamistavoite osaamistavoiteUusi = mapper.map(osaamistavoite, Osaamistavoite.class);
        osaamistavoiteEntity.mergeState(osaamistavoiteUusi);
        notifyUpdate(perusteenOsaRepo.findOne(id));
        return mapper.map(osaamistavoiteEntity, OsaamistavoiteLaajaDto.class);
    }

    @Override
    public void delete(final Long id) {
        assertExists(id);
        PerusteenOsa current = perusteenOsaRepo.findOne(id);
        if (current != null && current.getTila() == PerusteTila.POISTETTU) {
            throw new BusinessRuleViolationException("arkistoitujen-tutkinnon-osien-muokkaus-on-estetty");
        }

        lockManager.lock(id);
        try {
            List<KommenttiDto> allByPerusteenOsa = kommenttiService.getAllByPerusteenOsa(id);
            for (KommenttiDto kommentti : allByPerusteenOsa) {
                kommenttiService.deleteReally(kommentti.getId());
            }
            perusteenOsaRepo.delete(id);
        } finally {
            lockManager.unlock(id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Suppea> getAllWithName(String name) {
        return mapper
            .mapAsList(tutkinnonOsaRepo.findByNimiTekstiTekstiContainingIgnoreCase(name), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Suppea.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Revision> getVersiot(Long id) {
        return perusteenOsaRepo.getRevisions(id);
    }

    @Override
    @Transactional(readOnly = true)
    public fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja getVersio(Long id, Integer versioId) {
        return mapper.map(perusteenOsaRepo.findRevision(id, versioId), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Revision> getVersiotByViite(Long id) {
        PerusteenOsaViite p = perusteenOsaViiteRepository.findOne(id);
        if (p == null || p.getPerusteenOsa() == null) {
            throw new EntityNotFoundException("perusteen-osaa-ei-loytynyt-viite-idlla",
                    new HashMap<String, Object>(){{ put("id", id); }});
        }
        return getVersiot(p.getPerusteenOsa().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteenOsaDto getVersioByViite(Long id, Integer versioId) {
        PerusteenOsaViite p = perusteenOsaViiteRepository.findOne(id);
        if (p == null || p.getPerusteenOsa() == null) {
            throw new EntityNotFoundException("perusteen-osaa-ei-loytynyt-viite-idlla",
                    new HashMap<String, Object>(){{ put("id", id); }});
        }
        return getVersio(p.getPerusteenOsa().getId(), versioId);
    }

    @Override
    @Transactional
    public fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja revertToVersio(Long id, Integer versioId) {
        PerusteenOsa revision = perusteenOsaRepo.findRevision(id, versioId);
        return update(mapper.map(revision, fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja.class));
    }

    @Override
    public LukkoDto lock(Long id) {
        assertExists(id);
        return LukkoDto.of(lockManager.lock(id));
    }

    @Override
    public void unlock(Long id) {
        lockManager.unlock(id);
    }

    @Override
    public LukkoDto getLock(Long id) {
        assertExists(id);
        LukkoDto lukko = LukkoDto.of(lockManager.getLock(id));
        lockManager.lisaaNimiLukkoon(lukko);
        return lukko;
    }

    @Override
    @Transactional(readOnly = true)
    public Revision getLastModifiedRevision(final Long id) {
        PerusteTila tila = perusteenOsaRepo.getTila(id);
        if (tila == null) {
            return null;
        }
        if (tila == PerusteTila.LUONNOS) {
            //luonnos-tilassa olevan perusteen viimeisimmän muokkauksen määrittäminen on epäluotettavaa.
            return Revision.DRAFT;
        }
        return perusteenOsaRepo.getLatestRevisionId(id);
    }


    private void assertExists(Long id) {
        if (!perusteenOsaRepo.exists(id)) {
            throw new NotExistsException("pyydettya-perusteen-osaa-ei-ole-olemassa");
        }
    }

    private void aiheutaUusiTutkinnonOsaViiteRevisio(Long id) {
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(id);
        if (viite != null) {
            viite.setMuokattu(new Date());
            tutkinnonOsaViiteRepository.save(viite);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Set<PerusteprojektinPerusteenosaDto> getOwningProjektit(Long id) {
        return perusteprojektiPermissionRepository.findAllByPerusteenosa(id).stream()
                .map(pp -> pp.getPerusteProjektiId())
                .map(perusteProjektiId -> perusteprojektiRepository.findOne(perusteProjektiId))
                .map(pp -> mapper.map(pp, PerusteprojektinPerusteenosaDto.class))
                .collect(Collectors.toSet());
    }

}
