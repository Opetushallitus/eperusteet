package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import io.swagger.annotations.Api;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "/perusteet", produces = "application/json;charset=UTF-8")
@Api(value = "Julkaisut")
@Description("Perusteiden julkaisut")
public class JulkaisuController {

    @Autowired
    private JulkaisutService julkaisutService;

    @RequestMapping(method = GET, value = "/{perusteId}/julkaisu")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<JulkaisuBaseDto> getJulkaisut(
            @PathVariable("perusteId") final long id) {
        return julkaisutService.getJulkaisut(id);
    }

    @RequestMapping(method = POST, value = "/{projektiId}/julkaisu")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public JulkaisuBaseDto teeJulkaisu(
            @PathVariable("projektiId") final long projektiId,
            @RequestBody JulkaisuBaseDto julkaisuBaseDto) {
        return julkaisutService.teeJulkaisu(projektiId, julkaisuBaseDto);
    }

}
