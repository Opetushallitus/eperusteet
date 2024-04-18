package fi.vm.sade.eperusteet.service.dokumentti.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.eperusteet.domain.GeneratorVersion;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.dokumentti.ExternalPdfService;
import fi.vm.sade.eperusteet.utils.client.RestClientFactory;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

@Service
public class ExternalPdfServiceImpl implements ExternalPdfService {

    @Value("${fi.vm.sade.eperusteet.eperusteet.pdf-service:''}")
    private String pdfServiceUrl;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    RestClientFactory restClientFactory;

    private final ObjectMapper mapper = InitJacksonConverter.createMapper();

    @Override
    public void generatePdf(DokumenttiDto dto) throws JsonProcessingException {
        PerusteKaikkiDto sisalto = perusteService.getKaikkiSisalto(dto.getPerusteId());
        String json = mapper.writeValueAsString(sisalto);
        OphHttpClient client = restClientFactory.get(pdfServiceUrl, true);
        String url = pdfServiceUrl + "/api/pdf/generate/eperusteet/" + dto.getId() + "/" + dto.getKieli().name();

        if (dto.getGeneratorVersion().equals(GeneratorVersion.KVLIITE)) {
            url = url + "/kvliite";
        }

        String result = (String) client.execute(
                OphHttpRequest.Builder
                    .post(url)
                    .addHeader("Content-Type", "application/json;charset=UTF-8")
                    .setEntity(new OphHttpEntity.Builder()
                            .content(json)
                            .contentType(ContentType.APPLICATION_JSON)
                            .build())
                    .build())
                .handleErrorStatus(SC_FOUND, SC_UNAUTHORIZED, SC_FORBIDDEN, SC_METHOD_NOT_ALLOWED, SC_BAD_REQUEST, SC_INTERNAL_SERVER_ERROR, SC_NOT_FOUND)
                .with(res -> Optional.of("error"))
                .expectedStatus(SC_ACCEPTED)
                .mapWith(res -> res)
                .orElse(null);

        if (!ObjectUtils.isEmpty(result)) {
            throw new RuntimeException("Virhe pdf-palvelun kutsussa");
        }
    }
}
