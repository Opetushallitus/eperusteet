package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.dto.PoistettuSisaltoDto;
import fi.vm.sade.eperusteet.service.PoistoService;
import io.swagger.annotations.Api;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/poistettusisalto", produces = "application/json;charset=UTF-8")
@Api(value = "PoistettuSisalto")
@InternalApi
public class PoistettuSisaltoController {

    @Autowired
    private PoistoService poistoService;

    @RequestMapping(value = "/peruste/{perusteId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<PoistettuSisaltoDto>> getPoistetutSisallot(
            @PathVariable("perusteId") final long perusteId) {
        return ResponseEntity.ok(poistoService.getRemoved(perusteId));
    }

    @RequestMapping(value = "/peruste/{perusteId}/palauta/{palautettavaId}", method = RequestMethod.POST)
    @ResponseBody
    public void palauta(
            @PathVariable("perusteId") final long perusteId,
            @PathVariable("palautettavaId") final long palautettavaId) {
        poistoService.restore(perusteId, palautettavaId);
    }

}
