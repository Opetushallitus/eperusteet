package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.dto.julkinen.OpetussuunnitelmaEtusivuDto;
import fi.vm.sade.eperusteet.service.YlopsClient;
import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class YlopsClientImpl implements YlopsClient {

    @Value("${fi.vm.sade.eperusteet.eperusteet.ylops.service:''}")
    private String ylopsServiceUrl;

    @Value("${fi.vm.sade.eperusteet.eperusteet.ylops.service.internal:${fi.vm.sade.eperusteet.eperusteet.ylops.service:''}}")
    private String ylopsServiceUrl_internal;

    private final static String TILASTOT_URL="/api/opetussuunnitelmat/tilastot";
    private final static String KAIKKI_JULKAISTUT_OPETUSSUUNNITELMAT_URL ="/api//opetussuunnitelmat/julkiset/kaikki";

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
}
