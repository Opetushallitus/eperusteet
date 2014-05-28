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

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import fi.vm.sade.eperusteet.domain.Koulutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.Tila;
import fi.vm.sade.eperusteet.domain.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.EntityReference;
import fi.vm.sade.eperusteet.dto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.PageDto;
import fi.vm.sade.eperusteet.dto.PerusteDto;
import fi.vm.sade.eperusteet.dto.PerusteQuery;
import fi.vm.sade.eperusteet.dto.PerusteenSisaltoViiteDto;
import fi.vm.sade.eperusteet.dto.PerusteenosaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.repository.KoulutusRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
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

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteDto> getAll(PageRequest page, String kieli) {
        return findBy(page, new PerusteQuery());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteDto> findBy(PageRequest page, PerusteQuery pquery) {
        Page<Peruste> result = perusteet.findBy(page, pquery);
        return new PageDto<>(result, PerusteDto.class, page, mapper);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteDto get(final Long id) {
        Peruste p = perusteet.findById(id);
        return mapper.map(p, PerusteDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteDto getByIdAndSuoritustapa(final Long id, Suoritustapakoodi suoritustapakoodi) {
        Peruste p = perusteet.findPerusteByIdAndSuoritustapakoodi(id, suoritustapakoodi);
        return mapper.map(p, PerusteDto.class);
    }

    @Override
    public PerusteDto update(long id, PerusteDto perusteDto) {
        if (!perusteet.exists(id)) {
            throw new EntityNotFoundException("Objektia ei löytynyt id:llä: " + id);
        }

        Peruste perusteVanha = perusteet.findById(id);

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
    public PerusteenosaViiteDto getSuoritustapaSisalto(Long perusteId, Suoritustapakoodi suoritustapakoodi) {
        PerusteenOsaViite entity = perusteet.findSisaltoByIdAndSuoritustapakoodi(perusteId, suoritustapakoodi);
        return mapper.map(entity, PerusteenosaViiteDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public RakenneModuuliDto getTutkinnonRakenne(Long perusteid, Suoritustapakoodi suoritustapakoodi) {
        Peruste peruste = perusteet.findOne(perusteid);
        LOG.debug(suoritustapakoodi.toString());
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        RakenneModuuli rakenne = suoritustapa.getRakenne();
        if (rakenne == null) {
            rakenne = new RakenneModuuli();
            rakenne.setNimi(peruste.getNimi());
        }
        return mapper.map(rakenne, RakenneModuuliDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutkinnonOsaViiteDto> getTutkinnonOsat(Long perusteid, Suoritustapakoodi suoritustapakoodi) {
        Peruste peruste = perusteet.findOne(perusteid);
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        return mapper.mapAsList(suoritustapa.getTutkinnonOsat(), TutkinnonOsaViiteDto.class);
    }

    @Value("${fi.vm.sade.eperusteet.tutkinnonrakenne.maksimisyvyys}")
    private int maxRakenneDepth;

    @Override
    @Transactional
    public RakenneModuuliDto updateTutkinnonRakenne(Long perusteId, Suoritustapakoodi suoritustapakoodi, RakenneModuuliDto rakenne) {

        Suoritustapa suoritustapa = getSuoritustapa(perusteId, suoritustapakoodi);
        lockManager.ensureLockedByAuthenticatedUser(suoritustapa.getId());

        final Map<EntityReference, TutkinnonOsaViite> uniqueIndex = Maps.uniqueIndex(suoritustapa.getTutkinnonOsat(), IndexFunction.INSTANCE);
        rakenne.foreach(new VisitorImpl(uniqueIndex, maxRakenneDepth));
        RakenneModuuli moduuli = mapper.map(rakenne, RakenneModuuli.class);

        if (!moduuli.equals(suoritustapa.getRakenne())) {
            em.persist(moduuli);
            if (suoritustapa.getRakenne() != null) {
                em.remove(suoritustapa.getRakenne());
            }
            suoritustapa.setRakenne(moduuli);
        }
        return mapper.map(moduuli, RakenneModuuliDto.class);
    }

    @Override
    @Transactional
    public void removeTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi, Long osaId) {
        Suoritustapa suoritustapa = getSuoritustapa(id, suoritustapakoodi);
        lockManager.lock(suoritustapa.getId());
        try {
            Set<TutkinnonOsaViite> tutkinnonOsat = suoritustapa.getTutkinnonOsat();
            TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(osaId);
            tutkinnonOsat.remove(viite);
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
        final Suoritustapa suoritustapa = getSuoritustapa(id, suoritustapakoodi);
        TutkinnonOsaViite viite = mapper.map(osa, TutkinnonOsaViite.class);
        if (viite.getTutkinnonOsa() == null) {
            TutkinnonOsa tutkinnonOsa = new TutkinnonOsa();
            tutkinnonOsa.setTila(Tila.LUONNOS);
            tutkinnonOsa = perusteenOsaRepository.save(tutkinnonOsa);
            viite.setTutkinnonOsa(tutkinnonOsa);
        }
        viite.setSuoritustapa(suoritustapa);
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
        final Suoritustapa suoritustapa = getSuoritustapa(id, suoritustapakoodi);
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(osa.getId());

        if (viite == null || !viite.getSuoritustapa().equals(suoritustapa)
            || !viite.getTutkinnonOsa().getReference().equals(osa.getTutkinnonOsa())) {
            throw new BusinessRuleViolationException("Virheellinen viite");
        }
        viite.setJarjestys(osa.getJarjestys());
        viite.setLaajuus(osa.getLaajuus());
        viite.setYksikko(osa.getYksikko());
        return mapper.map(viite, TutkinnonOsaViiteDto.class);
    }

    private Suoritustapa getSuoritustapa(Long perusteid, Suoritustapakoodi suoritustapakoodi) {
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
        Suoritustapa suoritustapa = getSuoritustapa(perusteId, suoritustapakoodi);
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
        PerusteenOsaViite uusiViite = new PerusteenOsaViite();

        PerusteenOsaViite viiteEntity = perusteenOsaViiteRepo.findOne(perusteenosaViiteId);
        if (viiteEntity == null) {
            throw new BusinessRuleViolationException("Perusteenosaviitettä ei ole olemassa");
        }

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
    public LukkoDto lock(Long id, Suoritustapakoodi suoritustapakoodi) {
        Suoritustapa suoritustapa = getSuoritustapa(id, suoritustapakoodi);
        return lockManager.lock(suoritustapa.getId());
    }

    @Override
    public void unlock(Long id, Suoritustapakoodi suoritustapakoodi) {
        Suoritustapa suoritustapa = getSuoritustapa(id, suoritustapakoodi);
        lockManager.unlock(suoritustapa.getId());
    }

    @Override
    public LukkoDto getLock(Long id, Suoritustapakoodi suoritustapakoodi) {
        Suoritustapa suoritustapa = getSuoritustapa(id, suoritustapakoodi);
        return lockManager.getLock(suoritustapa.getId());
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
                        peruste.setTutkintokoodi(koulutustyyppiUri);
                        peruste.setVoimassaoloAlkaa(new GregorianCalendar(3000, 0, 1).getTime());
                        peruste.setKoulutukset(new HashSet<Koulutus>());
                        peruste.setSuoritustavat(luoSuoritustavat(koulutustyyppiUri));
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
    private Set<Suoritustapa> luoSuoritustavat(String koulutustyyppiUri) {
        Set<Suoritustapa> suoritustavat = new HashSet<>();
        suoritustavat.add(suoritustapaService.createSuoritustapaWithSisaltoRoot(Suoritustapakoodi.NAYTTO));
        if (koulutustyyppiUri.equals(KOULUTUSTYYPPI_URIT[0])) {
            suoritustavat.add(suoritustapaService.createSuoritustapaWithSisaltoRoot(Suoritustapakoodi.OPS));
        }
        return suoritustavat;
    }

    /**
     * Luo uuden perusteen perusrakenteella.
     *
     * @param koulutustyyppi
     * @return Palauttaa 'tyhjän' perusterungon
     */
    @Override
    // HUOM!: Luo vain ammatillisen puolen perusteita. Refactoroi, kun tulee lisää koulutustyyppejä.
    public Peruste luoPerusteRunko(String koulutustyyppi) {
        Peruste peruste = new Peruste();

        peruste.setTutkintokoodi(koulutustyyppi);
        peruste.setTila(Tila.LUONNOS);
        Set<Suoritustapa> suoritustavat = new HashSet<>();
        suoritustavat.add(suoritustapaService.createSuoritustapaWithSisaltoRoot(Suoritustapakoodi.NAYTTO));
        if (koulutustyyppi != null && koulutustyyppi.equals(KOULUTUSTYYPPI_URIT[0])) {
            suoritustavat.add(suoritustapaService.createSuoritustapaWithSisaltoRoot(Suoritustapakoodi.OPS));
        }
        peruste.setSuoritustavat(suoritustavat);

        return peruste;
    }

    private enum IndexFunction implements Function<TutkinnonOsaViite, EntityReference> {

        INSTANCE;

        @Override
        public EntityReference apply(TutkinnonOsaViite input) {
            return input.getTutkinnonOsa().getReference();
        }
    }

    private static class VisitorImpl implements AbstractRakenneOsaDto.Visitor {

        private final Map<EntityReference, TutkinnonOsaViite> uniqueIndex;
        private int maxDepth;

        public VisitorImpl(Map<EntityReference, TutkinnonOsaViite> uniqueIndex, int maxDepth) {
            this.uniqueIndex = uniqueIndex;
            this.maxDepth = maxDepth;
        }

        @Override
        public void visit(final AbstractRakenneOsaDto dto, final int depth) {
            if (depth >= maxDepth) {
                throw new BusinessRuleViolationException("Tutkinnon rakennehierarkia ylittää maksimisyvyyden");
            }
            if (dto instanceof RakenneOsaDto) {
                RakenneOsaDto r = (RakenneOsaDto) dto;
                r.setTutkinnonOsaViite(uniqueIndex.get(r.getTutkinnonOsa()).getReference());
            }
        }
    }

}
