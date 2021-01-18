package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.service.AmosaaClient;
import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AmosaaClientImpl implements AmosaaClient {

    @Value("${fi.vm.sade.eperusteet.eperusteet.amosaa.service:''}")
    private String amosaaServiceUrl;

    private final static String TILASTOT_URL="/api/tilastot/opetussuunnitelmat";

    @Autowired
    private OphClientHelper ophClientHelper;

    @Override
    public JsonNode getTilastot() {
        return ophClientHelper.get(amosaaServiceUrl, amosaaServiceUrl + TILASTOT_URL, JsonNode.class);
    }
}
