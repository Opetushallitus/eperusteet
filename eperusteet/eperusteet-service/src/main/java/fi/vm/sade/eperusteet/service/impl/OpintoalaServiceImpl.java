package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.dto.OpintoalaDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.service.OpintoalaService;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class OpintoalaServiceImpl implements OpintoalaService {

    @Autowired
    @Koodisto
    private DtoMapper mapper;

    @Value("${cas.service.koodisto-service:'https://virkailija.opintopolku.fi/koodisto-service'}")
    private final String KOODISTO_REST_SERVICE = "https://virkailija.opintopolku.fi/koodisto-service";

    private final String KOODISTO_REST_URL = "/rest/json/";

    private static final String OPINTOALA_URI = "opintoalaoph2002";

    @Autowired
    HttpEntity httpEntity;

    @Override
    @Cacheable("opintoalat")
    public List<OpintoalaDto> getAll() {
        RestTemplate restTemplate = new RestTemplate();
        String url = KOODISTO_REST_SERVICE + KOODISTO_REST_URL + OPINTOALA_URI + "/koodi/";
        try {
            ResponseEntity<KoodistoKoodiDto[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, KoodistoKoodiDto[].class);
            return mapper.mapAsList(Arrays.asList(response.getBody()), OpintoalaDto.class);
        }
        catch (HttpServerErrorException ex) {
        }
        return new ArrayList<>();
    }

}
