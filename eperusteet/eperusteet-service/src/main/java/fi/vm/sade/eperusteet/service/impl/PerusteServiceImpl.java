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

import fi.vm.sade.eperusteet.domain.Koulutus;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.Tila;
import fi.vm.sade.eperusteet.domain.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.PageDto;
import fi.vm.sade.eperusteet.dto.PerusteDto;
import fi.vm.sade.eperusteet.dto.PerusteInfoDto;
import fi.vm.sade.eperusteet.dto.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.PerusteQuery;
import fi.vm.sade.eperusteet.dto.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.PerusteenSisaltoViiteDto;
import fi.vm.sade.eperusteet.dto.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.UpdateDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.repository.KoulutusRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.RakenneRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.KoulutusalaService;
import fi.vm.sade.eperusteet.service.LockManager;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.SuoritustapaService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author jhyoty
 */
@Service
@Transactional
public class PerusteServiceImpl implements PerusteService {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteServiceImpl.class);
    private static final String KOODISTO_REST_URL = "https://virkailija.opintopolku.fi/koodisto-service/rest/json/";
    private static final String KOODISTO_RELAATIO_YLA = "relaatio/sisaltyy-ylakoodit/";
    private static final String KOODISTO_RELAATIO_ALA = "relaatio/sisaltyy-alakoodit/";
    private static final String[] KOULUTUSTYYPPI_URIT = {"koulutustyyppi_1", "koulutustyyppi_11", "koulutustyyppi_12"};
    private static final String KOULUTUSALALUOKITUS = "koulutusalaoph2002";
    private static final String OPINTOALALUOKITUS = "opintoalaoph2002";

    private static final List<String> ERIKOISTAPAUKSET = new ArrayList<>(Arrays.asList(new String[]{"koulutus_357802",
        "koulutus_327110", "koulutus_354803", "koulutus_324111", "koulutus_354710",
        "koulutus_324125", "koulutus_357709", "koulutus_327124", "koulutus_355904", "koulutus_324129", "koulutus_358903",
        "koulutus_327127",
        "koulutus_355412", "koulutus_324126", "koulutus_355413", "koulutus_324127", "koulutus_358412", "koulutus_327126",
        "koulutus_354708",
        "koulutus_324123", "koulutus_357707", "koulutus_327122"}));

    @Autowired
    PerusteRepository perusteet;

    @Autowired
    KoulutusRepository koulutusRepo;

    @Autowired
    PerusteenOsaViiteRepository rakenteenOsaRepository;

    @Autowired
    KoulutusalaService koulutusalaService;

    @Autowired
    private SuoritustapaService suoritustapaService;

    @Autowired
    PerusteenOsaViiteRepository perusteenOsaViiteRepo;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    @Koodisto
    private DtoMapper koodistoMapper;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;

    @Autowired
    private TutkinnonOsaViiteRepository tutkinnonOsaViiteRepository;

    @Autowired
    private LockManager lockManager;

    @Autowired
    private RakenneRepository rakenneRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteDto> getAll(PageRequest page, String kieli) {
        return findBy(page, new PerusteQuery());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerusteInfoDto> getAllInfo() {
        return mapper.mapAsList(perusteet.findAll(), PerusteInfoDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteDto> findBy(PageRequest page, PerusteQuery pquery) {
        Page<Peruste> result = perusteet.findBy(page, pquery);
        return new PageDto<>(result, PerusteDto.class, page, mapper);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteInfoDto> findByInfo(PageRequest page, PerusteQuery pquery) {
        Page<Peruste> result = perusteet.findBy(page, pquery);
        return new PageDto<>(result, PerusteInfoDto.class, page, mapper);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteDto get(final Long id) {
        Peruste p = perusteet.findById(id);
        return mapper.map(p, PerusteDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteKaikkiDto getKokoSisalto(final Long id) {
        PerusteKaikkiDto peruste = mapper.map(perusteet.findById(id), PerusteKaikkiDto.class);
        Map<Suoritustapakoodi, RakenneModuuliDto> rakenteet = new HashMap<>();
        Map<Suoritustapakoodi, List<TutkinnonOsaDto>> tutkinnonOsat = new HashMap<>();
        Map<Suoritustapakoodi, List<TutkinnonOsaViiteDto>> tutkinnonOsaViitteet = new HashMap<>();
        Map<Suoritustapakoodi, PerusteenOsaViiteDto> sisallot = new HashMap<>();

        for (SuoritustapaDto st : peruste.getSuoritustavat()) {
            rakenteet.put(st.getSuoritustapakoodi(), getTutkinnonRakenne(id, st.getSuoritustapakoodi(), 0));
            List<TutkinnonOsaViiteDto> tovat = getTutkinnonOsat(id, st.getSuoritustapakoodi());
            List<TutkinnonOsaDto> tosat = new ArrayList<>();

            for (TutkinnonOsaViiteDto tova : tovat) {
                PerusteenOsa tosa = perusteenOsaRepository.findOne(Long.parseLong(tova.getTutkinnonOsa().getId()));
                if (tosa instanceof TutkinnonOsa) {
                    tosat.add(mapper.map((TutkinnonOsa) tosa, TutkinnonOsaDto.class));
                }
            }

            tutkinnonOsaViitteet.put(st.getSuoritustapakoodi(), tovat);
            tutkinnonOsat.put(st.getSuoritustapakoodi(), tosat);
            sisallot.put(st.getSuoritustapakoodi(), getSuoritustapaSisalto(id, st.getSuoritustapakoodi()));
        }

        peruste.setRakenteet(rakenteet);
        peruste.setTutkinnonOsat(tutkinnonOsat);
        peruste.setTutkinnonOsaViitteet(tutkinnonOsaViitteet);
        peruste.setSisallot(sisallot);
        return peruste;
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteDto getByIdAndSuoritustapa(final Long id, Suoritustapakoodi suoritustapakoodi) {
        Peruste p = perusteet.findPerusteByIdAndSuoritustapakoodi(id, suoritustapakoodi);
        return mapper.map(p, PerusteDto.class);
    }

    @Override
    public PerusteDto update(long id, PerusteDto perusteDto) {
        Peruste perusteVanha = perusteet.findById(id);
        if (perusteVanha == null) {
            throw new BusinessRuleViolationException("Päivitettävää perustetta ei ole olemassa");
        }

        perusteet.lock(perusteVanha);
        perusteDto.setId(id);
        Peruste peruste = mapper.map(perusteDto, Peruste.class);
        peruste = checkIfKoulutuksetAlreadyExists(peruste);
        peruste.setSuoritustavat(perusteVanha.getSuoritustavat());
        peruste = perusteet.save(peruste);
        return mapper.map(peruste, PerusteDto.class);
    }

    private Peruste checkIfKoulutuksetAlreadyExists(Peruste peruste) {
        Set<Koulutus> koulutukset = new HashSet<>();
        Koulutus koulutusTemp;

        if (peruste != null && peruste.getKoulutukset() != null) {
            for (Koulutus koulutus : peruste.getKoulutukset()) {
                koulutusTemp = koulutusRepo.findOneByKoulutuskoodi(koulutus.getKoulutuskoodi());
                if (koulutusTemp != null) {
                    koulutukset.add(koulutusTemp);
                } else {
                    koulutukset.add(koulutus);
                }
            }
            peruste.setKoulutukset(koulutukset);
        }
        return peruste;
    }

    @Override
    @Transactional
    public PerusteenOsaViite addViite(final Long parentId, final Long seuraavaViite, PerusteenOsaViite viite) {
        LOG.info("ennen = " + seuraavaViite);
        throw new RuntimeException("not implemented yet!");
//        PerusteenOsaViite v = viitteet.findOne(parentId);
//        viite.setVanhempi(v);
//        int i = 0;
//        if (seuraavaViite != null) {
//            for (PerusteenOsaViite o : v.getLapset()) {
//                if (o.getId().equals(seuraavaViite)) {
//                   break;
//                }
//                i++;
//            }
//        } else {
//            v.getLapset().size();
//        }
//        v.getLapset().add(i, viite);
//        return viitteet.save(viite);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteenOsaViiteDto getSuoritustapaSisalto(Long perusteId, Suoritustapakoodi suoritustapakoodi) {
        PerusteenOsaViite entity = perusteet.findSisaltoByIdAndSuoritustapakoodi(perusteId, suoritustapakoodi);
        return mapper.map(entity, PerusteenOsaViiteDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public SuoritustapaDto getSuoritustapa(Long perusteId, Suoritustapakoodi suoritustapakoodi) {
        Suoritustapa entity = getSuoritustapaEntity(perusteId, suoritustapakoodi);
        return mapper.map(entity, SuoritustapaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public RakenneModuuliDto getTutkinnonRakenne(Long perusteid, Suoritustapakoodi suoritustapakoodi, Integer eTag) {

        Long rakenneId = rakenneRepository.getRakenneIdWithPerusteAndSuoritustapa(perusteid, suoritustapakoodi);
        if (rakenneId == null) {
            throw new BusinessRuleViolationException("Rakennetta ei ole olemassa");
        }
        Integer rakenneVersioId = rakenneRepository.getLatestRevisionId(rakenneId);
        if (eTag != null && rakenneVersioId != null && rakenneVersioId.equals(eTag)) {
            return null;
        }

        RakenneModuuli rakenne = rakenneRepository.findOne(rakenneId);
        RakenneModuuliDto rakenneModuuliDto = mapper.map(rakenne, RakenneModuuliDto.class);
        rakenneModuuliDto.setVersioId(rakenneVersioId);
        return rakenneModuuliDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Revision> getRakenneVersiot(Long id, Suoritustapakoodi suoritustapakoodi) {
        List<Revision> versiot = new ArrayList<>();

        Peruste peruste = perusteet.findOne(id);
        if (peruste == null) {
            throw new EntityNotFoundException("Perustetta ei löytynyt id:llä: " + id);
        }
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        if (suoritustapa == null) {
            throw new EntityNotFoundException("Suoritustapaa " + suoritustapakoodi.toString() + " ei löytynyt");
        }
        RakenneModuuli rakenne = suoritustapa.getRakenne();
        if (rakenne != null) {
            versiot = rakenneRepository.getRevisions(rakenne.getId());
        }

        return versiot;
    }

    @Transactional(readOnly = true)
    private RakenneModuuli haeRakenneVersio(Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId) {
        RakenneModuuli rakenneVersio = null;
        Peruste peruste = perusteet.findOne(id);
        if (peruste == null) {
            throw new EntityNotFoundException("Perustetta ei löytynyt id:llä: " + id);
        }
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        if (suoritustapa == null) {
            throw new EntityNotFoundException("Suoritustapaa " + suoritustapakoodi.toString() + " ei löytynyt");
        }
        RakenneModuuli rakenne = suoritustapa.getRakenne();
        if (rakenne != null) {
            rakenneVersio = rakenneRepository.findRevision(rakenne.getId(), versioId);
        }
        return rakenneVersio;
    }

    @Override
    @Transactional(readOnly = true)
    public RakenneModuuliDto getRakenneVersio(Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId) {
        return mapper.map(haeRakenneVersio(id, suoritustapakoodi, versioId), RakenneModuuliDto.class);
    }

    @Transactional(readOnly = true)
    private void haeTutkinnonOsaViitteetRakenteesta(AbstractRakenneOsa rakenne, Set<TutkinnonOsaViite> tovat) {
        if (rakenne instanceof RakenneModuuli) {
            for (AbstractRakenneOsa lapsi : ((RakenneModuuli) rakenne).getOsat()) {
                haeTutkinnonOsaViitteetRakenteesta(lapsi, tovat);
            }
        } else if (rakenne instanceof RakenneOsa) {
            TutkinnonOsaViite viite = ((RakenneOsa) rakenne).getTutkinnonOsaViite();
            tovat.add(viite);
        }
    }

    @Override
    @Transactional
    public RakenneModuuliDto revertRakenneVersio(Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId) {
        RakenneModuuli rakenneVersio = haeRakenneVersio(id, suoritustapakoodi, versioId);
        Peruste peruste = perusteet.findOne(id);
        Set<TutkinnonOsaViite> rakenneTovat = new HashSet<>();
        haeTutkinnonOsaViitteetRakenteesta(rakenneVersio, rakenneTovat);

        Map<Long, TutkinnonOsaViite> tovat = new HashMap<>();
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        Set<TutkinnonOsaViite> tutkinnonOsat = suoritustapa.getTutkinnonOsat();
        for (TutkinnonOsaViite tov : tutkinnonOsat) {
            tovat.put(tov.getId(), tov);
        }

        for (TutkinnonOsaViite tov : rakenneTovat) {
            TutkinnonOsaViite utov = tovat.get(tov.getId());
            utov.setPoistettu(false);
        }
        return updateTutkinnonRakenne(id, suoritustapakoodi, mapper.map(rakenneVersio, RakenneModuuliDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public TutkinnonOsaViiteDto getTutkinnonOsaViite(Long osaId) {
        TutkinnonOsaViite re = tutkinnonOsaViiteRepository.findOne(osaId);
        return mapper.map(re, TutkinnonOsaViiteDto.class);
    }

    @Override
    public TutkinnonOsaViiteDto updateTutkinnonOsaViite(Long osaId, TutkinnonOsaViiteDto tov) {
        TutkinnonOsaViite to = tutkinnonOsaViiteRepository.findOne(osaId);
        to.setJarjestys(tov.getJarjestys());
        to.setLaajuus(tov.getLaajuus());
        to.setMuokattu(new Date());
        return mapper.map(to, TutkinnonOsaViiteDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutkinnonOsaViiteDto> getTutkinnonOsat(Long perusteid, Suoritustapakoodi suoritustapakoodi) {
        Peruste peruste = perusteet.findOne(perusteid);
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        Set<TutkinnonOsaViite> tutkinnonOsat = suoritustapa.getTutkinnonOsat();
        return mapper.mapAsList(suoritustapa.getTutkinnonOsat(), TutkinnonOsaViiteDto.class);
    }

    @Value("${fi.vm.sade.eperusteet.tutkinnonrakenne.maksimisyvyys}")
    private int maxRakenneDepth;

    @Override
    @Transactional
    public RakenneModuuliDto updateTutkinnonRakenne(Long perusteId, Suoritustapakoodi suoritustapakoodi, UpdateDto<RakenneModuuliDto> rakenne) {
        RakenneModuuliDto updated = updateTutkinnonRakenne(perusteId, suoritustapakoodi, rakenne.getDto());
        if ( rakenne.getMetadata() != null ) {
            perusteet.setRevisioKommentti(rakenne.getMetadata().getKommentti());
        }
        return updated;
    }

    @Override
    @Transactional
    public RakenneModuuliDto updateTutkinnonRakenne(Long perusteId, Suoritustapakoodi suoritustapakoodi, RakenneModuuliDto rakenne) {

        Suoritustapa suoritustapa = getSuoritustapaEntity(perusteId, suoritustapakoodi);
        lockManager.ensureLockedByAuthenticatedUser(suoritustapa.getId());

        rakenne.foreach(new VisitorImpl(maxRakenneDepth));
        RakenneModuuli moduuli = mapper.map(rakenne, RakenneModuuli.class);

        if (!moduuli.isSame(suoritustapa.getRakenne())) {
            RakenneModuuli current = suoritustapa.getRakenne();
            if (current != null) {
                current.mergeState(moduuli);
            } else {
                current = moduuli;
                suoritustapa.setRakenne(current);
            }
            em.persist(current);
        }

        return mapper.map(moduuli, RakenneModuuliDto.class);
    }

    @Override
    @Transactional
    public void removeTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi, Long osaId) {
        Suoritustapa suoritustapa = getSuoritustapaEntity(id, suoritustapakoodi);
        //varmistetaan että rakenteen muokkaus ei ole käynnissä.
        lockManager.lock(suoritustapa.getId());
        //workaround jolla estetään versiointiongelmat yhtäaikaisten muokkausten tapauksessa.
        em.refresh(suoritustapa, LockModeType.PESSIMISTIC_WRITE);
        try {
            Set<TutkinnonOsaViite> tutkinnonOsat = suoritustapa.getTutkinnonOsat();
            TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(osaId);
            viite.setPoistettu(true);
//            tutkinnonOsat.remove(viite);
        } finally {
            lockManager.unlock(suoritustapa.getId());
        }
    }

    @Override
    @Transactional
    public TutkinnonOsaViiteDto attachTutkinnonOsa(Long id, Suoritustapakoodi koodi, TutkinnonOsaViiteDto osa) {
        //XXX
        return addTutkinnonOsa(id, koodi, osa);
    }

    @Override
    @Transactional
    public TutkinnonOsaViiteDto addTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi, TutkinnonOsaViiteDto osa) {
        final Suoritustapa suoritustapa = getSuoritustapaEntity(id, suoritustapakoodi);
        //workaround jolla estetään versiointiongelmat yhtäaikaisten muokkausten tapauksessa.
        em.refresh(suoritustapa, LockModeType.PESSIMISTIC_WRITE);

        TutkinnonOsaViite viite = mapper.map(osa, TutkinnonOsaViite.class);
        if (viite.getTutkinnonOsa() == null) {
            TutkinnonOsa tutkinnonOsa = new TutkinnonOsa();
            tutkinnonOsa.setTila(Tila.LUONNOS);
            tutkinnonOsa = perusteenOsaRepository.save(tutkinnonOsa);
            viite.setTutkinnonOsa(tutkinnonOsa);
        }
        viite.setSuoritustapa(suoritustapa);
        viite.setMuokattu(new Date());
        if (suoritustapa.getTutkinnonOsat().add(viite)) {
            viite = tutkinnonOsaViiteRepository.save(viite);
        } else {
            throw new BusinessRuleViolationException("Viite tutkinnon osaan on jo olemassa");
        }
        return mapper.map(viite, TutkinnonOsaViiteDto.class);
    }

    @Override
    @Transactional
    public TutkinnonOsaViiteDto updateTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi, TutkinnonOsaViiteDto osa) {
        final Suoritustapa suoritustapa = getSuoritustapaEntity(id, suoritustapakoodi);
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(osa.getId());

        if (viite == null || !viite.getSuoritustapa().equals(suoritustapa)
                || !viite.getTutkinnonOsa().getReference().equals(osa.getTutkinnonOsa())) {
            throw new BusinessRuleViolationException("Virheellinen viite");
        }
        viite.setJarjestys(osa.getJarjestys());
        viite.setLaajuus(osa.getLaajuus());
        viite.setMuokattu(new Date());
        return mapper.map(viite, TutkinnonOsaViiteDto.class);
    }

    private Suoritustapa getSuoritustapaEntity(Long perusteid, Suoritustapakoodi suoritustapakoodi) {
        final Peruste peruste = perusteet.findOne(perusteid);
        if (peruste == null) {
            throw new BusinessRuleViolationException("Perustetta ei ole olemassa");
        }
        final Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        if (suoritustapa == null) {
            throw new BusinessRuleViolationException("Perusteella " + peruste + " + ei ole suoritustapaa "
                    + suoritustapa);
        }
        return suoritustapa;
    }

    @Override
    @Transactional
    public PerusteenSisaltoViiteDto addSisalto(Long perusteId, Suoritustapakoodi suoritustapakoodi, PerusteenSisaltoViiteDto viite) {
        Suoritustapa suoritustapa = getSuoritustapaEntity(perusteId, suoritustapakoodi);
        if (suoritustapa.getSisalto() == null) {
            throw new BusinessRuleViolationException("Perusteen " + perusteId + " + suoritustavalla "
                    + suoritustapakoodi
                    + " ei ole sisältöä");
        }

        PerusteenOsaViite uusiViite = new PerusteenOsaViite();
        if (viite == null) {
            TekstiKappale uusiKappale = new TekstiKappale();
            uusiKappale.setTila(Tila.LUONNOS);
            em.persist(uusiKappale);
            uusiViite.setPerusteenOsa(uusiKappale);
        } else {
            PerusteenOsaViite viiteEntity = mapper.map(viite, PerusteenOsaViite.class);
            uusiViite.setLapset(viiteEntity.getLapset());
            uusiViite.setPerusteenOsa(viiteEntity.getPerusteenOsa());
        }

        em.refresh(suoritustapa, LockModeType.PESSIMISTIC_WRITE);
        final PerusteenOsaViite sisalto = suoritustapa.getSisalto();
        uusiViite.setVanhempi(sisalto);
        sisalto.getLapset().add(uusiViite);
        em.persist(uusiViite);
        return mapper.map(uusiViite, PerusteenSisaltoViiteDto.class);
    }

    @Override
    @Transactional
    public PerusteenSisaltoViiteDto addSisaltoLapsi(Long perusteId, Long perusteenosaViiteId) {

        PerusteenOsaViite viiteEntity = perusteenOsaViiteRepo.findOne(perusteenosaViiteId);
        if (viiteEntity == null) {
            throw new BusinessRuleViolationException("Perusteenosaviitettä ei ole olemassa");
        }
        perusteenOsaViiteRepo.lock(viiteEntity);
        PerusteenOsaViite uusiViite = new PerusteenOsaViite();
        TekstiKappale uusiKappale = new TekstiKappale();
        uusiKappale.setTila(Tila.LUONNOS);
        em.persist(uusiKappale);
        uusiViite.setPerusteenOsa(uusiKappale);
        uusiViite.setVanhempi(viiteEntity);
        em.persist(uusiViite);
        viiteEntity.getLapset().add(uusiViite);

        return mapper.map(uusiViite, PerusteenSisaltoViiteDto.class);
    }

    @Override
    @Transactional
    public PerusteenSisaltoViiteDto attachSisaltoLapsi(Long perusteId, Long parentViiteId, Long tekstikappaleId) {
        PerusteenOsaViite viiteEntity = perusteenOsaViiteRepo.findOne(parentViiteId);
        if (viiteEntity == null) {
            throw new BusinessRuleViolationException("Perusteenosaviitettä ei ole olemassa");
        }
        perusteenOsaViiteRepo.lock(viiteEntity);
        PerusteenOsaViite uusiViite = new PerusteenOsaViite();
        PerusteenOsa kappale = perusteenOsaRepository.findOne(tekstikappaleId);
        uusiViite.setPerusteenOsa(kappale);
        uusiViite.setVanhempi(viiteEntity);
        em.persist(uusiViite);
        viiteEntity.getLapset().add(uusiViite);

        return mapper.map(uusiViite, PerusteenSisaltoViiteDto.class);
    }

    @Override
    public LukkoDto lock(Long id, Suoritustapakoodi suoritustapakoodi) {
        Suoritustapa suoritustapa = getSuoritustapaEntity(id, suoritustapakoodi);
        return LukkoDto.of(lockManager.lock(suoritustapa.getId()));
    }

    @Override
    public void unlock(Long id, Suoritustapakoodi suoritustapakoodi) {
        Suoritustapa suoritustapa = getSuoritustapaEntity(id, suoritustapakoodi);
        lockManager.unlock(suoritustapa.getId());
    }

    @Override
    public LukkoDto getLock(Long id, Suoritustapakoodi suoritustapakoodi) {
        Suoritustapa suoritustapa = getSuoritustapaEntity(id, suoritustapakoodi);
        return LukkoDto.of(lockManager.getLock(suoritustapa.getId()));
    }

    /**
     * Lämmittää tyhjään järjestelmään koodistosta löytyvät koulutukset.
     *
     * @return
     */
    @Override
    @Transactional
    public String lammitys() {

        RestTemplate restTemplate = new RestTemplate();
        List<Peruste> perusteEntityt = new ArrayList<>();
        KoodistoKoodiDto[] tutkinnot;
        Map<String, String> erikoistapausMap = alustaErikoistapausMap();

        int i = 0;
        for (String koulutustyyppiUri : KOULUTUSTYYPPI_URIT) {
            tutkinnot = restTemplate.getForObject(KOODISTO_REST_URL + KOODISTO_RELAATIO_YLA + koulutustyyppiUri, KoodistoKoodiDto[].class);
            Peruste peruste;

            for (KoodistoKoodiDto tutkinto : tutkinnot) {
                if (tutkinto.getKoodisto().getKoodistoUri().equals("koulutus")
                        && (koulutusRepo.findOneByKoulutuskoodi(tutkinto.getKoodiUri()) == null)) {
                    // Haetaan erikoistapausperusteet, jotka kuvaavat kahden eri koulutusalan tutkinnot
                    peruste = haeErikoistapaus(tutkinto.getKoodiUri(), perusteEntityt, erikoistapausMap);
                    if (peruste == null) {
                        peruste = koodistoMapper.map(tutkinto, Peruste.class);
                        peruste.setKoulutustyyppi(koulutustyyppiUri);
                        peruste.setVoimassaoloAlkaa(new GregorianCalendar(3000, 0, 1).getTime());
                        peruste.setKoulutukset(new HashSet<Koulutus>());
                        peruste.setSuoritustavat(luoSuoritustavat(koulutustyyppiUri, null));
                        peruste.setTila(Tila.VALMIS);
                    }
                    peruste.getKoulutukset().add(luoKoulutus(tutkinto));

                    if (!perusteEntityt.contains(peruste)) {
                        perusteEntityt.add(peruste);
                    }
                    LOG.info(++i + " perustetta lisätty.");
                }
            }
        }
        perusteet.save(perusteEntityt);
        return "Perusteet tallennettu";
    }

    private Peruste haeErikoistapaus(String koodiUri, List<Peruste> perusteEntityt, Map<String, String> erikoistapausMap) {
        Peruste peruste = null;
        boolean perusteFound = false;
        if (ERIKOISTAPAUKSET.contains(koodiUri)) {
            for (Peruste perusteEntity : perusteEntityt) {
                for (Koulutus koulutus : perusteEntity.getKoulutukset()) {
                    if (koulutus.getKoulutuskoodi().equals(erikoistapausMap.get(koodiUri))) {
                        peruste = perusteEntity;
                        perusteFound = true;
                        break;
                    }
                }
                if (perusteFound) {
                    break;
                }
            }
        }
        return peruste;
    }

    /**
     *
     * @param koodiUri luotavan koulutuksen koodisto koodiUri
     * @return luotu koulutus entity
     */
    private Koulutus luoKoulutus(KoodistoKoodiDto tutkinto) {
        Koulutus koulutus = koodistoMapper.map(tutkinto, Koulutus.class);

        KoodistoKoodiDto[] koulutusAlarelaatiot;
        RestTemplate restTemplate = new RestTemplate();

        koulutus.setKoulutuskoodi(tutkinto.getKoodiUri());
        // Haetaan joka tutkinnolle alarelaatiot ja lisätään tarvittavat tiedot koulutus entityyn
        koulutusAlarelaatiot = restTemplate.getForObject(KOODISTO_REST_URL + KOODISTO_RELAATIO_ALA + "/"
                + tutkinto.getKoodiUri(), KoodistoKoodiDto[].class);
        koulutus.setKoulutusalakoodi(parseAlarelaatiokoodi(koulutusAlarelaatiot, KOULUTUSALALUOKITUS));
        koulutus.setOpintoalakoodi(parseAlarelaatiokoodi(koulutusAlarelaatiot, OPINTOALALUOKITUS));
        return koulutus;
    }

    private String parseAlarelaatiokoodi(KoodistoKoodiDto[] koulutusAlarelaatiot, String relaatio) {
        String koulutusAlarelaatiokoodi = null;
        for (KoodistoKoodiDto koulutusAlarelaatio : koulutusAlarelaatiot) {
            if (koulutusAlarelaatio.getKoodisto().getKoodistoUri().equals(relaatio)) {
                koulutusAlarelaatiokoodi = koulutusAlarelaatio.getKoodiUri();
                break;
            }
        }
        return koulutusAlarelaatiokoodi;
    }

    private Map<String, String> alustaErikoistapausMap() {
        Map<String, String> erikoistapausMap = new HashMap<>();

        for (int i = 0; i < ERIKOISTAPAUKSET.size(); i++) {
            String vastaarvo = i % 2 == 0 ? ERIKOISTAPAUKSET.get(i + 1) : ERIKOISTAPAUKSET.get(i - 1);
            erikoistapausMap.put(ERIKOISTAPAUKSET.get(i), vastaarvo);
        }
        return erikoistapausMap;

    }

    /**
     *
     * @param koulutustyyppiUri
     * @return palauttaa mahdolliset tutkinnon suoritustavat
     */
    private Set<Suoritustapa> luoSuoritustavat(String koulutustyyppiUri, LaajuusYksikko yksikko) {
        Set<Suoritustapa> suoritustavat = new HashSet<>();
        suoritustavat.add(suoritustapaService.createSuoritustapaWithSisaltoAndRakenneRoots(Suoritustapakoodi.NAYTTO, null));
        if (koulutustyyppiUri.equals(KOULUTUSTYYPPI_URIT[0])) {
            suoritustavat.add(suoritustapaService.createSuoritustapaWithSisaltoAndRakenneRoots(Suoritustapakoodi.OPS, yksikko));
        }
        return suoritustavat;
    }

    /**
     * Luo uuden perusteen perusrakenteella.
     *
     * @param koulutustyyppi
     * @param yksikko
     * @param tila
     * @return Palauttaa 'tyhjän' perusterungon
     */
    @Override
    // FIXME: Luo vain ammatillisen puolen perusteita. Refactoroi, kun tulee lisää koulutustyyppejä.
    public Peruste luoPerusteRunko(String koulutustyyppi, LaajuusYksikko yksikko, Tila tila) {
        Peruste peruste = new Peruste();
        peruste.setKoulutustyyppi(koulutustyyppi);
        peruste.setTila(tila);
        Set<Suoritustapa> suoritustavat = new HashSet<>();
        suoritustavat.add(suoritustapaService.createSuoritustapaWithSisaltoAndRakenneRoots(Suoritustapakoodi.NAYTTO, null));
        if (koulutustyyppi != null && koulutustyyppi.equals(KOULUTUSTYYPPI_URIT[0])) {
            suoritustavat.add(suoritustapaService.createSuoritustapaWithSisaltoAndRakenneRoots(Suoritustapakoodi.OPS, yksikko != null ? yksikko : LaajuusYksikko.OSAAMISPISTE));
        }
        peruste.setSuoritustavat(suoritustavat);
        return peruste;
    }

    @Override
    public Peruste luoPerusteRunkoToisestaPerusteesta(Long perusteId) {
        Peruste vanha = perusteet.getOne(perusteId);
        Peruste peruste = new Peruste();
        peruste.setTila(Tila.LUONNOS);
        peruste.setKuvaus(vanha.getKuvaus());
        peruste.setNimi(vanha.getNimi());
        peruste.setKoulutustyyppi(vanha.getKoulutustyyppi());

        Set<Koulutus> vanhatKoulutukset = vanha.getKoulutukset();
        Set<Koulutus> koulutukset = new HashSet<>();
        for (Koulutus vanhaKoulutus : vanhatKoulutukset) {
            koulutukset.add(vanhaKoulutus);
        }
        peruste.setKoulutukset(koulutukset);

        Set<Suoritustapa> suoritustavat = vanha.getSuoritustavat();
        Set<Suoritustapa> uudetSuoritustavat = new HashSet<>();

        for (Suoritustapa st : suoritustavat) {
            uudetSuoritustavat.add(suoritustapaService.createFromOther(st.getId()));
        }

        peruste.setSuoritustavat(uudetSuoritustavat);

        peruste = perusteet.save(peruste);
        return peruste;
    }

    private static class VisitorImpl implements AbstractRakenneOsaDto.Visitor {

        private final int maxDepth;

        public VisitorImpl(int maxDepth) {
            this.maxDepth = maxDepth;
        }

        @Override
        public void visit(final AbstractRakenneOsaDto dto, final int depth) {
            if (depth >= maxDepth) {
                throw new BusinessRuleViolationException("Tutkinnon rakennehierarkia ylittää maksimisyvyyden");
            }
        }
    }

}
