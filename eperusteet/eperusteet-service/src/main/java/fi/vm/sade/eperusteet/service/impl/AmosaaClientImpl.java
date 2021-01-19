package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.dto.tilastot.OpetussuunnitelmaTilastoDto;
import fi.vm.sade.eperusteet.service.AmosaaClient;
import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class AmosaaClientImpl implements AmosaaClient {

    @Value("${fi.vm.sade.eperusteet.eperusteet.amosaa.service:''}")
    private String amosaaServiceUrl;

    private final static String TILASTOT_URL="/api/tilastot/opetussuunnitelmat";

    @Autowired
    private OphClientHelper ophClientHelper;

    @Override
    public List<Object> getTilastot() {

        List<Object> tulos = new ArrayList<>();
        OpetussuunnitelmaTilastoDto tilastot = null;
        int sivu = 0;

        while (tilastot == null || !ObjectUtils.isEmpty(tilastot.getData())) {
            tilastot = ophClientHelper.get(amosaaServiceUrl, String.format(amosaaServiceUrl + TILASTOT_URL + "/%d/100", sivu++), OpetussuunnitelmaTilastoDto.class);
            tulos.addAll(tilastot.getData());
        }

        return tulos;
    }
}
