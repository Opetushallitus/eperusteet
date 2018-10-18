package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.peruste.VapaaTekstiQueryDto;
import fi.vm.sade.eperusteet.dto.util.TekstiHakuTulosDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.PerusteService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.concurrent.ExecutionException;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@InternalApi
@RestController
@RequestMapping("/experimental")
public class ExperimentalController {

    @Autowired
    private PerusteService service;

    @RequestMapping(value = "/tekstihaku", method = GET)
    @ApiOperation(value = "perusteiden sisältöihin kohdistuva tekstihaku")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sivu", dataType = "integer", paramType = "query"),
            @ApiImplicitParam(name = "sivukoko", dataType = "integer", paramType = "query"),
            @ApiImplicitParam(name = "teksti", dataType = "string", paramType = "query", value = "Vapaatekstihaun merkkijono"),
            @ApiImplicitParam(name = "peruste", dataType = "long", paramType = "query", value = "Rajattava peruste")
    })
    public ResponseEntity<Page<TekstiHakuTulosDto>> getAll(@ApiIgnore VapaaTekstiQueryDto pquery) {
        Page<TekstiHakuTulosDto> result = service.findByTeksti(pquery);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        else {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    @RequestMapping(value = "/tekstihaku", method = POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void getAll() {
        service.rakennaTekstihaku();
    }

}
