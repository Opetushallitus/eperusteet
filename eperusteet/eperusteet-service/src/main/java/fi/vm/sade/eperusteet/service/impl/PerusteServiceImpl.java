package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Koulutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.PageDto;
import fi.vm.sade.eperusteet.dto.PerusteDto;
import fi.vm.sade.eperusteet.dto.PerusteQuery;
import fi.vm.sade.eperusteet.dto.PerusteenosaViiteDto;
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
    
    private static final List<String> ERIKOISTAPAUKSET = new ArrayList<>(Arrays.asList(new String[]{"koulutus_357802", "koulutus_327110", "koulutus_354803", "koulutus_324111", "koulutus_354710",
            "koulutus_324125", "koulutus_357709", "koulutus_327124", "koulutus_355904", "koulutus_324129", "koulutus_358903", "koulutus_327127",
            "koulutus_355412", "koulutus_324126", "koulutus_355413", "koulutus_324127", "koulutus_358412", "koulutus_327126", "koulutus_354708",
            "koulutus_324123", "koulutus_357707", "koulutus_327122"}));
  
    @Autowired
    PerusteRepository perusteet;
    @Autowired
    PerusteenOsaViiteRepository viitteet;
    @Autowired
    KoulutusalaService koulutusalaService;
    @Autowired
    @Dto
    private DtoMapper mapper;
    @Autowired
    @Koodisto
    private DtoMapper koodistoMapper;

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
        PerusteenOsaViite v = viitteet.findOne(parentId);
        viite.setVanhempi(v);
        int i = 0;
        if (seuraavaViite != null) {
            for (PerusteenOsaViite o : v.getLapset()) {
                if (o.getId().equals(seuraavaViite)) { 
                   break;
                }
                i++;
            }
        } else {
            v.getLapset().size();
        }
        v.getLapset().add(i, viite);
        return viitteet.save(viite);
    }
    
    @Override
    public PerusteenosaViiteDto getSuoritustapaSisalto(Long perusteId, Suoritustapakoodi suoritustapakoodi) {
        PerusteenOsaViite entity = perusteet.findByIdAndSuoritustapakoodi(perusteId, suoritustapakoodi);
        return mapper.map(entity, PerusteenosaViiteDto.class);
    }

    /**
     * Lämmittää tyhjään järjestelmään koodistosta löytyvät koulutukset.
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
                if (tutkinto.getKoodisto().getKoodistoUri().equals("koulutus") && (perusteet.findOneByKoodiUri(tutkinto.getKoodiUri()) == null)) {                                 
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
    
    private Peruste haeErikoistapaus(String koodiUri, List<Peruste> perusteEntityt, Map<String,String> erikoistapausMap) {
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

    private Map<String, String> alustaErikoistapausMap() {
        Map<String, String> erikoistapausMap = new HashMap<>();
        
        for (int i = 0; i < ERIKOISTAPAUKSET.size(); i++) {
            String vastaarvo = i%2==0 ? ERIKOISTAPAUKSET.get(i+1) : ERIKOISTAPAUKSET.get(i-1);
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

}