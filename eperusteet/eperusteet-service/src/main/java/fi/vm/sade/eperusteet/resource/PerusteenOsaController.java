package fi.vm.sade.eperusteet.resource;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/api/perusteenosat")
public class PerusteenOsaController {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteenOsaController.class);
    
    @Autowired
    private PerusteenOsaService service;

    @RequestMapping(method = GET)
    @ResponseBody
    public List<? extends PerusteenOsa> getAll() {
        LOG.info("FINDALL");
        return service.getAll();
    }

    @RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<PerusteenOsa> get(@PathVariable("id") final Long id) {
        PerusteenOsa t = service.get(id);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<PerusteenOsa> add(@RequestBody PerusteenOsa perusteenOsa, UriComponentsBuilder ucb, HttpServletRequest req) {
        perusteenOsa.setId(null);
        service.add(perusteenOsa);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucb.path("/perusteenosat/{id}").buildAndExpand(perusteenOsa.getId()).toUri());
        return new ResponseEntity<>(perusteenOsa, headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}", method = POST)
    @ResponseBody
    public PerusteenOsa update(@PathVariable("id") final Long id, @RequestBody PerusteenOsa perusteenOsa) {
        LOG.info("save {}", perusteenOsa);
        perusteenOsa.setId(id);
        service.update(id, perusteenOsa);
        return perusteenOsa;
    }

    @RequestMapping(value = "/{id}", method = DELETE, consumes = "*/*")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final Long id) {
        LOG.info("delete {}", id);
        service.delete(id);
    }

}
