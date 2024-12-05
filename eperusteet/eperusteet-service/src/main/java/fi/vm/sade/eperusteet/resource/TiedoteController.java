package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.peruste.TiedoteQuery;
import fi.vm.sade.eperusteet.resource.util.KieliConverter;
import fi.vm.sade.eperusteet.service.TiedoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/tiedotteet")
@Tag(name = "Tiedotteet", description = "Tiedotteiden hallinta")
public class TiedoteController {

    @Autowired
    private TiedoteService tiedoteService;

    @InitBinder
    public void initBinder(final WebDataBinder webdataBinder) {
        webdataBinder.registerCustomEditor(Kieli.class, new KieliConverter());
    }

    @Operation(summary = "tiedotteiden haku")
    @Parameters({
            @Parameter(name = "sivu", schema = @Schema(implementation = Integer.class), in = ParameterIn.QUERY),
            @Parameter(name = "sivukoko", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "kieli", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, array=@ArraySchema(schema = @Schema()), description = "tiedotteen kieli"),
            @Parameter(name = "nimi", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, description = "hae nimellä"),
            @Parameter(name = "perusteId", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY, description = "hae perusteeseen liitetyt tiedotteet"),
            @Parameter(name = "perusteeton", schema = @Schema(implementation = Boolean.class, defaultValue = "true"), in = ParameterIn.QUERY, description = "hae perusteettomat tiedotteet"),
            @Parameter(name = "julkinen", schema = @Schema(implementation = Boolean.class, defaultValue = "true"), in = ParameterIn.QUERY,  description = "hae julkiset tiedotteet"),
            @Parameter(name = "yleinen", schema = @Schema(implementation = Boolean.class, defaultValue = "true"), in = ParameterIn.QUERY, description = "hae yleiset tiedotteet"),
            @Parameter(name = "tiedoteJulkaisuPaikka", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, array=@ArraySchema(schema = @Schema()), description = "tiedotteen julkaisupaikat"),
            @Parameter(name = "perusteIds", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY, array=@ArraySchema(schema = @Schema()), description = "tiedotteen perusteiden"),
            @Parameter(name = "koulutusTyyppi", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, array=@ArraySchema(schema = @Schema()), description = "tiedotteen koulutustyypit"),
            @Parameter(name = "jarjestys", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, description = "tiedotteen jarjestys"),
            @Parameter(name = "jarjestysNouseva", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY, description = "tiedotteen jarjestyksen suunta"),
            @Parameter(name = "koulutustyypiton", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY, description = "koulutustyypiton tiedote"),
    })
    @RequestMapping(value = "/haku", method = GET)
    public Page<TiedoteDto> findTiedotteetBy(@Parameter(hidden = true) TiedoteQuery tquery) {
        return tiedoteService.findBy(tquery);
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
