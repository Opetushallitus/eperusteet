package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.KoulutusalaDto;
import fi.vm.sade.eperusteet.dto.OpintoalaDto;
import fi.vm.sade.eperusteet.service.KoulutusalaService;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class KoulutusalaServiceImpl implements KoulutusalaService {

    private static final String KOODISTO_REST_URL = "https://virkailija.opintopolku.fi/koodisto-service/rest/json/";
    private static final String KOODISTO_RELAATIO_ALA = "relaatio/sisaltyy-alakoodit/";
    private static final String KOULUTUSALA_URI = "koulutusalaoph2002";
    //private static final String OPINTOALA_URI = "opintoalaoph2002";

    @Autowired
    @Koodisto
    private DtoMapper mapper;

    @Autowired
    HttpEntity httpEntity;

    @Override
    @Cacheable("koulutusalat")
    public List<KoulutusalaDto> getAll() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<KoodistoKoodiDto[]> response = restTemplate.exchange(KOODISTO_REST_URL + KOULUTUSALA_URI + "/koodi/", HttpMethod.GET, httpEntity, KoodistoKoodiDto[].class);
        List<KoulutusalaDto> koulutusalatDtos = mapper.mapAsList(Arrays.asList(response.getBody()), KoulutusalaDto.class);

        for (KoulutusalaDto koulutusalaDto : koulutusalatDtos) {
            ResponseEntity<KoodistoKoodiDto[]> opintoalat = restTemplate.exchange(KOODISTO_REST_URL + KOODISTO_RELAATIO_ALA + koulutusalaDto.getKoodi(), HttpMethod.GET, httpEntity, KoodistoKoodiDto[].class);
            koulutusalaDto.setOpintoalat(mapper.mapAsList(Arrays.asList(opintoalat.getBody()), OpintoalaDto.class));
        }

        return koulutusalatDtos;
    }


}
