package fi.vm.sade.eperusteet.service.impl;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.eperusteet.dto.PalauteDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.service.PalauteService;
import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import fi.vm.sade.eperusteet.utils.client.RestClientFactory;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Profile("!local")
@Service
@Slf4j
public class PalauteServiceImpl implements PalauteService {

    @Value("${sqs.palaute_queue_url:''}")
    private String palauteQueueUrl;

    @Value("${palaute-service.url:''}")
    private String palauteServiceUrl;

    @Autowired
    private OphClientHelper ophClientHelper;

    @Autowired
    private AmazonSQS amazonSQS;

    private ObjectMapper objectMapper = new ObjectMapper();
    private final static String PALAUTE_KEY = "eperusteet-opintopolku";

    @Override
    public PalauteDto lahetaPalaute(PalauteDto palaute) throws JsonProcessingException {
        palaute.setCreatedAt(new Date());
        palaute.setKey(PALAUTE_KEY);

        log.debug("send feedback {}", objectMapper.writeValueAsString(palaute));

        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(palauteQueueUrl)
                .withMessageBody(objectMapper.writeValueAsString(palaute))
                .withDelaySeconds(5);
        amazonSQS.sendMessage(send_msg_request);

        return palaute;
    }

    @Override
    public List<Object> getPalautteet() { // ei toimi kunnes palauteservicen kirjautuminen on korjattu
        return ophClientHelper.getList(palauteServiceUrl, palauteServiceUrl + "/api/palaute", Object.class);
    }

}
