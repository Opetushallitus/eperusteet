package fi.vm.sade.eperusteet.resource.julkinen;


import fi.vm.sade.eperusteet.dto.peruste.TutkinnonOsaQueryDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/tutkinnonosat")
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
//            @ApiImplicitParam(name = "nimi", dataType = "string", paramType = "query"),
            @ApiImplicitParam(
                    name = "koodiUri",
                    dataType = "string",
                    required = true,
                    paramType = "query",
                    value = "tutkinnonosakoodi",
                    example = "tutkinnonosat_123456"),
    })
    public Page<TutkinnonOsaDto> getAll(@ApiIgnore TutkinnonOsaQueryDto pquery) {
        return service.findTutkinnonOsatBy(pquery);
    }
}
