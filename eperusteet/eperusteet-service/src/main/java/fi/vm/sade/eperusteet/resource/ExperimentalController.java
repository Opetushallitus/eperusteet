package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.peruste.VapaaTekstiQueryDto;
import fi.vm.sade.eperusteet.dto.util.TekstiHakuTulosDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.PerusteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@InternalApi
@RestController
@RequestMapping("/experimental")
public class ExperimentalController {

    @Autowired
    private PerusteService service;

    @RequestMapping(value = "/tekstihaku", method = GET)
    @ResponseBody
    public ResponseEntity<Page<TekstiHakuTulosDto>> getAll(VapaaTekstiQueryDto pquery) {
        try {
            Page<TekstiHakuTulosDto> result = service.findByTeksti(pquery);
            if (result != null) {
                return ResponseEntity.ok(result);
            }
        } catch (ExecutionException | InterruptedException ignored) {
        }
        return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
    }
}
