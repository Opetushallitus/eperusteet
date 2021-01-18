package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.service.YlopsClient;
import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class YlopsClientImpl implements YlopsClient {

    @Value("${fi.vm.sade.eperusteet.eperusteet.ylops.service:''}")
    private String ylopsServiceUrl;

    private final static String TILASTOT_URL="/api/opetussuunnitelmat/adminlist";

    @Autowired
    private OphClientHelper ophClientHelper;

    @Override
    public JsonNode getTilastot() {
        return ophClientHelper.get(ylopsServiceUrl, ylopsServiceUrl + TILASTOT_URL, JsonNode.class);
    }
}
