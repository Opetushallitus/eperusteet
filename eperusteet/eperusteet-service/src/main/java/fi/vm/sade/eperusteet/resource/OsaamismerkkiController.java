package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiBaseDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiKategoriaDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiQuery;
import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.service.OsaamismerkkiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/osaamismerkit")
@Api(value = "Osaamismerkit")
@InternalApi
public class OsaamismerkkiController {

    @Autowired
    private OsaamismerkkiService osaamismerkkiService;

    @ApiOperation(value = "osaamismerkkien haku")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sivu", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "sivukoko", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "nimi", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "tila", dataType = "string", paramType = "query", allowMultiple = true),
            @ApiImplicitParam(name = "koodit", dataType = "long", paramType = "query", allowMultiple = true),
            @ApiImplicitParam(name = "kategoria", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "voimassa", dataType = "boolean", paramType = "query"),
            @ApiImplicitParam(name = "tuleva", dataType = "boolean", paramType = "query"),
            @ApiImplicitParam(name = "poistunut", dataType = "boolean", paramType = "query"),
            @ApiImplicitParam(name = "kieli", dataType = "string", paramType = "query"),
    })
    @RequestMapping(value = "/haku", method = GET)
    public Page<OsaamismerkkiDto> findOsaamismerkitBy(@ApiIgnore OsaamismerkkiQuery query) {
        return osaamismerkkiService.findBy(query);
    }

    @ApiOperation(value = "julkisten osaamismerkkien haku")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nimi", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "kategoria", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "koodit", dataType = "long", paramType = "query", allowMultiple = true),
            @ApiImplicitParam(name = "poistunut", dataType = "boolean", paramType = "query"),
            @ApiImplicitParam(name = "kieli", dataType = "string", paramType = "query")
    })
    @RequestMapping(value = "/haku/julkiset", method = GET)
    public List<OsaamismerkkiBaseDto> findJulkisetOsaamismerkitBy(@ApiIgnore OsaamismerkkiQuery query) {
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

    @ApiImplicitParams({
            @ApiImplicitParam(name = "poistunut", dataType = "boolean", paramType = "query"),
            @ApiImplicitParam(name = "kieli", dataType = "string", paramType = "query")
    })
    @RequestMapping(value = "/kategoriat/julkiset", method = GET)
    @ResponseBody
    public List<OsaamismerkkiKategoriaDto> getJulkisetKategoriat(@ApiIgnore OsaamismerkkiQuery query) {
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
