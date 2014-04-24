package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.PerusteDto;
import fi.vm.sade.eperusteet.dto.PerusteQuery;
import fi.vm.sade.eperusteet.dto.PerusteenSisaltoViiteDto;
import fi.vm.sade.eperusteet.dto.PerusteenosaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonRakenneDto;
import fi.vm.sade.eperusteet.service.PerusteService;
import java.util.List;
import java.util.concurrent.TimeUnit;
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


@Controller
@RequestMapping("/api/perusteet")
public class PerusteController {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteController.class);

    @Autowired
    private PerusteService service;

    @RequestMapping(method = GET)
    @ResponseBody
    public ResponseEntity<Page<PerusteDto>> getAll(PerusteQuery pquery) {
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return new ResponseEntity<>(service.findBy(p, pquery), ResponseHeaders.cacheHeaders(1, TimeUnit.MINUTES), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<PerusteDto> get(@PathVariable("id") final long id) {
        PerusteDto t = service.get(id);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, ResponseHeaders.cacheHeaders(1, TimeUnit.SECONDS), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/rakenne", method = GET)
    @ResponseBody
    public ResponseEntity<TutkinnonRakenneDto> getRakenne(@PathVariable("id") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        return new ResponseEntity<>(service.getTutkinnonRakenne(id, Suoritustapakoodi.of(suoritustapakoodi)), ResponseHeaders.cacheHeaders(1, TimeUnit.SECONDS), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/tutkinnonosat", method = GET)
    @ResponseBody
    public ResponseEntity<List<TutkinnonOsaViiteDto>> getTutkinnonOsat(@PathVariable("id") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        return new ResponseEntity<>(service.getTutkinnonOsat(id, Suoritustapakoodi.of(suoritustapakoodi)), ResponseHeaders.cacheHeaders(1, TimeUnit.SECONDS), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/rakenne", method = POST)
    @ResponseBody
    public TutkinnonRakenneDto updatePerusteenRakenne(@PathVariable("id") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi, @RequestBody TutkinnonRakenneDto rakenne) {
        return service.updateTutkinnonRakenne(id, Suoritustapakoodi.of(suoritustapakoodi), rakenne);
    }

    /**
     * Luo perusteeseen suoritustavan alle tyhjän perusteenosan
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
