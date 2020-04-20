package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.dto.peruste.PerusteAikatauluDto;
import fi.vm.sade.eperusteet.service.PerusteAikatauluService;
import io.swagger.annotations.Api;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/aikataulu")
@Api(value = "Aikataulut")
public class PerusteAikatauluController {

    @Autowired
    PerusteAikatauluService perusteAikatauluService;

    @RequestMapping(value = "/{perusteId}", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<PerusteAikatauluDto> updatePerusteenAikataulut(@PathVariable("perusteId") final Long perusteId, @RequestBody List<PerusteAikatauluDto> aikataulut) {
        return perusteAikatauluService.save(perusteId, aikataulut);
    }
}
