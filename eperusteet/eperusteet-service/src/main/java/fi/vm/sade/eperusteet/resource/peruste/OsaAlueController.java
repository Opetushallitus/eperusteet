package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueKokonaanDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueLaajaDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.OsaAlueService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/tutkinnonosat/{viiteId}/osaalueet")
@Api(value = "OsaAlueet")
@InternalApi
public class OsaAlueController {

    @Autowired
    private OsaAlueService service;

//    @RequestMapping(value = "/{osaAlueId}", method = GET)
//    @ResponseBody
//    public ResponseEntity<OsaAlueLaajaDto> getOsaAlue(
//            @PathVariable("viiteId") final Long viiteId,
//            @PathVariable("osaAlueId") final Long osaAlueId) {
//        return new ResponseEntity<>(service.getOsaAlue(viiteId, osaAlueId), HttpStatus.OK);
//    }
//
//    @RequestMapping(value = "/{osaAlueId}", method = PUT)
//    @ResponseBody
//    public ResponseEntity<OsaAlueLaajaDto> updateOsaAlue(
//            @PathVariable("viiteId") final Long viiteId,
//            @PathVariable("osaAlueId") final Long osaAlueId,
//            @RequestBody OsaAlueLaajaDto osaAlue) {
//        return new ResponseEntity<>(service.updateOsaAlue(viiteId, osaAlueId, osaAlue), HttpStatus.OK);
//    }
//
//    @RequestMapping(value = "/{osaAlueId}", method = DELETE)
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void removeOsaAlue(
//            @PathVariable("viiteId") final Long viiteId,
//            @PathVariable("osaAlueId") final Long osaAlueId) {
//        service.removeOsaAlue(viiteId, osaAlueId);
//    }
//
//    @RequestMapping(value = "/{osaAlueId}", method = POST)
//    @ResponseBody
//    public ResponseEntity<OsaAlueLaajaDto> addOsaAlue(
//            @PathVariable("viiteId") final Long viiteId,
//            @RequestBody OsaAlueLaajaDto osaAlue) {
//        return new ResponseEntity<>(service.addOsaAlue(viiteId, osaAlue), HttpStatus.OK);
//    }

}
