package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiQuery;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.OsaamismerkkiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/osaamismerkit")
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
            @ApiImplicitParam(name = "tila", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "kategoria", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "voimassa", dataType = "boolean", paramType = "query"),
            @ApiImplicitParam(name = "julkaistu", dataType = "boolean", paramType = "query"),
            @ApiImplicitParam(name = "laadinta", dataType = "boolean", paramType = "query")
    })
    @RequestMapping(value = "/haku", method = GET)
    public Page<OsaamismerkkiDto> findOsaamismerkitBy(@ApiIgnore OsaamismerkkiQuery query) {
        return osaamismerkkiService.findBy(query);
    }

}
