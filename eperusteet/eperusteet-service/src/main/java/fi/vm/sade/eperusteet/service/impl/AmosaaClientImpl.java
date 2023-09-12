package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.dto.tilastot.OpetussuunnitelmaTilastoDto;
import fi.vm.sade.eperusteet.service.AmosaaClient;
import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import java.util.ArrayList;
import java.util.List;

import fi.vm.sade.eperusteet.utils.client.RestClientFactory;
import fi.vm.sade.javautils.http.OphHttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class AmosaaClientImpl implements AmosaaClient {

    @Value("${fi.vm.sade.eperusteet.eperusteet.amosaa.service:''}")
    private String amosaaServiceUrl;

    private final static String TILASTOT_URL="/api/tilastot/opetussuunnitelmat";
    private final static String ARVIOINTIASTEIKOT_URL="/api/arviointiasteikot";

    @Autowired
    private OphClientHelper ophClientHelper;

    @Autowired
    RestClientFactory restClientFactory;

    @Override
    @Cacheable("amosaatilastot")
    public List<Object> getTilastot() {

        List<Object> tulos = new ArrayList<>();
        OpetussuunnitelmaTilastoDto tilastot = null;
        int sivu = 0;

        while (tilastot == null || !ObjectUtils.isEmpty(tilastot.getData())) {
            tilastot = ophClientHelper.get(amosaaServiceUrl, String.format(amosaaServiceUrl + TILASTOT_URL + "/%d/20", sivu++), OpetussuunnitelmaTilastoDto.class);
            tulos.addAll(tilastot.getData());
        }

        return tulos;
    }

    @Override
    public void updateArvioinnit() {
        restClientFactory.get(amosaaServiceUrl, true)
                        .execute(OphHttpRequest.Builder
                                .get(amosaaServiceUrl + ARVIOINTIASTEIKOT_URL + "/update").build());
    }
}
