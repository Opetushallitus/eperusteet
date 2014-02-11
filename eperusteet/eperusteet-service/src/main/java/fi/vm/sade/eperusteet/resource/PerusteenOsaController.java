package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TutkinnonOsa;
import java.util.List;
import fi.vm.sade.eperusteet.dto.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.resource.util.PerusteenOsaMappings;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/perusteenosat")
public class PerusteenOsaController {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteenOsaController.class);
    
    @Autowired
    private PerusteenOsaService service;
    
    @RequestMapping(method = GET)
    public List<? extends PerusteenOsaDto> getAll() {
        LOG.info("FINDALL");
        return service.getAll();
    }

    @RequestMapping(value = "/{id}", method = GET)
    public ResponseEntity<PerusteenOsaDto> get(@PathVariable("id") final Long id) {
        PerusteenOsaDto t = service.get(id);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(method = POST, params = PerusteenOsaMappings.IS_TUTKINNON_OSA_PARAM)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TutkinnonOsaDto> add(@RequestBody TutkinnonOsaDto tutkinnonOsaDto, UriComponentsBuilder ucb) {
        LOG.info("add {}", tutkinnonOsaDto);
        tutkinnonOsaDto = service.add(tutkinnonOsaDto, TutkinnonOsaDto.class, TutkinnonOsa.class);
        return new ResponseEntity<>(tutkinnonOsaDto, buildHeadersFor(tutkinnonOsaDto.getId(), ucb), HttpStatus.CREATED);
    }
    
    @RequestMapping(method = POST, params = PerusteenOsaMappings.IS_TEKSTIKAPPALE_PARAM)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TekstiKappaleDto> add(@RequestBody TekstiKappaleDto tekstikappaleDto, UriComponentsBuilder ucb) {
        LOG.info("add {}", tekstikappaleDto);
        tekstikappaleDto = service.add(tekstikappaleDto, TekstiKappaleDto.class, TekstiKappale.class);
        return new ResponseEntity<>(tekstikappaleDto, buildHeadersFor(tekstikappaleDto.getId(), ucb), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}", method = POST)
    public PerusteenOsaDto update(@PathVariable("id") final Long id, @RequestBody PerusteenOsaDto perusteenOsa) {
        LOG.info("save {}", perusteenOsa);
        perusteenOsa.setId(id);
        return service.update(id, perusteenOsa);
    }

    @RequestMapping(value = "/{id}", method = DELETE, consumes = "*/*")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final Long id) {
        LOG.info("delete {}", id);
        service.delete(id);
    }
    
    @RequestMapping(value = "/tyypit", method = GET)
    public List<String> getPerusteenOsaTypes() {
        return PerusteenOsaMappings.getPerusteenOsaTypes();
    }

    private HttpHeaders buildHeadersFor(Long id, UriComponentsBuilder ucb) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucb.path("/perusteenosat/{id}").buildAndExpand(id).toUri());
        return headers;
    }
}
