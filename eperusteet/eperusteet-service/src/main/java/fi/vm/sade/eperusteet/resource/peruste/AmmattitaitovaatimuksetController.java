package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.dto.AmmattitaitovaatimusQueryDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteKontekstiDto;
import fi.vm.sade.eperusteet.service.AmmattitaitovaatimusService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(value = "/ammattitaitovaatimukset")
@Api(value = "Ammattitaitovaatimukset")
public class AmmattitaitovaatimuksetController {

    @Autowired
    private AmmattitaitovaatimusService ammattitaitovaatimusService;

    @RequestMapping(value = "/perusteet", method = GET)
    @ApiOperation(value = "Ammattitaitovaatimuksen sisältävien perusteiden haku")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sivu", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "sivukoko", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "uri", dataType = "string", paramType = "query")
    })
    public Page<PerusteBaseDto> getPerusteetByAmmattitaitovaatimus(@ApiIgnore AmmattitaitovaatimusQueryDto pquery) {
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        Page<PerusteBaseDto> result = ammattitaitovaatimusService.findPerusteet(p, pquery);
        return result;
    }

    @RequestMapping(value = "/tutkinnonosat", method = GET)
    @ApiOperation(value = "Ammattitaitovaatimuksen sisältävien perusteiden haku")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sivu", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "sivukoko", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "uri", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "kaikki", dataType = "boolean", paramType = "query"),
    })
    public Page<TutkinnonOsaViiteKontekstiDto> getTutkinnonOsatByAmmattitaitovaatimus(@ApiIgnore AmmattitaitovaatimusQueryDto pquery) {
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return ammattitaitovaatimusService.findTutkinnonOsat(p, pquery);
    }

}
