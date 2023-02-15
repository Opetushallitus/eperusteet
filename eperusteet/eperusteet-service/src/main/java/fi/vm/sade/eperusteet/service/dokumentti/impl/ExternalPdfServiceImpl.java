package fi.vm.sade.eperusteet.service.dokumentti.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.resource.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.dokumentti.ExternalPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ExternalPdfServiceImpl implements ExternalPdfService {

    @Value("${fi.vm.sade.eperusteet.eperusteet.pdf-service:''}")
    private String pdfServiceUrl;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    HttpEntity httpEntity;

    private final ObjectMapper mapper = InitJacksonConverter.createMapper();

    @Override
    public void generatePdf(DokumenttiDto dto) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        PerusteKaikkiDto sisalto = perusteService.getKaikkiSisalto(dto.getPerusteId());
        String json = mapper.writeValueAsString(sisalto);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        String url = pdfServiceUrl + "/api/pdf/generate/eperusteet/" + dto.getId() + "/" + dto.getKieli().name() + "/" + dto.getGeneratorVersion().name();
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }
}
