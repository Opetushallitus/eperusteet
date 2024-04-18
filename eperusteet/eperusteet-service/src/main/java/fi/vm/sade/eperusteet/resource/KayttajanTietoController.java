package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;

import java.util.List;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/kayttajatieto")
@Api("Kayttajat")
@InternalApi
public class KayttajanTietoController {

    @Autowired
    KayttajanTietoService service;

    @RequestMapping(method = RequestMethod.GET)
    public KayttajanTietoDto getKirjautunutKayttajat() {
        return service.haeKirjautaunutKayttaja();
    }

    @RequestMapping(value = "/{oid:.+}", method = GET)
    public KayttajanTietoDto getKayttaja(@PathVariable("oid") final String oid) {
        return service.hae(oid);
    }

    @RequestMapping(value = "/{oid:.+}/perusteprojektit", method = GET)
    public List<KayttajanProjektitiedotDto> getKayttajanPerusteprojektit(@PathVariable("oid") final String oid) {
        return service.haePerusteprojektit(oid);
    }

    @RequestMapping(value = "/{oid:.+}/perusteprojektit/{projektiId}", method = GET)
    public KayttajanProjektitiedotDto getKayttajanPerusteprojekti(
            @PathVariable("oid") final String oid,
            @PathVariable("projektiId") final Long projektiId
    ) {
        return service.haePerusteprojekti(oid, projektiId);
    }

}
