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
package fi.vm.sade.eperusteet.resource.peruste;

import com.mangofactory.swagger.annotations.ApiIgnore;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.resource.util.CacheControl;
import fi.vm.sade.eperusteet.resource.util.PerusteenOsaMappings;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 *
 * @author jhyoty
 */
@RestController
@RequestMapping("/perusteet/{perusteId}/suoritustavat/{suoritustapakoodi}")
@ApiIgnore
public class TutkinnonRakenneController {

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;
    @Autowired
    private PerusteService perusteService;

    /**
     * Luo ja liittää uuden tutkinnon osa perusteeseen.
     *
     * @param id
     * @param suoritustapakoodi
     * @param osa viitteen tiedot
     * @return Viite uuten tutkinnon osaan
     */
    @RequestMapping(value = "/tutkinnonosat", method = POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public TutkinnonOsaViiteDto addTutkinnonOsa(@PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi, @RequestBody TutkinnonOsaViiteDto osa) {
        if (osa.getTutkinnonOsa() != null) {
            return perusteService.attachTutkinnonOsa(id, Suoritustapakoodi.of(suoritustapakoodi), osa);
        }
        return perusteService.addTutkinnonOsa(id, Suoritustapakoodi.of(suoritustapakoodi), osa);
    }

    /**
     * Liitää olemassa olevan tutkinnon osan perusteeseen
     *
     * @param id tutkinnon id
     * @param suoritustapakoodi suoritustapa (naytto,ops)
     * @param osa liitettävä tutkinnon osa
     * @return
     */
    @RequestMapping(value = "/tutkinnonosat", method = PUT)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public TutkinnonOsaViiteDto attachTutkinnonOsa(@PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi, @RequestBody TutkinnonOsaViiteDto osa) {
        return perusteService.attachTutkinnonOsa(id, Suoritustapakoodi.of(suoritustapakoodi), osa);
    }

    @RequestMapping(value = "/lukko/tutkinnonosat", method = GET)
    @ResponseBody
    public ResponseEntity<Map<Long, LukkoDto>> getLocksTutkinnonOsat(@PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        return new ResponseEntity<>(perusteService.getLocksTutkinnonOsat(id, Suoritustapakoodi.of(suoritustapakoodi)), HttpStatus.OK);
    }

    @RequestMapping(value = "/rakenne", method = GET)
    @ResponseBody
    public ResponseEntity<RakenneModuuliDto> getRakenne(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi, @RequestHeader(value = "If-None-Match", required = false) Integer eTag, HttpServletResponse response) {
        RakenneModuuliDto rakenne = perusteService.getTutkinnonRakenne(id, Suoritustapakoodi.of(suoritustapakoodi), eTag);

        if (rakenne == null) {
            response.addHeader("ETag", eTag.toString());
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
        response.addHeader("ETag", rakenne.getVersioId().toString());
        return new ResponseEntity<>(rakenne, HttpStatus.OK);
    }

    @RequestMapping(value = "/rakenne/versio/{versioId}", method = GET)
    @ResponseBody
    @CacheControl(age = CacheControl.ONE_YEAR)
    public ResponseEntity<RakenneModuuliDto> getRakenneVersio(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi, @PathVariable("versioId") final Integer versioId) {
        RakenneModuuliDto t = perusteService.getRakenneVersio(id, Suoritustapakoodi.of(suoritustapakoodi), versioId);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/rakenne/versiot", method = GET)
    @ResponseBody
    public List<Revision> getRakenneVersiot(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        return perusteService.getRakenneVersiot(id, Suoritustapakoodi.of(suoritustapakoodi));
    }

    @RequestMapping(value = "/tutkinnonosat", method = GET)
    @ResponseBody
    public List<TutkinnonOsaViiteDto> getTutkinnonOsat(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        return perusteService.getTutkinnonOsat(id, Suoritustapakoodi.of(suoritustapakoodi));
    }

    @RequestMapping(value = "/tutkinnonosat/{osanId}/muokattavakopio", method = POST, params = PerusteenOsaMappings.IS_TUTKINNON_OSA_PARAM)
    public TutkinnonOsaViiteDto kloonaaTutkinnonOsa(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapakoodi") final String suoritustapakoodi,
        @PathVariable("osanId") final Long id) {
        return perusteenOsaViiteService.kloonaaTutkinnonOsa(perusteId, Suoritustapakoodi.of(suoritustapakoodi), id);
    }

    @RequestMapping(value = "/tutkinnonosat/{osanId}", method = DELETE)
    @ResponseBody
    public void removeTutkinnonOsa(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi, @PathVariable("osanId") final Long osanId) {
        perusteService.removeTutkinnonOsa(id, Suoritustapakoodi.of(suoritustapakoodi), osanId);
    }

    @RequestMapping(value = "/rakenne/palauta/{versioId}", method = POST)
    @ResponseBody
    @CacheControl(age = CacheControl.ONE_YEAR)
    public ResponseEntity<RakenneModuuliDto> revertRakenneVersio(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi, @PathVariable("versioId") final Integer versioId) {
        RakenneModuuliDto t = perusteService.revertRakenneVersio(id, Suoritustapakoodi.of(suoritustapakoodi), versioId);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/rakenne", method = POST)
    @ResponseBody
    public RakenneModuuliDto updatePerusteenRakenne(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi, @RequestBody UpdateDto<RakenneModuuliDto> rakenne) {
        return perusteService.updateTutkinnonRakenne(id, Suoritustapakoodi.of(suoritustapakoodi), rakenne);
    }

    @RequestMapping(value = "/tutkinnonosat/{osanId}", method = POST)
    @ResponseBody
    public TutkinnonOsaViiteDto updateTutkinnonOsa(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi, @PathVariable("osanId") final Long osanId, @RequestBody TutkinnonOsaViiteDto osa) {
        osa.setId(osanId);
        return perusteService.updateTutkinnonOsa(id, Suoritustapakoodi.of(suoritustapakoodi), osa);
    }

}
