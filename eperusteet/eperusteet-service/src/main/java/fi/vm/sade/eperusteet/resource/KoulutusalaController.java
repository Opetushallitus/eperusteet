package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.KoulutusalaDto;
import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.service.KoulutusalaService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/koulutusalat")
@InternalApi
public class KoulutusalaController {

    @Autowired
    private KoulutusalaService service;

    @RequestMapping(method = GET)
    @ResponseBody
    public List<KoulutusalaDto> getAllKoulutusalat() {
        List<KoulutusalaDto> klist = service.getAll();
        return klist;
    }

    /*@RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<KoulutusalaDto> get(@PathVariable("id") final Long id) {
        KoulutusalaDto k = service.get(id);
        if (k == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(k, HttpStatus.OK);
    }*/

}

