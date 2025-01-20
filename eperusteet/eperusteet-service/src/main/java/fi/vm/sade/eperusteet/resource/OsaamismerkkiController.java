package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiBaseDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiKategoriaDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiQuery;
import fi.vm.sade.eperusteet.service.OsaamismerkkiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/osaamismerkit")
@Tag(name = "Osaamismerkit")
@InternalApi
public class OsaamismerkkiController {

    @Autowired
    private OsaamismerkkiService osaamismerkkiService;

    @Operation(summary = "osaamismerkkien haku")
    @Parameters({
            @Parameter(name = "sivu", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "sivukoko", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "nimi", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "tila", array = @ArraySchema(schema = @Schema(type = "string")), in = ParameterIn.QUERY),
            @Parameter(name = "koodit", array = @ArraySchema(schema = @Schema(type = "number")), in = ParameterIn.QUERY),
            @Parameter(name = "kategoria", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "voimassa", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY),
            @Parameter(name = "tuleva", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY),
            @Parameter(name = "poistunut", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY),
            @Parameter(name = "kieli", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
    })
    @RequestMapping(value = "/haku", method = GET)
    public Page<OsaamismerkkiDto> findOsaamismerkitBy(@Parameter(hidden = true) OsaamismerkkiQuery query) {
        return osaamismerkkiService.findBy(query);
    }

    @Operation(summary = "julkisten osaamismerkkien haku")
    @Parameters({
            @Parameter(name = "nimi", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "kategoria", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "koodit", in = ParameterIn.QUERY, array = @ArraySchema(schema = @Schema(type = "number"))),
            @Parameter(name = "poistunut", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY),
            @Parameter(name = "kieli", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY)
    })
    @RequestMapping(value = "/haku/julkiset", method = GET)
    public List<OsaamismerkkiBaseDto> findJulkisetOsaamismerkitBy(@Parameter(hidden = true) OsaamismerkkiQuery query) {
        return osaamismerkkiService.findJulkisetBy(query);
    }

    @RequestMapping(value = "/osaamismerkki/{id}", method = GET)
    @ResponseBody
    public OsaamismerkkiBaseDto getJulkinenOsaamismerkkiById(@PathVariable("id") final Long id) {
        return osaamismerkkiService.getJulkinenOsaamismerkkiById(id);
    }

    @RequestMapping(value = "/osaamismerkki/koodi/{koodi}", method = GET)
    @ResponseBody
    public OsaamismerkkiBaseDto getJulkinenOsaamismerkkiByKoodi(@PathVariable("koodi") final Long koodi) {
        return osaamismerkkiService.getJulkinenOsaamismerkkiByKoodi(koodi);
    }

    @RequestMapping(value = "/osaamismerkki/update", method = POST)
    @ResponseBody
    public OsaamismerkkiDto updateOsaamismerkki(@RequestBody OsaamismerkkiDto osaamismerkkiDto) {
        return osaamismerkkiService.updateOsaamismerkki(osaamismerkkiDto);
    }

    @RequestMapping(value = "/osaamismerkki/delete/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteOsaamismerkki(@PathVariable("id") final Long id) {
        osaamismerkkiService.deleteOsaamismerkki(id);
    }

    @RequestMapping(value = "/kategoriat", method = GET)
    @ResponseBody
    public List<OsaamismerkkiKategoriaDto> getKategoriat() {
        return osaamismerkkiService.getKategoriat();
    }

    @Parameters({
            @Parameter(name = "poistunut", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY),
            @Parameter(name = "kieli", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY)
    })
    @RequestMapping(value = "/kategoriat/julkiset", method = GET)
    @ResponseBody
    public List<OsaamismerkkiKategoriaDto> getJulkisetKategoriat(@Parameter(hidden = true) OsaamismerkkiQuery query) {
        return osaamismerkkiService.getJulkisetKategoriat(query);
    }

    @RequestMapping(value = "/kategoria/update", method = POST)
    @ResponseBody
    public OsaamismerkkiKategoriaDto updateKategoria(@RequestBody OsaamismerkkiKategoriaDto osaamismerkkiKategoriaDto) throws HttpMediaTypeNotSupportedException, MimeTypeException {
        return osaamismerkkiService.updateKategoria(osaamismerkkiKategoriaDto);
    }

    @RequestMapping(value = "/kategoria/delete/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteKategoria(@PathVariable("id") final Long id) {
        osaamismerkkiService.deleteKategoria(id);
    }
}
