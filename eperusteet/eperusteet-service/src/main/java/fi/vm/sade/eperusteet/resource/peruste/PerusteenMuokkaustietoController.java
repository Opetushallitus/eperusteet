package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.dto.MuokkaustietoKayttajallaDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;
import io.swagger.annotations.Api;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/muokkaustieto")
@InternalApi
@Api("Muokkaustieto")
public class PerusteenMuokkaustietoController {

    @Autowired
    private PerusteenMuokkaustietoService muokkausTietoService;

    @RequestMapping(value = "/{perusteId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<MuokkaustietoKayttajallaDto>> getPerusteenMuokkausTiedotWithLuomisaika(@PathVariable("perusteId") final Long perusteId,
                                                                                                      @RequestParam(value = "viimeisinLuomisaika", required = false) final Long viimeisinLuomisaika,
                                                                                                      @RequestParam(value = "lukumaara", required = false, defaultValue = "10") int lukumaara) {
        return ResponseEntity.ok(muokkausTietoService.getPerusteenMuokkausTietos(perusteId, viimeisinLuomisaika != null ? new Date(viimeisinLuomisaika) : new Date(), lukumaara));
    }
}
