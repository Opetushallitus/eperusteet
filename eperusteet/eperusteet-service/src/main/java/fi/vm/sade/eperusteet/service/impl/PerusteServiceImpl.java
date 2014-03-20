package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Koulutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.dto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.PageDto;
import fi.vm.sade.eperusteet.dto.PerusteDto;
import fi.vm.sade.eperusteet.dto.PerusteQuery;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.service.KoulutusalaService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
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
    private static final String KOODISTO_REST_URL = "https://virkailija.opintopolku.fi/koodisto-service/rest/json/";
    private static final String KOODISTO_RELAATIO_YLA = "relaatio/sisaltyy-ylakoodit/";
    private static final String KOODISTO_RELAATIO_ALA = "relaatio/sisaltyy-alakoodit/";
    private static final String[] KOULUTUSTYYPPI_URIT = {"koulutustyyppi_1", "koulutustyyppi_11", "koulutustyyppi_12"};
    private static final String KOULUTUSALALUOKITUS = "koulutusalaoph2002";
    private static final String OPINTOALALUOKITUS = "opintoalaoph2002";

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
        List<Peruste> perusteEntityt = new ArrayList<>();
        KoodistoKoodiDto[] tutkinnot;

        int i = 0;
        for (String koulutustyyppiUri : KOULUTUSTYYPPI_URIT) {
            tutkinnot = restTemplate.getForObject(KOODISTO_REST_URL + KOODISTO_RELAATIO_YLA + koulutustyyppiUri, KoodistoKoodiDto[].class);

            Peruste peruste;
            KoodistoKoodiDto[] koulutusAlarelaatiot;
            
            for (KoodistoKoodiDto tutkinto : tutkinnot) {

                if (tutkinto.getKoodisto().getKoodistoUri().equals("koulutus") && (perusteet.findOneByKoodiUri(tutkinto.getKoodiUri()) == null)) {
                    peruste = koodistoMapper.map(tutkinto, Peruste.class);
                    peruste.setTutkintokoodi(koulutustyyppiUri);
                    peruste.setPaivays(new GregorianCalendar(3000, 0, 1).getTime());
                    peruste.setKoulutukset(new HashSet<Koulutus>());
                    Koulutus koulutus = new Koulutus();
                    koulutus.setKoulutuskoodi(tutkinto.getKoodiUri());
                    // Haetaan joka tutkinnolle alarelaatiot ja lisätään tarvittavat tiedot peruste entityyn
                    koulutusAlarelaatiot = restTemplate.getForObject(KOODISTO_REST_URL + KOODISTO_RELAATIO_ALA + "/" + tutkinto.getKoodiUri(), KoodistoKoodiDto[].class);
                    koulutus.setKoulutusalakoodi(parseAlarelaatiokoodi(koulutusAlarelaatiot, KOULUTUSALALUOKITUS));
                    koulutus.setOpintoalakoodi(parseAlarelaatiokoodi(koulutusAlarelaatiot, OPINTOALALUOKITUS));
                    peruste.getKoulutukset().add(koulutus);

                    perusteEntityt.add(peruste);

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
}