package fi.vm.sade.eperusteet.resource;

import com.wordnik.swagger.annotations.Api;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.service.TiedoteService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 *
 * @author mikkom
 */
@RestController
@RequestMapping("/tiedotteet")
@Api(value="Tiedotteet", description = "Tiedotteiden hallinta")
public class TiedoteController {
    @Autowired
    private TiedoteService tiedoteService;

    @RequestMapping(method = GET)
    @ResponseBody
    public List<TiedoteDto> getAll() {
        return tiedoteService.getAll();
    }

    @RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<TiedoteDto> get(@PathVariable("id") final Long id) {
        return new ResponseEntity<>(tiedoteService.getTiedote(id), HttpStatus.OK);
    }

    @RequestMapping(method = POST)
    @ResponseBody
    public ResponseEntity<TiedoteDto> addTiedote(@RequestBody TiedoteDto tiedoteDto)
    {
        return new ResponseEntity<>(tiedoteService.addTiedote(tiedoteDto), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = POST)
    @ResponseBody
    public ResponseEntity<TiedoteDto> updateTiedote(
            @PathVariable("id") final Long id,
            @RequestBody TiedoteDto tiedoteDto) {
        tiedoteDto.setId(id);
        return new ResponseEntity<>(tiedoteService.updateTiedote(tiedoteDto), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTiedote(@PathVariable("id") final Long id) {
        tiedoteService.removeTiedote(id);
    }
}
