package fi.vm.sade.eperusteet.resource.julkinen;


import fi.vm.sade.eperusteet.dto.peruste.TutkinnonOsaQueryDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteKontekstiDto;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/tutkinnonosat")
@Api(value = "Tutkinnonosat", description = "Tutkinnon osat")
public class TutkinnonOsaJulkinenController {
    @Autowired
    private PerusteenOsaService service;

    @RequestMapping(method = GET)
    @ResponseBody
    @ApiOperation(value = "hae tutkinnon osia")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sivu", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "sivukoko", dataType = "long", paramType = "query"),
            @ApiImplicitParam(
                    name = "koodiUri",
                    dataType = "string",
                    required = true,
                    paramType = "query",
                    value = "tutkinnonosakoodi",
                    example = "tutkinnonosat_123456"),
    })
    public Page<TutkinnonOsaDto> getAllTutkinnonOsatByKoodiUri(@ApiIgnore TutkinnonOsaQueryDto pquery) {
        return service.findTutkinnonOsatBy(pquery);
    }

    @RequestMapping(method = GET, value = "/all")
    @ResponseBody
    @ApiOperation(value = "hae tutkinnon osia")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sivu", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "sivukoko", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "nimi", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "perusteId", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "vanhentuneet", dataType = "boolean", paramType = "query"),
            @ApiImplicitParam(name = "kieli", dataType = "string", paramType = "query"),
    })
    public Page<TutkinnonOsaViiteKontekstiDto> getAllTutkinnonOsatBy(@ApiIgnore TutkinnonOsaQueryDto pquery) {
        return service.findAllTutkinnonOsatBy(pquery);
    }

    @RequestMapping(method = GET, value = "/{tutkinnonOsaId}/viitteet")
    @ResponseBody
    @ApiOperation(value = "hae tutkinnon osiin liittyvät viitteet")
    public List<TutkinnonOsaViiteKontekstiDto> getAllTutkinnonOsaViitteet(
            @PathVariable("tutkinnonOsaId") Long tutkinnonOsaId) {
        return service.findTutkinnonOsaViitteetByTutkinnonOsa(tutkinnonOsaId);
    }


}
