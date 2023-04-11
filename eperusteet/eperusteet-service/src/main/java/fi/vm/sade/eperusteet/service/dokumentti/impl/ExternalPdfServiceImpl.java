package fi.vm.sade.eperusteet.service.dokumentti.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.resource.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.dokumentti.ExternalPdfService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.utils.client.RestClientFactory;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Service
public class ExternalPdfServiceImpl implements ExternalPdfService {

    @Value("${fi.vm.sade.eperusteet.eperusteet.pdf-service:''}")
    private String pdfServiceUrl;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    HttpEntity httpEntity;

    @Autowired
    RestClientFactory restClientFactory;

    private final ObjectMapper mapper = InitJacksonConverter.createMapper();

//    @Override
//    public void generatePdf(DokumenttiDto dto) throws JsonProcessingException {
//        RestTemplate restTemplate = new RestTemplate();
//        PerusteKaikkiDto sisalto = perusteService.getKaikkiSisalto(dto.getPerusteId());
//        String json = mapper.writeValueAsString(sisalto);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<String> entity = new HttpEntity<>(json, headers);
//
//        String url = pdfServiceUrl + "/api/pdf/generate/eperusteet/" + dto.getId() + "/" + dto.getKieli().name() + "/" + dto.getGeneratorVersion().name();
//        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//    }

    @Override
    public void generatePdf(DokumenttiDto dto) throws JsonProcessingException {
        PerusteKaikkiDto sisalto = perusteService.getKaikkiSisalto(dto.getPerusteId());
        String json = mapper.writeValueAsString(sisalto);
        OphHttpClient client = restClientFactory.get(pdfServiceUrl, true);
        String url = pdfServiceUrl + "/api/pdf/generate/eperusteet/" + dto.getId() + "/" + dto.getKieli().name() + "/" + dto.getGeneratorVersion().name();

        client.execute(
                OphHttpRequest.Builder
                    .post(url)
                    .addHeader("Content-Type", "application/json;charset=UTF-8")
                    .setEntity(new OphHttpEntity.Builder()
                            .content(json)
                            .contentType(ContentType.APPLICATION_JSON)
                            .build())
                    .build());
    }
}
