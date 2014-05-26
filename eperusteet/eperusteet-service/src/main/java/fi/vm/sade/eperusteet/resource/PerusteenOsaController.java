package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TutkinnonOsa;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.resource.util.PerusteenOsaMappings;
import fi.vm.sade.eperusteet.service.LockManager;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.exception.LockException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Controller
@RequestMapping("/perusteenosat")
public class PerusteenOsaController {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteenOsaController.class);

    @Autowired
    private PerusteenOsaService service;

    @Autowired
    private LockManager lockManager;

    @RequestMapping(method = GET)
    @ResponseBody
    public List<? extends PerusteenOsaDto> getAll() {
        LOG.info("FINDALL");
        return service.getAll();
    }

    @RequestMapping(method = GET, params = "nimi")
    @ResponseBody
    public List<? extends PerusteenOsaDto> getAllWithName(@RequestParam("nimi") final String name) {
    	LOG.debug("find with nimi: {}", name);
    	return service.getAllWithName(name);
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
    public ResponseEntity<List<PerusteenOsaDto>> get(@PathVariable("koodiUri") final String koodiUri) {
    	LOG.info("get by koodi {}", koodiUri);
    	List<PerusteenOsaDto> t = service.getAllByKoodiUri(koodiUri);
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
        if (!lockManager.isLockedByAuthenticatedUser(id)) {
            throw new LockException("lukitus-vaaditaan");
        }
        tekstiKappaleDto.setId(id);
        return service.update(tekstiKappaleDto, TekstiKappaleDto.class, TekstiKappale.class);
    }

    @RequestMapping(value = "/{id}", method = POST, params = PerusteenOsaMappings.IS_TUTKINNON_OSA_PARAM)
    @ResponseBody
    public TutkinnonOsaDto update(@PathVariable("id") final Long id, @RequestBody TutkinnonOsaDto tutkinnonOsaDto) {
        LOG.info("update {}", tutkinnonOsaDto);
        if (!lockManager.isLockedByAuthenticatedUser(id)) {
            throw new LockException("lukitus-vaaditaan");
        }
        tutkinnonOsaDto.setId(id);
        return service.update(tutkinnonOsaDto, TutkinnonOsaDto.class, TutkinnonOsa.class);
    }

    @RequestMapping(value = "/{id}/lukko", method = GET)
    @ResponseBody
    public ResponseEntity<LukkoDto> checkLock(@PathVariable("id") final Long id) {
        LukkoDto lock = lockManager.getLock(id);
        return new ResponseEntity<>(lock, lock == null ? HttpStatus.OK : HttpStatus.PRECONDITION_FAILED);
    }

    @RequestMapping(value = "/{id}/lukko", method = {POST, PUT})
    @ResponseBody
    public ResponseEntity<LukkoDto> lock(@PathVariable("id") final Long id) {
        if (lockManager.lock(id)) {
            return new ResponseEntity<>(lockManager.getLock(id), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(lockManager.getLock(id), HttpStatus.CONFLICT);
        }
    }

    @RequestMapping(value = "/{id}/lukko", method = DELETE)
    @ResponseBody
    public void unlock(@PathVariable("id") final Long id) {
        lockManager.unlock(id);
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
