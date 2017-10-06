package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.TiedoteService;
import fi.vm.sade.eperusteet.service.audit.EperusteetAudit;

import static fi.vm.sade.eperusteet.service.audit.EperusteetMessageFields.TIEDOTE;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.LISAYS;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.MUOKKAUS;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.POISTO;

import fi.vm.sade.eperusteet.service.audit.LogMessage;
import io.swagger.annotations.Api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * @author mikkom
 */
@RestController
@RequestMapping("/tiedotteet")
@Api(value = "Tiedotteet", description = "Tiedotteiden hallinta")
public class TiedoteController {

    @Autowired
    private EperusteetAudit audit;

    @Autowired
    private TiedoteService tiedoteService;

    @RequestMapping(method = GET)
    @ResponseBody
    public List<TiedoteDto> getAll(
            @RequestParam(value = "vainJulkiset", required = false, defaultValue = "false") boolean vainJulkiset,
            @RequestParam(value = "perusteprojekti", required = false) Long perusteId,
            @RequestParam(value = "alkaen", required = false, defaultValue = "0") Long alkaen) {
        return tiedoteService.getAll(vainJulkiset, alkaen, perusteId);
    }

    @RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<TiedoteDto> get(@PathVariable("id") final Long id) {
        final TiedoteDto tiedote = tiedoteService.getTiedote(id);
        return new ResponseEntity<>(tiedote, tiedote == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @RequestMapping(method = POST)
    @ResponseBody
    @InternalApi
    public ResponseEntity<TiedoteDto> addTiedote(@RequestBody TiedoteDto tiedoteDto) {
        return audit.withAudit(LogMessage.builder(null, TIEDOTE, LISAYS), (Void) -> {
            return new ResponseEntity<>(tiedoteService.addTiedote(tiedoteDto), HttpStatus.OK);
        });
    }

    @RequestMapping(value = "/{id}", method = POST)
    @ResponseBody
    @InternalApi
    public ResponseEntity<TiedoteDto> updateTiedote(
            @PathVariable("id") final Long id,
            @RequestBody TiedoteDto tiedoteDto) {
        return audit.withAudit(LogMessage.builder(null, TIEDOTE, MUOKKAUS), (Void) -> {
            tiedoteDto.setId(id);
            return new ResponseEntity<>(tiedoteService.updateTiedote(tiedoteDto), HttpStatus.OK);
        });
    }

    @RequestMapping(value = "/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @InternalApi
    public void deleteTiedote(@PathVariable("id") final Long id) {
        audit.withAudit(LogMessage.builder(null, TIEDOTE, POISTO), (Void) -> {
            tiedoteService.removeTiedote(id);
            return null;
        });
    }
}
