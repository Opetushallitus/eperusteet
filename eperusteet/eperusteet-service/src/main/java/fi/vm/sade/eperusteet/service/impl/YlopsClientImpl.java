package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.dto.julkinen.OpetussuunnitelmaEtusivuDto;
import fi.vm.sade.eperusteet.service.YlopsClient;
import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("!test & !docker")
public class YlopsClientImpl implements YlopsClient {

    @Value("${fi.vm.sade.eperusteet.eperusteet.ylops.service:''}")
    private String ylopsServiceUrl;

    @Value("${fi.vm.sade.eperusteet.eperusteet.ylops.service.internal:${fi.vm.sade.eperusteet.eperusteet.ylops.service:''}}")
    private String ylopsServiceUrl_internal;

    private final static String TILASTOT_URL="/api/opetussuunnitelmat/tilastot";
    private final static String KAIKKI_JULKAISTUT_OPETUSSUUNNITELMAT_URL ="/api/opetussuunnitelmat/julkiset/kaikki";
    private final static String ADD_ARKISTOITU_PERUSTE_URL = "/api/peruste/arkistoitu/%d/arkistoi";
    private final static String DELETE_ARKISTOITU_PERUSTE_URL = "/api/peruste/arkistoitu/%d/poista";

    @Autowired
    private OphClientHelper ophClientHelper;

    @Override
    @Cacheable("ylopstilastot")
    public JsonNode getTilastot() {
        return ophClientHelper.get(ylopsServiceUrl, ylopsServiceUrl_internal + TILASTOT_URL, JsonNode.class);
    }

    @Override
    public List<OpetussuunnitelmaEtusivuDto> getOpetussuunnitelmatEtusivu() {
        return ophClientHelper.getList(ylopsServiceUrl, ylopsServiceUrl + KAIKKI_JULKAISTUT_OPETUSSUUNNITELMAT_URL, OpetussuunnitelmaEtusivuDto.class);
    }

    @Override
    public void arkistoiPeruste(Long perusteId) {
        String url = ylopsServiceUrl_internal + String.format(ADD_ARKISTOITU_PERUSTE_URL, perusteId);
        ophClientHelper.post(ylopsServiceUrl, url);
    }

    @Override
    public void poistaArkistointi(Long perusteId) {
        String url = ylopsServiceUrl_internal + String.format(DELETE_ARKISTOITU_PERUSTE_URL, perusteId);
        ophClientHelper.post(ylopsServiceUrl, url);
    }


}
