/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.resource;

import com.wordnik.swagger.annotations.Api;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteInfoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenSisaltoViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.resource.util.CacheControl;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@Controller
@RequestMapping("/perusteet")
@Api(value="Perusteet", description = "Perusteiden hallintaan liittyvät operaatiot")
public class PerusteController {

    @Autowired
    private PerusteService service;

    @Autowired
    private PerusteenOsaViiteService PerusteenOsaViiteService;

    @RequestMapping(value = "/info", method = GET)
    @ResponseBody
    public Page<PerusteInfoDto> getAllInfo(PerusteQuery pquery) {
        // Vain valmiita perusteita voi hakea tämän rajapinnan avulla
        pquery.setTila(PerusteTila.VALMIS.toString());
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return service.findByInfo(p, pquery);
    }

    @RequestMapping(method = GET)
    @ResponseBody
    public Page<PerusteDto> getAll(PerusteQuery pquery) {
        // Vain valmiita perusteita voi hakea tämän rajapinnan avulla
        pquery.setTila(PerusteTila.VALMIS.toString());
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return service.findBy(p, pquery);
    }

    @RequestMapping(value = "/{id}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PerusteDto update(@PathVariable("id") final long id, @RequestBody PerusteDto perusteDto) {
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

    @RequestMapping(value = "/{id}/kaikki", method = GET)
    @ResponseBody
    public ResponseEntity<PerusteKaikkiDto> getKokoSisalto(@PathVariable("id") final long id) {
        PerusteKaikkiDto kokoSisalto = service.getKokoSisalto(id);
        return new ResponseEntity(kokoSisalto, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/rakenne", method = GET)
    @ResponseBody
    public ResponseEntity<RakenneModuuliDto> getRakenne(@PathVariable("id") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi,
            @RequestHeader(value = "If-None-Match", required = false) Integer eTag, HttpServletResponse response) {
        RakenneModuuliDto rakenne = service.getTutkinnonRakenne(id, Suoritustapakoodi.of(suoritustapakoodi), eTag);

        if (rakenne == null) {
            response.addHeader("ETag", eTag.toString());
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
        response.addHeader("ETag", rakenne.getVersioId().toString());
        return new ResponseEntity<>(rakenne, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/rakenne/versiot", method = GET)
    @ResponseBody
    public List<Revision> getRakenneVersiot(@PathVariable("id") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        return service.getRakenneVersiot(id, Suoritustapakoodi.of(suoritustapakoodi));
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/rakenne/versio/{versioId}", method = GET)
    @ResponseBody
    @CacheControl(age = CacheControl.ONE_YEAR)
    public ResponseEntity<RakenneModuuliDto> getRakenneVersio(@PathVariable("id") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi,
            @PathVariable("versioId") final Integer versioId) {
        RakenneModuuliDto t = service.getRakenneVersio(id, Suoritustapakoodi.of(suoritustapakoodi), versioId);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/rakenne/palauta/{versioId}", method = POST)
    @ResponseBody
    @CacheControl(age = CacheControl.ONE_YEAR)
    public ResponseEntity<RakenneModuuliDto> revertRakenneVersio(
            @PathVariable("id") final Long id,
            @PathVariable("suoritustapakoodi") final String suoritustapakoodi,
            @PathVariable("versioId") final Integer versioId) {
        RakenneModuuliDto t = service.revertRakenneVersio(id, Suoritustapakoodi.of(suoritustapakoodi), versioId);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/rakenne", method = POST)
    @ResponseBody
    public RakenneModuuliDto updatePerusteenRakenne(
            @PathVariable("id") final Long id,
            @PathVariable("suoritustapakoodi") final String suoritustapakoodi,
            @RequestBody UpdateDto<RakenneModuuliDto> rakenne) {
        return service.updateTutkinnonRakenne(id, Suoritustapakoodi.of(suoritustapakoodi), rakenne);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/tutkinnonosat", method = GET)
    @ResponseBody
    public List<TutkinnonOsaViiteDto> getTutkinnonOsat(
            @PathVariable("id") final Long id,
            @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        return service.getTutkinnonOsat(id, Suoritustapakoodi.of(suoritustapakoodi));
    }


    /**
     * Luo ja liittää uuden tutkinnon osa perusteeseen.
     *
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
     *
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

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/lukko/tutkinnonosat", method = GET)
    @ResponseBody
    public ResponseEntity<Map<Long, LukkoDto>> getLocksTutkinnonOsat(
            @PathVariable("id") final Long id,
            @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        return new ResponseEntity<>(service.getLocksTutkinnonOsat(id, Suoritustapakoodi.of(suoritustapakoodi)), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/lukko/perusteenosat", method = GET)
    @ResponseBody
    public ResponseEntity<Map<Long, LukkoDto>> getLocksPerusteenosat(
            @PathVariable("id") final Long id,
            @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        return new ResponseEntity<>(service.getLocksPerusteenOsat(id, Suoritustapakoodi.of(suoritustapakoodi)), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/lukko/kaikki", method = GET)
    @ResponseBody
    public ResponseEntity<Map<Long, LukkoDto>> getLockAll(
            @PathVariable("id") final Long id,
            @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        Map<Long, LukkoDto> locks = service.getLocksTutkinnonOsat(id, Suoritustapakoodi.of(suoritustapakoodi));
        locks.putAll(service.getLocksPerusteenOsat(id, Suoritustapakoodi.of(suoritustapakoodi)));

        LukkoDto lockRakenne = service.getLock(id, Suoritustapakoodi.of(suoritustapakoodi));
        if (lockRakenne != null) {
            locks.put(id, lockRakenne);
        }
        return new ResponseEntity<>(locks, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/lukko", method = GET)
    @ResponseBody
    public ResponseEntity<LukkoDto> getLock(
            @PathVariable("id") final Long id,
            @PathVariable("suoritustapakoodi") final String suoritustapakoodi,
            @RequestHeader(value = "If-None-Match", required = false) Integer eTag, HttpServletResponse response) {
        LukkoDto lock = service.getLock(id, Suoritustapakoodi.of(suoritustapakoodi));
        return new ResponseEntity(lock, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/lukko", method = {POST, PUT})
    @ResponseBody
    public ResponseEntity<LukkoDto> lock(
            @PathVariable("id") final Long id,
            @PathVariable("suoritustapakoodi") final String suoritustapakoodi,
            @RequestHeader(value = "If-None-Match", required = false) Integer eTag, HttpServletResponse response) {
        RakenneModuuliDto rakenne = service.getTutkinnonRakenne(id, Suoritustapakoodi.of(suoritustapakoodi), eTag);
        if (rakenne == null) {
            response.addHeader("ETag", eTag.toString());
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
        response.addHeader("ETag", rakenne.getVersioId().toString());
        return new ResponseEntity<>(service.lock(id, Suoritustapakoodi.of(suoritustapakoodi)), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/suoritustavat/{suoritustapakoodi}/lukko", method = DELETE)
    @ResponseBody
    public void unlock(@PathVariable("id") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        service.unlock(id, Suoritustapakoodi.of(suoritustapakoodi));
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

//    @RequestMapping(value = "/{perusteId}/suoritustavat/{suoritustapa}/sisalto/{perusteenosaViiteId}/kloonaa", method = POST)
//    @ResponseBody
//    public PerusteenOsaViiteDto kloonaa(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("suoritustapa") final String suoritustapa,
//            @PathVariable("perusteenosaViiteId") final Long id) {
//        PerusteenOsaViiteDto re = PerusteenOsaViiteService.kloonaa(perusteId, suoritustapa, id);
//        return re;
//    }

    @RequestMapping(value = "/{perusteId}/suoritustavat/{suoritustapa}/sisalto/{perusteenosaViiteId}/lapsi", method = POST)
    @ResponseBody
    public ResponseEntity<PerusteenSisaltoViiteDto> addSisaltoLapsi(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("suoritustapa") final String suoritustapa,
            @PathVariable("perusteenosaViiteId") final Long perusteenosaViiteId) {
        return new ResponseEntity<>(service.addSisaltoLapsi(perusteId, perusteenosaViiteId), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{perusteId}/suoritustavat/{suoritustapa}/sisalto/{parentId}/lapsi/{childId}", method = POST)
    @ResponseBody
    public ResponseEntity<PerusteenSisaltoViiteDto> addSisaltoLapsi(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("suoritustapa") final String suoritustapa,
            @PathVariable("parentId") final Long parentId,
            @PathVariable("childId") final Long childId) {
        return new ResponseEntity<>(service.attachSisaltoLapsi(perusteId, parentId, childId), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{perusteId}/suoritustavat/{suoritustapakoodi}/sisalto", method = GET)
    @ResponseBody
    public ResponseEntity<PerusteenOsaViiteDto> getSuoritustapaSisalto(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {

        PerusteenOsaViiteDto dto = service.getSuoritustapaSisalto(perusteId, Suoritustapakoodi.of(suoritustapakoodi));
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/{perusteId}/suoritustavat/{suoritustapakoodi}", method = GET)
    @ResponseBody
    public ResponseEntity<SuoritustapaDto> getSuoritustapa(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {

        SuoritustapaDto dto = service.getSuoritustapa(perusteId, Suoritustapakoodi.of(suoritustapakoodi));
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
