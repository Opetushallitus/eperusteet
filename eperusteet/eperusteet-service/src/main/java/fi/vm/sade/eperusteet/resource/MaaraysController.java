package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.MaaraysDto;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.MaaraysService;
import io.swagger.annotations.Api;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@InternalApi
@RestController
@RequestMapping(value = "/maarays")
@Api("Maaraykset")
public class MaaraysController {

    @Autowired
    private MaaraysService maaraysService;

    @RequestMapping(method = GET)
    public List<MaaraysDto> getMaaraykset() {
        return maaraysService.getMaaraykset();
    }

    @InternalApi
    @RequestMapping(method = POST)
    public MaaraysDto addMaarays(@RequestBody MaaraysDto maaraysDto) {
        return maaraysService.addMaarays(maaraysDto);
    }

    @InternalApi
    @RequestMapping(value = "/{id}", method = POST)
    public MaaraysDto updateMaarays(
            @PathVariable("id") final Long id,
            @RequestBody MaaraysDto maaraysDto) {
        return maaraysService.updateMaarays(maaraysDto);
    }

    @InternalApi
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = DELETE)
    public void deleteMaarays(@PathVariable("id") final Long id) {
        maaraysService.deleteMaarays(id);
    }
}
