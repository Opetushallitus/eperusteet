package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuDto;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
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
