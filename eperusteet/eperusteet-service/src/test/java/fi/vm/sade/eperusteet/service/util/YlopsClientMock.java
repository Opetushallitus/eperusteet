package fi.vm.sade.eperusteet.service.util;

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.dto.julkinen.OpetussuunnitelmaEtusivuDto;
import fi.vm.sade.eperusteet.service.YlopsClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile({"test", "docker"})
public class YlopsClientMock implements YlopsClient {
    @Override
    public JsonNode getTilastot() {
        return null;
    }

    @Override
    public List<OpetussuunnitelmaEtusivuDto> getOpetussuunnitelmatEtusivu() {
        return List.of();
    }

    @Override
    public void arkistoiPeruste(Long perusteId) {

    }

    @Override
    public void poistaArkistointi(Long perusteId) {

    }
}
