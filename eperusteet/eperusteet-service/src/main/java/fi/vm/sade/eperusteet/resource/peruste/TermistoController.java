package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.dto.peruste.TermiDto;
import fi.vm.sade.eperusteet.service.TermistoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/perusteet/{perusteId}")
@InternalApi
@Tag(name = "Termit")
public class TermistoController {

    @Autowired
    private TermistoService termistoService;

    @RequestMapping(value = "/termisto", method = GET)
    public List<TermiDto> getAllTermit(
        @PathVariable("perusteId") final Long perusteId) {
        return termistoService.getTermit(perusteId);
    }

    @RequestMapping(value = "/termisto", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public TermiDto addTermi(
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody TermiDto dto) {
        dto.setId(null);
        return termistoService.addTermi(perusteId, dto);
    }

    @RequestMapping(value = "/termisto/{avain}", method = GET)
    public TermiDto getTermi(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("avain") final String avain) {
        return termistoService.getTermi(perusteId, avain);
    }

    @RequestMapping(value = "/termisto/{id}", method = POST)
    public TermiDto updateTermi(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id,
            @RequestBody TermiDto dto) {
        dto.setId(id);
        return termistoService.updateTermi(perusteId, dto);
    }

    @RequestMapping(value = "/termisto/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTermi(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {
        termistoService.deleteTermi(perusteId, id);
    }
}
