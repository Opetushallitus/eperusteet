package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.peruste.TiedoteQuery;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.resource.util.KieliConverter;
import fi.vm.sade.eperusteet.service.TiedoteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 *
 * @author mikkom
 */
@RestController
@RequestMapping("/tiedotteet")
@Api(value = "Tiedotteet", description = "Tiedotteiden hallinta")
public class TiedoteController {

    @Autowired
    private TiedoteService tiedoteService;

    @InitBinder
    public void initBinder(final WebDataBinder webdataBinder) {
        webdataBinder.registerCustomEditor(Kieli.class, new KieliConverter());
    }

    @ApiOperation(value = "tiedotteiden haku")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sivu", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "sivukoko", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "kieli", dataType = "string", paramType = "query", allowMultiple = true, value = "tiedotteen kieli"),
            @ApiImplicitParam(name = "nimi", dataType = "string", paramType = "query", value = "hae nimellä"),
            @ApiImplicitParam(name = "perusteId", dataType = "long", paramType = "query", value = "hae perusteeseen liitetyt tiedotteet"),
            @ApiImplicitParam(name = "perusteeton", dataType = "boolean", paramType = "query", defaultValue = "true", value = "hae perusteettomat tiedotteet"),
            @ApiImplicitParam(name = "julkinen", dataType = "boolean", paramType = "query", defaultValue = "true", value = "hae julkiset tiedotteet"),
            @ApiImplicitParam(name = "yleinen", dataType = "boolean", paramType = "query", defaultValue = "true", value = "hae yleiset tiedotteet")
    })
    @RequestMapping(value = "/haku", method = GET)
    public Page<TiedoteDto> findTiedotteetBy(@ApiIgnore TiedoteQuery pquery) {
        return tiedoteService.findBy(pquery);
    }

    @RequestMapping(method = GET)
    public List<TiedoteDto> getAllTiedotteet(
        @RequestParam(value = "vainJulkiset", required = false, defaultValue = "false") boolean vainJulkiset,
        @RequestParam(value = "perusteId", required = false) Long perusteId,
        @RequestParam(value = "alkaen", required = false, defaultValue = "0") Long alkaen
    ) {
        return tiedoteService.getAll(vainJulkiset, alkaen, perusteId);
    }

    @RequestMapping(value = "/{id}", method = GET)
    public TiedoteDto getTiedote(@PathVariable("id") final Long id) {
        return tiedoteService.getTiedote(id);
    }

    @InternalApi
    @RequestMapping(method = POST)
    public TiedoteDto addTiedote(@RequestBody TiedoteDto tiedoteDto) {
        return tiedoteService.addTiedote(tiedoteDto);
    }

    @InternalApi
    @RequestMapping(value = "/{id}", method = POST)
    public TiedoteDto updateTiedote(
        @PathVariable("id") final Long id,
        @RequestBody TiedoteDto tiedoteDto) {
        tiedoteDto.setId(id);
        return tiedoteService.updateTiedote(tiedoteDto);
    }

    @InternalApi
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = DELETE)
    public void deleteTiedote(@PathVariable("id") final Long id) {
        tiedoteService.removeTiedote(id);
    }
}
