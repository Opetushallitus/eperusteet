package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.MuuMaaraysDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.MuutMaarayksetService;
import io.swagger.annotations.Api;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@InternalApi
@RestController
@RequestMapping(value = "/muutmaaraykset")
@Api("MuutMaaraykset")
public class MuuMaaraysController {

    @Autowired
    private MuutMaarayksetService muutMaarayksetService;

    @RequestMapping(method = GET)
    public List<MuuMaaraysDto> getMuutMaaraykset() {
        return muutMaarayksetService.getMaaraykset();
    }

    @InternalApi
    @RequestMapping(method = POST)
    public MuuMaaraysDto addMuuMaarays(@RequestBody MuuMaaraysDto muuMaaraysDto) {
        return muutMaarayksetService.addMaarays(muuMaaraysDto);
    }

    @InternalApi
    @RequestMapping(value = "/{id}", method = POST)
    public MuuMaaraysDto updateMuuMaarays(
            @PathVariable("id") final Long id,
            @RequestBody MuuMaaraysDto muuMaaraysDto) {
        return muutMaarayksetService.updateMaarays(muuMaaraysDto);
    }

    @InternalApi
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = DELETE)
    public void deleteMuuMaarays(@PathVariable("id") final Long id) {
        muutMaarayksetService.deleteMaarays(id);
    }
}
