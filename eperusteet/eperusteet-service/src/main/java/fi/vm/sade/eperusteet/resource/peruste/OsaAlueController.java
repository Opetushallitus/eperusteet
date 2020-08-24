package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.dto.LukkoDto;
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
@RequestMapping(value = "/tutkinnonosat/{viiteId}/osaalueet")
@Api(value = "OsaAlueet")
@InternalApi
public class OsaAlueController {

    @Autowired
    private OsaAlueService service;

    @RequestMapping(value = "/{osaAlueId}", method = GET)
    @ResponseBody
    public ResponseEntity<OsaAlueLaajaDto> getOsaAlueV2(
            @PathVariable("viiteId") final Long viiteId,
            @PathVariable("osaAlueId") final Long osaAlueId) {
        return new ResponseEntity<>(service.getOsaAlue(viiteId, osaAlueId), HttpStatus.OK);
    }

    @RequestMapping(method = POST)
    @ResponseBody
    public ResponseEntity<OsaAlueLaajaDto> addOsaAlueV2(
            @PathVariable("viiteId") final Long viiteId,
            @RequestBody OsaAlueLaajaDto osaAlue) {
        return new ResponseEntity<>(service.addOsaAlue(viiteId, osaAlue), HttpStatus.OK);
    }

    @RequestMapping(value = "/{osaAlueId}", method = PUT)
    @ResponseBody
    public ResponseEntity<OsaAlueLaajaDto> updateOsaAlueV2(
            @PathVariable("viiteId") final Long viiteId,
            @PathVariable("osaAlueId") final Long osaAlueId,
            @RequestBody OsaAlueLaajaDto osaAlue) {
        return new ResponseEntity<>(service.updateOsaAlue(viiteId, osaAlueId, osaAlue), HttpStatus.OK);
    }

    @RequestMapping(value = "/{osaAlueId}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeOsaAlueV2(
            @PathVariable("viiteId") final Long viiteId,
            @PathVariable("osaAlueId") final Long osaAlueId) {
        service.removeOsaAlue(viiteId, osaAlueId);
    }

    @RequestMapping(value = "/{osaAlueId}/lukko", method = GET)
    @ResponseBody
    public ResponseEntity<LukkoDto> getOsaAlueLock(
            @PathVariable("viiteId") final Long viiteId,
            @PathVariable("osaAlueId") final Long osaAlueId) {
        LukkoDto lock = service.getOsaAlueLock(viiteId, osaAlueId);
        if (lock != null) {
            return new ResponseEntity<>(lock, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{osaAlueId}/lukko", method = POST)
    @ResponseBody
    public ResponseEntity<LukkoDto> lockOsaAlue(
            @PathVariable("viiteId") final Long viiteId,
            @PathVariable("osaAlueId") final Long osaAlueId) {
        return new ResponseEntity<>(service.lockOsaAlue(viiteId, osaAlueId), HttpStatus.OK);
    }

    @RequestMapping(value = "/{osaAlueId}/lukko", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlockOsaAlue(
            @PathVariable("viiteId") final Long viiteId,
            @PathVariable("osaAlueId") final Long osaAlueId) {
        service.unlockOsaAlue(viiteId, osaAlueId);
    }

}
