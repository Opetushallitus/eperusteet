package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.PerusteDto;
import fi.vm.sade.eperusteet.dto.PerusteQuery;
import fi.vm.sade.eperusteet.dto.PerusteenSisaltoViiteDto;
import fi.vm.sade.eperusteet.dto.PerusteenosaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.service.LockManager;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.exception.LockException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@Controller
@RequestMapping("/perusteet")
public class PerusteController {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteController.class);

    @Autowired
    private PerusteService service;

    @Autowired
    private PerusteenOsaViiteService PerusteenOsaViiteService;

    @Autowired
    private LockManager lockManager;

//    @RequestMapping(method = GET)
//    @ResponseBody
//    public ResponseEntity<Page<PerusteDto>> getAll(PerusteQuery pquery) {
//        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
//        return new ResponseEntity<>(service.findBy(p, pquery), ResponseHeaders.cacheHeaders(1, TimeUnit.MINUTES), HttpStatus.OK);
//    }

    @RequestMapping(method = GET)
    @ResponseBody
    public Page<PerusteDto> getAll(PerusteQuery pquery) {
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return service.findBy(p, pquery);
    }

    @RequestMapping(value = "/{id}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PerusteDto update(@PathVariable("id") final long id, @RequestBody PerusteDto perusteDto) {
        LOG.info("update {}", perusteDto);
        perusteDto = service.update(id, perusteDto);
        return perusteDto;
    }


    @RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<PerusteDto> get(@PathVariable("id") final long id) {
        PerusteDto t = service.get(id);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/rakenne", method = GET)
    @ResponseBody
    public ResponseEntity<RakenneModuuliDto> getRakenne(@PathVariable("id") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        return new ResponseEntity<>(service.getTutkinnonRakenne(id, Suoritustapakoodi.of(suoritustapakoodi)), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/tutkinnonosat", method = GET)
    @ResponseBody
    public List<TutkinnonOsaViiteDto> getTutkinnonOsat(@PathVariable("id") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        return service.getTutkinnonOsat(id, Suoritustapakoodi.of(suoritustapakoodi));
    }

    /**
     * Luo ja liittää uuden tutkinnon osa perusteeseen.
     * @param id
     * @param suoritustapakoodi
     * @param osa viitteen tiedot
     * @return Viite uuten tutkinnon osaan
     */
    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/tutkinnonosat", method = POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public TutkinnonOsaViiteDto addTutkinnonOsa(
        @PathVariable("id") final Long id,
        @PathVariable("suoritustapakoodi") final String suoritustapakoodi,
        @RequestBody TutkinnonOsaViiteDto osa) {
        return service.addTutkinnonOsa(id, Suoritustapakoodi.of(suoritustapakoodi), osa);
    }

    /**
     * Liitää olemassa olevan tutkinnon osan perusteeseen
     * @param id tutkinnon id
     * @param suoritustapakoodi suoritustapa (naytto,ops)
     * @param osa liitettävä tutkinnon osa
     * @return
     */
    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/tutkinnonosat", method = PUT)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public TutkinnonOsaViiteDto attachTutkinnonOsa(
        @PathVariable("id") final Long id,
        @PathVariable("suoritustapakoodi") final String suoritustapakoodi,
        @RequestBody TutkinnonOsaViiteDto osa) {
        return service.attachTutkinnonOsa(id, Suoritustapakoodi.of(suoritustapakoodi), osa);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/tutkinnonosat/{osanId}", method = POST)
    @ResponseBody
    public TutkinnonOsaViiteDto updateTutkinnonOsa(
        @PathVariable("id") final Long id,
        @PathVariable("suoritustapakoodi") final String suoritustapakoodi,
        @PathVariable("osanId") final Long osanId,
        @RequestBody TutkinnonOsaViiteDto osa) {
        osa.setId(osanId);
        return service.updateTutkinnonOsa(id, Suoritustapakoodi.of(suoritustapakoodi), osa);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/tutkinnonosat/{osanId}", method = DELETE)
    @ResponseBody
    public void removeTutkinnonOsa(
        @PathVariable("id") final Long id,
        @PathVariable("suoritustapakoodi") final String suoritustapakoodi,
        @PathVariable("osanId") final Long osanId) {
        service.removeTutkinnonOsa(id, Suoritustapakoodi.of(suoritustapakoodi), osanId);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/rakenne", method = POST)
    @ResponseBody
    public RakenneModuuliDto updatePerusteenRakenne(@PathVariable("id") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi, @RequestBody RakenneModuuliDto rakenne) {
        PerusteenosaViiteDto ss = service.getSuoritustapaSisalto(id, Suoritustapakoodi.of(suoritustapakoodi));
        Long sid = ss.getId();
        if (!lockManager.isLockedByAuthenticatedUser(sid)) {
            throw new LockException("lukitus-vaaditaan");
        }
        return service.updateTutkinnonRakenne(id, Suoritustapakoodi.of(suoritustapakoodi), rakenne);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/lukko", method = GET)
    @ResponseBody
    public ResponseEntity<LukkoDto> checkLock(@PathVariable("id") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        PerusteenosaViiteDto ss = service.getSuoritustapaSisalto(id, Suoritustapakoodi.of(suoritustapakoodi));
        Long sid = ss.getId();
        LukkoDto lock = lockManager.getLock(sid);
        return new ResponseEntity<>(lock, lock == null ? HttpStatus.OK : HttpStatus.PRECONDITION_FAILED);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/lukko", method = {POST, PUT})
    @ResponseBody
    public ResponseEntity<LukkoDto> lock(@PathVariable("id") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        PerusteenosaViiteDto ss = service.getSuoritustapaSisalto(id, Suoritustapakoodi.of(suoritustapakoodi));
        Long sid = ss.getId();
        if (lockManager.lock(sid)) {
            return new ResponseEntity<>(lockManager.getLock(sid), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(lockManager.getLock(sid), HttpStatus.CONFLICT);
        }
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/lukko", method = DELETE)
    @ResponseBody
    public void unlock(@PathVariable("id") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        PerusteenosaViiteDto ss = service.getSuoritustapaSisalto(id, Suoritustapakoodi.of(suoritustapakoodi));
        lockManager.unlock(ss.getId());
    }

    /**
     * Luo perusteeseen suoritustavan alle tyhjän perusteenosan
     *
     * @param perusteId
     * @param suoritustapa
     * @return Luodun perusteenOsaViite entityReferencen
     */
    @RequestMapping(value = "/{perusteId}/suoritustavat/{suoritustapa}/sisalto", method = POST)
    @ResponseBody
    public ResponseEntity<PerusteenSisaltoViiteDto> addSisalto(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapa) {
        return new ResponseEntity<>(service.addSisalto(perusteId, Suoritustapakoodi.of(suoritustapa), null), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{perusteId}/suoritustavat/{suoritustapa}/sisalto", method = PUT)
    @ResponseBody
    public ResponseEntity<PerusteenSisaltoViiteDto> addSisaltoViite(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapa,
        @RequestBody PerusteenSisaltoViiteDto sisaltoViite) {
        return new ResponseEntity<>(service.addSisalto(perusteId, Suoritustapakoodi.of(suoritustapa), sisaltoViite), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{perusteId}/suoritustavat/{suoritustapa}/sisalto/{perusteenosaViiteId}/lapsi", method = POST)
    @ResponseBody
    public ResponseEntity<PerusteenSisaltoViiteDto> addSisaltoLapsi(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapa,
        @PathVariable("perusteenosaViiteId") final Long perusteenosaViiteId) {
        return new ResponseEntity<>(service.addSisaltoLapsi(perusteId, perusteenosaViiteId), HttpStatus.CREATED);
    }


    @RequestMapping(value = "/{perusteId}/suoritustavat/{suoritustapakoodi}", method = GET)
    @ResponseBody
    public ResponseEntity<PerusteenosaViiteDto> getSuoritustapaSisalto(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {

        PerusteenosaViiteDto dto = service.getSuoritustapaSisalto(perusteId, Suoritustapakoodi.of(suoritustapakoodi));
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/lammitys", method = GET)
    @ResponseBody
    public ResponseEntity<String> lammitys() {

        return new ResponseEntity<>(service.lammitys(), HttpStatus.OK);
    }

}