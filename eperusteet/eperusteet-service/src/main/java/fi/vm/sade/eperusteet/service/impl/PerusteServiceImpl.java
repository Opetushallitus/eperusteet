package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Koulutusala;
import fi.vm.sade.eperusteet.domain.Opintoala;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.dto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.PerusteQuery;
import fi.vm.sade.eperusteet.dto.PageDto;
import fi.vm.sade.eperusteet.dto.PerusteDto;
import fi.vm.sade.eperusteet.repository.KoulutusalaRepository;
import fi.vm.sade.eperusteet.repository.OpintoalaRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;
import java.util.ArrayList;
import java.util.List;
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
    
    private static final String KOODISTO_REST_URL = "https://virkailija.opintopolku.fi/koodisto-service/rest/json";
    private static final String KOODISTO_RELAATIO_YLA = "/relaatio/sisaltyy-ylakoodit";
    private static final String KOODISTO_RELAATIO_ALA = "/relaatio/sisaltyy-alakoodit";
    private static final String KOULUTUSTYYPPI_PERUSKOULUTUS_URI = "/koulutustyyppi_1";
    private static final String KOULUTUSALA_KOODISTOURI = "/koulutusalaoph2002";
    private static final String OPINTOALA_KOODISTOURI = "/opintoalaoph2002";

    @Autowired
    PerusteRepository perusteet;
    
    @Autowired
    KoulutusalaRepository koulutusalatRepo;
    
    @Autowired
    OpintoalaRepository opintoalatRepo;

    @Autowired
    PerusteenOsaViiteRepository viitteet;

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
    @Transactional
    public String lammitys() {
        
        RestTemplate restTemplate = new RestTemplate();
        
        //------------ KOULUTUSALAT ------------------------------//
        KoodistoKoodiDto[] koulutusalat = restTemplate.getForObject(KOODISTO_REST_URL + KOULUTUSALA_KOODISTOURI + "/koodi", KoodistoKoodiDto[].class);
        Koulutusala koulutusalaEntity;
        for (KoodistoKoodiDto koulutusala : koulutusalat) {
            koulutusalaEntity = new Koulutusala();
            koulutusalaEntity.setKoodi(koulutusala.getKoodiUri());
            if (koulutusalatRepo.findWithKoodi(KOODISTO_REST_URL).size() < 1) {
                koulutusalatRepo.save(koulutusalaEntity);
            }
        }
        
        //------------ OPINTOALAT ------------------------------//
        KoodistoKoodiDto[] opintoalat = restTemplate.getForObject(KOODISTO_REST_URL + OPINTOALA_KOODISTOURI + "/koodi", KoodistoKoodiDto[].class);
        Opintoala opintoalaEntity;
        for (KoodistoKoodiDto opintoala : opintoalat) {
            opintoalaEntity = new Opintoala();
            opintoalaEntity.setKoodi(opintoala.getKoodiUri());
            if (opintoalatRepo.findWithKoodi(opintoala.getKoodiUri()).size() < 1) {
                opintoalatRepo.save(opintoalaEntity);
            }
        }
        
        //------------ PERUSTEET ---------------------------------//
        List<Peruste> perusteEntityt = new ArrayList<>();
        KoodistoKoodiDto[] tutkinnot = restTemplate.getForObject(KOODISTO_REST_URL + KOODISTO_RELAATIO_YLA + KOULUTUSTYYPPI_PERUSKOULUTUS_URI, KoodistoKoodiDto[].class);
        System.out.println("Tutkinnot koko: " + tutkinnot.length);
        
        Peruste peruste;
        KoodistoKoodiDto[] koulutusAlakoodit;
        for (KoodistoKoodiDto tutkinto : tutkinnot) {
                
            if (tutkinto.getKoodisto().getKoodistoUri().equals("koulutus")) {
                peruste = koodistoMapper.map(tutkinto, Peruste.class);
                
                koulutusAlakoodit = restTemplate.getForObject(KOODISTO_REST_URL + KOODISTO_RELAATIO_ALA + "/" +tutkinto.getKoodiUri(), KoodistoKoodiDto[].class);
                peruste.setTutkintokoodi(parseTutkintotyyppi(koulutusAlakoodit));
                peruste.setKoulutusala(parseKoulutusala(koulutusAlakoodit));
                peruste.setOpintoalat(parseOpintoalat(koulutusAlakoodit));
                
                perusteEntityt.add(peruste);     
            }
        }

        perusteet.save(perusteEntityt);
        
        return "Perusteet tallennettu";

    }

    private String parseTutkintotyyppi(KoodistoKoodiDto[] koulutusAlakoodit) {
        String tutkintotyyppi = "";
        for (KoodistoKoodiDto koulutusAlakoodi : koulutusAlakoodit) {    
            if (koulutusAlakoodi.getKoodisto().getKoodistoUri().equals("tutkintotyyppi")) {
                tutkintotyyppi = koulutusAlakoodi.getKoodiUri();
                break;
            }
        }
        return tutkintotyyppi;
    }

    private Koulutusala parseKoulutusala(KoodistoKoodiDto[] koulutusAlakoodit) {
        Koulutusala koulutusala = null;
        for (KoodistoKoodiDto koulutusAlakoodi : koulutusAlakoodit) {    
            if (koulutusAlakoodi.getKoodisto().getKoodistoUri().equals("koulutusalaoph2002")) {
                //koulutusala.setKoodi(koulutusAlakoodi.getKoodiUri());
                List<Koulutusala> alat = koulutusalatRepo.findWithKoodi(koulutusAlakoodi.getKoodiUri());
                if (alat != null && alat.size() > 0) {
                    koulutusala = alat.get(0);
                } else {
                    koulutusala = null;
                }
                break;
            }
        }   
        return koulutusala;
    }

    private List<Opintoala> parseOpintoalat(KoodistoKoodiDto[] koulutusAlakoodit) {
        List<Opintoala> opintoalat = new ArrayList<>();
        Opintoala opintoala;
        for (KoodistoKoodiDto koulutusAlakoodi : koulutusAlakoodit) {    
            if (koulutusAlakoodi.getKoodisto().getKoodistoUri().equals("opintoalaoph2002")) {
                
                List<Opintoala> alat = opintoalatRepo.findWithKoodi(koulutusAlakoodi.getKoodiUri());
                if (alat != null && alat.size() > 0) {
                    opintoala = alat.get(0);
                    opintoalat.add(opintoala);
                } 

            }
        }
        return opintoalat;    
    }
}
