package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.arviointi.ArviointiAsteikkoDto;
import fi.vm.sade.eperusteet.service.AmosaaClient;
import fi.vm.sade.eperusteet.service.ArviointiAsteikkoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/arviointiasteikot")
@Api(value="Arviointiasteikot")
public class ArviointiAsteikkoController {

    @Autowired
    ArviointiAsteikkoService service;

    @Autowired
    AmosaaClient amosaaClient;

    @RequestMapping(method = GET)
    public List<ArviointiAsteikkoDto> getAll() {
        return service.getAll();
    }

    @RequestMapping(value = "/{id}", method = GET)
    public ResponseEntity<ArviointiAsteikkoDto> get(@PathVariable("id") final Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @RequestMapping(method = PUT)
    public ResponseEntity<List<ArviointiAsteikkoDto>> updateArviointiasteikot(
            @RequestBody List<ArviointiAsteikkoDto> arviointiasteikotDtos
    ) {
        List<ArviointiAsteikkoDto> arviointiasteikot = service.update(arviointiasteikotDtos);
        amosaaClient.updateArvioinnit();
        return ResponseEntity.ok(arviointiasteikot);
    }
}
