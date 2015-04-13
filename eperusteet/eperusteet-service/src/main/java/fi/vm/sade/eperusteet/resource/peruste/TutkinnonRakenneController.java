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

import com.google.common.base.Supplier;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.kayttaja.HenkiloTietoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.dto.util.TutkinnonOsaViiteUpdateDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.resource.util.CacheControl;
import fi.vm.sade.eperusteet.resource.util.CacheableResponse;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import java.util.ArrayList;
import java.util.List;
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

import static fi.vm.sade.eperusteet.resource.util.Etags.eTagHeader;
import static fi.vm.sade.eperusteet.resource.util.Etags.revisionOf;
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
@InternalApi
public class TutkinnonRakenneController {

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;
    @Autowired
    private PerusteService perusteService;
    @Autowired
    private KayttajanTietoService kayttajanTietoService;

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
    public TutkinnonOsaViiteDto addTutkinnonOsa(@PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final Suoritustapakoodi suoritustapakoodi, @RequestBody TutkinnonOsaViiteDto osa) {
        if (osa.getTutkinnonOsa() != null) {
            return perusteService.attachTutkinnonOsa(id, suoritustapakoodi, osa);
        }
        return perusteService.addTutkinnonOsa(id, suoritustapakoodi, osa);
    }

    /**
     * Liitää olemassa olevan tutkinnon osan perusteeseen
     *
     * @param id tutkinnon id
     * @param suoritustapakoodi suoritustapa (naytto,ops)
     * @param osa liitettävä tutkinnon osa
     */
    @RequestMapping(value = "/tutkinnonosat", method = PUT)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public TutkinnonOsaViiteDto attachTutkinnonOsa(@PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final Suoritustapakoodi suoritustapakoodi, @RequestBody TutkinnonOsaViiteDto osa) {
        return perusteService.attachTutkinnonOsa(id, suoritustapakoodi, osa);
    }

    @RequestMapping(value = "/rakenne", method = GET)
    @ResponseBody
    public ResponseEntity<RakenneModuuliDto> getRakenne(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final Suoritustapakoodi suoritustapakoodi, @RequestHeader(value = "If-None-Match", required = false) String eTag) {
        Integer revisio = revisionOf(eTag);
        RakenneModuuliDto rakenne = perusteService.getTutkinnonRakenne(id, suoritustapakoodi, revisio);
        if (rakenne == null) {
            return new ResponseEntity<>(eTagHeader(revisio), HttpStatus.NOT_MODIFIED);
        }
        return new ResponseEntity<>(rakenne, eTagHeader(rakenne.getVersioId()), HttpStatus.OK);
    }

    @RequestMapping(value = "/rakenne/versio/{versioId}", method = GET)
    @ResponseBody
    @CacheControl(age = CacheControl.ONE_YEAR)
    public ResponseEntity<RakenneModuuliDto> getRakenneVersio(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final Suoritustapakoodi suoritustapakoodi, @PathVariable("versioId") final Integer versioId) {
        RakenneModuuliDto t = perusteService.getRakenneVersio(id, suoritustapakoodi, versioId);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/rakenne/versiot", method = GET)
    @ResponseBody
    public List<CombinedDto<Revision, HenkiloTietoDto>> getRakenneVersiot(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {
        List<Revision> versiot = perusteService.getRakenneVersiot(id, Suoritustapakoodi.of(suoritustapakoodi));
        List<CombinedDto<Revision, HenkiloTietoDto>> laajennetut = new ArrayList<>();
        for (Revision r : versiot) {
            laajennetut.add(new CombinedDto<>(r, new HenkiloTietoDto(kayttajanTietoService.hae(r.getMuokkaajaOid()))));
        }
        return laajennetut;
    }

    @RequestMapping(value = "/tutkinnonosat", method = GET)
    @ResponseBody
    public ResponseEntity<List<TutkinnonOsaViiteDto>> getTutkinnonOsat(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final Suoritustapakoodi suoritustapakoodi) {
        return CacheableResponse.create(perusteService.getLastModifiedRevision(id), 1, new Supplier<List<TutkinnonOsaViiteDto>>() {
            @Override
            public List<TutkinnonOsaViiteDto> get() {
                return perusteService.getTutkinnonOsat(id, suoritustapakoodi);
            }

        });
    }

    @RequestMapping(value = "/tutkinnonosat/versiot/{versio}", method = GET)
    @ResponseBody
    public List<TutkinnonOsaViiteDto> getTutkinnonOsat(
        @PathVariable("perusteId") final Long id,
        @PathVariable("suoritustapakoodi") final Suoritustapakoodi suoritustapakoodi,
        @PathVariable("versio") final Integer versio) {
        return perusteService.getTutkinnonOsat(id, suoritustapakoodi, versio);
    }

    @RequestMapping(value = "/tutkinnonosat/{osanId}/muokattavakopio", method = POST)
    public TutkinnonOsaViiteDto kloonaaTutkinnonOsa(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapakoodi") final Suoritustapakoodi suoritustapakoodi,
        @PathVariable("osanId") final Long id) {
        return perusteenOsaViiteService.kloonaaTutkinnonOsa(perusteId, suoritustapakoodi, id);
    }

    @RequestMapping(value = "/tutkinnonosat/{osanId}", method = DELETE)
    @ResponseBody
    public void removeTutkinnonOsa(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final Suoritustapakoodi suoritustapakoodi, @PathVariable("osanId") final Long osanId) {
        perusteService.removeTutkinnonOsa(id, suoritustapakoodi, osanId);
    }

    @RequestMapping(value = "/rakenne/palauta/{versioId}", method = POST)
    @ResponseBody
    @CacheControl(age = CacheControl.ONE_YEAR)
    public ResponseEntity<RakenneModuuliDto> revertRakenneVersio(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final Suoritustapakoodi suoritustapakoodi, @PathVariable("versioId") final Integer versioId) {
        RakenneModuuliDto t = perusteService.revertRakenneVersio(id, suoritustapakoodi, versioId);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/rakenne", method = POST)
    @ResponseBody
    public RakenneModuuliDto updatePerusteenRakenne(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final Suoritustapakoodi suoritustapakoodi, @RequestBody UpdateDto<RakenneModuuliDto> rakenne) {
        return perusteService.updateTutkinnonRakenne(id, suoritustapakoodi, rakenne);
    }

    @RequestMapping(value = "/tutkinnonosat/{osanId}", method = POST)
    @ResponseBody
    public TutkinnonOsaViiteDto updateTutkinnonOsa(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final Suoritustapakoodi suoritustapakoodi, @PathVariable("osanId") final Long osanId, @RequestBody TutkinnonOsaViiteUpdateDto osa) {
        return perusteService.updateTutkinnonOsa(id, suoritustapakoodi, osa);
    }

    @RequestMapping(value = "/tutkinnonosat/{viiteId}", method = GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public TutkinnonOsaViiteDto getTutkinnonOsaViite(
        @PathVariable("perusteId") final Long id, @PathVariable("suoritustapakoodi") final Suoritustapakoodi suoritustapakoodi, @PathVariable("viiteId") final Long viiteId) {
        return perusteService.getTutkinnonOsaViite(id, suoritustapakoodi, viiteId);
    }

}
