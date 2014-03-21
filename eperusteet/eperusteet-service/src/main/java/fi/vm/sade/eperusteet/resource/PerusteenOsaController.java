package fi.vm.sade.eperusteet.resource;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriComponentsBuilder;

import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TutkinnonOsa;
import fi.vm.sade.eperusteet.dto.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.resource.util.PerusteenOsaMappings;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;

@Controller
@RequestMapping("/api/perusteenosat")
public class PerusteenOsaController {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteenOsaController.class);
    
    @Autowired
    private PerusteenOsaService service;
    
    @RequestMapping(method = GET)
    @ResponseBody
    public List<? extends PerusteenOsaDto> getAll() {
        LOG.info("FINDALL");
        return service.getAll();
    }

    @RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<PerusteenOsaDto> get(@PathVariable("id") final Long id) {
        LOG.info("get {}", id);
    	PerusteenOsaDto t = service.get(id);
        if (t == null) {
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/{id}/revisions", method = GET)
    @ResponseBody
    public List<Revision> getRevisions(@PathVariable("id") final Long id) {
    	LOG.debug("get revisions");
    	return service.getRevisions(id);
    }
    
    @RequestMapping(value = "/{id}/revisions/{revisionId}", method = GET)
    @ResponseBody
    public ResponseEntity<PerusteenOsaDto> getRevision(@PathVariable("id") final Long id, @PathVariable("revisionId") final Integer revisionId) {
    	LOG.debug("get #{} revision #{}", id, revisionId);
    	PerusteenOsaDto t = service.getRevision(id, revisionId);
        if (t == null) {
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/{koodiUri}", method = GET, params = "koodi=true")
    @ResponseBody
    public ResponseEntity<PerusteenOsaDto> get(@PathVariable("koodiUri") final String koodiUri) {
    	LOG.info("get by koodi {}", koodiUri);
    	PerusteenOsaDto t = service.getByKoodiUri(koodiUri);
        if (t == null) {
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(method = POST, params = PerusteenOsaMappings.IS_TUTKINNON_OSA_PARAM)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<TutkinnonOsaDto> add(@RequestBody TutkinnonOsaDto tutkinnonOsaDto, UriComponentsBuilder ucb) {
        LOG.info("add {}", tutkinnonOsaDto);
        tutkinnonOsaDto = service.save(tutkinnonOsaDto, TutkinnonOsaDto.class, TutkinnonOsa.class);
        return new ResponseEntity<>(tutkinnonOsaDto, buildHeadersFor(tutkinnonOsaDto.getId(), ucb), HttpStatus.CREATED);
    }
    
    @RequestMapping(method = POST, params = PerusteenOsaMappings.IS_TEKSTIKAPPALE_PARAM)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<TekstiKappaleDto> add(@RequestBody TekstiKappaleDto tekstikappaleDto, UriComponentsBuilder ucb) {
        LOG.info("add {}", tekstikappaleDto);
        tekstikappaleDto = service.save(tekstikappaleDto, TekstiKappaleDto.class, TekstiKappale.class);
        return new ResponseEntity<>(tekstikappaleDto, buildHeadersFor(tekstikappaleDto.getId(), ucb), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}", method = POST, params = PerusteenOsaMappings.IS_TEKSTIKAPPALE_PARAM)
    @ResponseBody
    public TekstiKappaleDto update(@PathVariable("id") final Long id, @RequestBody TekstiKappaleDto tekstiKappaleDto) {
        LOG.info("update {}", tekstiKappaleDto);
        tekstiKappaleDto.setId(id);
        return service.update(tekstiKappaleDto, TekstiKappaleDto.class, TekstiKappale.class);
    }
    
    @RequestMapping(value = "/{id}", method = POST, params = PerusteenOsaMappings.IS_TUTKINNON_OSA_PARAM)
    @ResponseBody
    public TutkinnonOsaDto update(@PathVariable("id") final Long id, @RequestBody TutkinnonOsaDto tutkinnonOsaDto) {
        LOG.info("update {}", tutkinnonOsaDto);
        tutkinnonOsaDto.setId(id);
        return service.update(tutkinnonOsaDto, TutkinnonOsaDto.class, TutkinnonOsa.class);
    }
   
    @RequestMapping(value = "/{id}", method = DELETE, consumes = "*/*")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void delete(@PathVariable final Long id) {
        LOG.info("delete {}", id);
        service.delete(id);
    }
    
    @RequestMapping(value = "/tyypit", method = GET)
    @ResponseBody
    public List<String> getPerusteenOsaTypes() {
        return PerusteenOsaMappings.getPerusteenOsaTypes();
    }

    private HttpHeaders buildHeadersFor(Long id, UriComponentsBuilder ucb) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucb.path("/perusteenosat/{id}").buildAndExpand(id).toUri());
        return headers;
    }
}
