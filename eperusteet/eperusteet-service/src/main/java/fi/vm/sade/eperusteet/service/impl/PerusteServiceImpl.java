package fi.vm.sade.eperusteet.service.impl;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import fi.vm.sade.eperusteet.domain.Koulutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.EntityReference;
import fi.vm.sade.eperusteet.dto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.PageDto;
import fi.vm.sade.eperusteet.dto.PerusteDto;
import fi.vm.sade.eperusteet.dto.PerusteQuery;
import fi.vm.sade.eperusteet.dto.PerusteenSisaltoViiteDto;
import fi.vm.sade.eperusteet.dto.PerusteenosaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonRakenneDto;
import fi.vm.sade.eperusteet.repository.KoulutusRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.service.KoulutusalaService;
import fi.vm.sade.eperusteet.service.PerusteService;
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
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    PerusteenOsaViiteRepository perusteenOsaViiteRepo;
    @Autowired
    @Dto
    private DtoMapper mapper;
    @Autowired
    @Koodisto
    private DtoMapper koodistoMapper;
    @PersistenceContext
    private EntityManager em;

    @Override
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
    public PerusteenosaViiteDto getSuoritustapaSisalto(Long perusteId, Suoritustapakoodi suoritustapakoodi) {
        PerusteenOsaViite entity = perusteet.findSisaltoByIdAndSuoritustapakoodi(perusteId, suoritustapakoodi);
        return mapper.map(entity, PerusteenosaViiteDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public TutkinnonRakenneDto getTutkinnonRakenne(Long perusteid, Suoritustapakoodi suoritustapakoodi) {
        Peruste peruste = perusteet.findOne(perusteid);
        LOG.debug(suoritustapakoodi.toString());
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        RakenneModuuli rakenne = suoritustapa.getRakenne();
        if ( rakenne == null ) {
            rakenne = new RakenneModuuli();
            rakenne.setNimi(peruste.getNimi());
        }
        return new TutkinnonRakenneDto(
            mapper.mapAsList(suoritustapa.getTutkinnonOsat(), TutkinnonOsaViiteDto.class),
            mapper.map(rakenne, RakenneModuuliDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutkinnonOsaViiteDto> getTutkinnonOsat(Long perusteid, Suoritustapakoodi suoritustapakoodi) {
        Peruste peruste = perusteet.findOne(perusteid);
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        return mapper.mapAsList(suoritustapa.getTutkinnonOsat(), TutkinnonOsaViiteDto.class);
    }

    @Override
    @Transactional
    public TutkinnonRakenneDto updateTutkinnonRakenne(Long perusteid, Suoritustapakoodi suoritustapakoodi, TutkinnonRakenneDto rakenne) {
        final Peruste peruste = perusteet.findOne(perusteid);
        if (peruste == null) {
            throw new IllegalArgumentException("Perustetta ei ole olemassa");
        }
        final Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        if (suoritustapa == null) {
            throw new IllegalArgumentException("Perusteella " + peruste + " + ei ole suoritustapaa " + suoritustapakoodi);
        }

        Set<TutkinnonOsaViite> osat = new HashSet<>();
        for (TutkinnonOsaViite v : mapper.mapAsList(rakenne.getTutkinnonOsat(), TutkinnonOsaViite.class)) {
            osat.add(v);
        }

        suoritustapa.setTutkinnonOsat(osat);
        for (TutkinnonOsaViite v : suoritustapa.getTutkinnonOsat()) {
            em.persist(v);
        }

        final Map<EntityReference, TutkinnonOsaViite> uniqueIndex = Maps.uniqueIndex(suoritustapa.getTutkinnonOsat(), IndexFunction.INSTANCE);
        rakenne.getRakenne().visit(new VisitorImpl(uniqueIndex));
        RakenneModuuli moduuli = mapper.map(rakenne.getRakenne(), RakenneModuuli.class);

        em.persist(moduuli);

        if (suoritustapa.getRakenne() != null) {
            em.remove(suoritustapa.getRakenne());
        }
        suoritustapa.setRakenne(moduuli);
        return getTutkinnonRakenne(perusteid, suoritustapakoodi);
    }
    
    @Override
    @Transactional
    public PerusteenSisaltoViiteDto addSisalto(Long perusteId, Suoritustapakoodi suoritustapakoodi, PerusteenSisaltoViiteDto viite) {
        PerusteenOsaViite uusiViite = null;
        
        Peruste peruste = perusteet.findOne(perusteId);
        if (peruste == null) {
            throw new IllegalArgumentException("Perustetta ei ole olemassa");
        }
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        if (suoritustapa == null) {
            throw new IllegalArgumentException("Perusteella " + peruste + " + ei ole suoritustapaa " + suoritustapakoodi);
        }
        
        if (suoritustapa.getSisalto() == null) {
            throw new IllegalArgumentException("Perusteen " + peruste + " + suoritustavalla " + suoritustapakoodi + " ei ole sisältöä");
        }
        
        uusiViite = new PerusteenOsaViite();
        
        if (viite == null) {
            TekstiKappale uusiKappale = new TekstiKappale();
            em.persist(uusiKappale);
            uusiViite.setPerusteenOsa(uusiKappale);
        } else {
           PerusteenOsaViite viiteEntity = mapper.map(viite, PerusteenOsaViite.class);
           uusiViite.setLapset(viiteEntity.getLapset());
           uusiViite.setPerusteenOsa(viiteEntity.getPerusteenOsa());   
        }
        uusiViite.setVanhempi(suoritustapa.getSisalto());
        em.persist(uusiViite);
        suoritustapa.getSisalto().getLapset().add(uusiViite); 
                
        return mapper.map(uusiViite, PerusteenSisaltoViiteDto.class);
    }

    @Override
    @Transactional
    public PerusteenSisaltoViiteDto addSisaltoLapsi(Long perusteId, Long perusteenosaViiteId) {
        PerusteenOsaViite uusiViite = new PerusteenOsaViite();
        
        PerusteenOsaViite viiteEntity = perusteenOsaViiteRepo.findOne(perusteenosaViiteId);
        if (viiteEntity == null) {
            throw new IllegalArgumentException("Perusteenosaviitettä ei ole olemassa");
        }

        TekstiKappale uusiKappale = new TekstiKappale();
        em.persist(uusiKappale);
        uusiViite.setPerusteenOsa(uusiKappale);
        uusiViite.setVanhempi(viiteEntity);
        em.persist(uusiViite);
        viiteEntity.getLapset().add(uusiViite);
        
        return mapper.map(uusiViite, PerusteenSisaltoViiteDto.class);
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
                LOG.info("koodiUri: " + tutkinto.getKoodiUri());
                if (tutkinto.getKoodisto().getKoodistoUri().equals("koulutus") && (koulutusRepo.findOneByKoulutuskoodi(tutkinto.getKoodiUri()) == null)) {
                    // Haetaan erikoistapausperusteet, jotka kuvaavat kahden eri koulutusalan tutkinnot
                    peruste = haeErikoistapaus(tutkinto.getKoodiUri(), perusteEntityt, erikoistapausMap);
                    if (peruste == null) {
                        peruste = koodistoMapper.map(tutkinto, Peruste.class);
                        peruste.setTutkintokoodi(koulutustyyppiUri);
                        peruste.setPaivays(new GregorianCalendar(3000, 0, 1).getTime());
                        peruste.setKoulutukset(new HashSet<Koulutus>());
                        peruste.setSuoritustavat(luoSuoritustavat(koulutustyyppiUri));
                    }
                    peruste.getKoulutukset().add(luoKoulutus(tutkinto.getKoodiUri()));

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
        if (ERIKOISTAPAUKSET.contains(koodiUri)) {
            for (Peruste perusteEntity : perusteEntityt) {
                if (perusteEntity.getKoodiUri().equals(erikoistapausMap.get(koodiUri))) {
                    peruste = perusteEntity;
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
    private Koulutus luoKoulutus(String koodiUri) {
        Koulutus koulutus = new Koulutus();
        KoodistoKoodiDto[] koulutusAlarelaatiot;
        RestTemplate restTemplate = new RestTemplate();

        koulutus.setKoulutuskoodi(koodiUri);
        // Haetaan joka tutkinnolle alarelaatiot ja lisätään tarvittavat tiedot peruste entityyn
        koulutusAlarelaatiot = restTemplate.getForObject(KOODISTO_REST_URL + KOODISTO_RELAATIO_ALA + "/" + koodiUri, KoodistoKoodiDto[].class);
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

        Suoritustapa suoritustapa = new Suoritustapa();
        suoritustapa.setSuoritustapakoodi(Suoritustapakoodi.NAYTTO);
        suoritustavat.add(suoritustapa);

        if (koulutustyyppiUri.equals(KOULUTUSTYYPPI_URIT[0])) {
            suoritustapa = new Suoritustapa();
            suoritustapa.setSuoritustapakoodi(Suoritustapakoodi.OPS);
            suoritustavat.add(suoritustapa);
        }
        return suoritustavat;
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

        public VisitorImpl(Map<EntityReference, TutkinnonOsaViite> uniqueIndex) {
            this.uniqueIndex = uniqueIndex;
        }

        @Override
        public void visit(AbstractRakenneOsaDto dto) {
            if (dto instanceof RakenneOsaDto) {
                RakenneOsaDto r = (RakenneOsaDto) dto;
                r.setTutkinnonOsaViite(uniqueIndex.get(r.getTutkinnonOsa()).getReference());
            }
        }
    }

}
