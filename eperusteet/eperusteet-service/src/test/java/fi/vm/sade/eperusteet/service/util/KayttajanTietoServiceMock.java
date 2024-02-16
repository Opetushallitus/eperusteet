package fi.vm.sade.eperusteet.service.util;

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class KayttajanTietoServiceMock implements KayttajanTietoService {

    @Override
    public KayttajanTietoDto haeKirjautaunutKayttaja() {
        return hae(null);
    }

    @Override
    public KayttajanTietoDto hae(String oid) {
        return null;
    }

    @Override
    public Future<KayttajanTietoDto> haeAsync(String oid) {
        return new AsyncResult<>(null);
    }

    @Override
    public KayttajanProjektitiedotDto haePerusteprojekti(String oid, Long projektiId) {
        return null;
    }

    @Override
    public List<KayttajanProjektitiedotDto> haePerusteprojektit(String oid) {
        return Collections.emptyList();
    }

    @Override
    public List<KayttajanTietoDto> haeKayttajatiedot(List<String> oid) {
        return null;
    }

    @Override
    public JsonNode getOrganisaatioVirkailijat(String organisaatioOids) {
        return null;
    }

}
