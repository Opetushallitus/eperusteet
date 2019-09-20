package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import io.swagger.annotations.Api;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(value = "/perusteet/{perusteId}/julkaisu", produces = "application/json;charset=UTF-8")
@Api(value = "Julkaisut")
@Description("Perusteiden julkaisut")
@Profile("!test")
public class JulkaisuController {

    @Autowired
    private JulkaisutService julkaisutService;

    @RequestMapping(method = GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<JulkaisuBaseDto> getJulkaisu(
            @PathVariable("perusteId") final long id) {
        return julkaisutService.getJulkaisut(id);
    }
}
