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
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Puu;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.resource.util.CacheableResponse;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping("/perusteet/{perusteId}/suoritustavat/{suoritustapa}")
@InternalApi
@Api(value = "Sisallot")
public class PerusteenSisaltoController {

    @Autowired
    private PerusteService service;

    @Autowired
    private PerusteenOsaViiteService viiteService;

    /**
     * Luo perusteeseen suoritustavan alle uuden perusteenosan
     *
     * @param perusteId perusteId
     * @param suoritustapa suoritustapa
     * @param dto perusteenosaviite (valinnainen, luodaan tyhjänä jos puuttuu)
     * @return Luodun perusteenOsaViite referencen
     */
    @RequestMapping(value = "/sisalto", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public PerusteenOsaViiteDto.Matala addSisaltoUUSI(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapa,
        @RequestBody(required = false) final PerusteenOsaViiteDto.Matala dto) {
        return service.addSisaltoUUSI(perusteId, Suoritustapakoodi.of(suoritustapa), dto);
    }

    @RequestMapping(value = "/sisalto", method = PUT)
    @ResponseStatus(HttpStatus.CREATED)
    public PerusteenOsaViiteDto.Matala addSisaltoViiteUUSI(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapa,
        @RequestBody final PerusteenOsaViiteDto.Matala dto
    ) {
        return service.addSisaltoUUSI(perusteId, Suoritustapakoodi.of(suoritustapa), dto);
    }

    @RequestMapping(value = "/sisalto/{perusteenosaViiteId}/lapsi", method = POST)
    public ResponseEntity<PerusteenOsaViiteDto.Matala> addSisaltoLapsi(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapa,
        @PathVariable("perusteenosaViiteId") final Long perusteenosaViiteId) {
        return new ResponseEntity<>(service.addSisaltoLapsi(perusteId, perusteenosaViiteId, null), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/sisalto/{parentId}/lapsi/viitteella", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public PerusteenOsaViiteDto.Matala addSisaltoUusiLapsiViitteella(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("suoritustapa") final String suoritustapa,
            @PathVariable("parentId") final Long parentId,
            @RequestBody(required = false) final PerusteenOsaViiteDto.Matala dto) {
        return service.addSisaltoLapsi(perusteId, parentId, dto);
    }

    @RequestMapping(value = "/sisalto/{parentId}/lapsi/{childId}", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public PerusteenOsaViiteDto.Matala addSisaltoUusiLapsi(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapa,
        @PathVariable("parentId") final Long parentId,
        @PathVariable("childId") final Long childId) {
        PerusteenOsaViiteDto.Matala viite = new PerusteenOsaViiteDto.Matala();
        viite.setPerusteenOsaRef(new Reference(childId));
        return service.addSisaltoLapsi(perusteId, parentId, viite);
    }

    @RequestMapping(value = "/sisalto", method = GET)
    public ResponseEntity<PerusteenOsaViiteDto<?>> getSuoritustapaSisaltoUUSI(
        @RequestParam(value = "muoto", required = false, defaultValue = "suppea") final String view,
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final Suoritustapakoodi suoritustapakoodi
        ) {
        return CacheableResponse.create(service.getPerusteVersion(perusteId), 1, new Supplier<PerusteenOsaViiteDto<?>>() {
            @Override
            public PerusteenOsaViiteDto<?> get() {
                Class<? extends Puu> puuClz = "suppea".equals(view) ? PerusteenOsaViiteDto.Suppea.class : PerusteenOsaViiteDto.Laaja.class;
                return service
                    .getSuoritustapaSisalto(
                            perusteId,
                            suoritustapakoodi,
                            puuClz);
            }
        });
    }

    @RequestMapping(value = "/sisalto/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSisaltoViite(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final Suoritustapakoodi suoritustapakoodi,
        @PathVariable("id") final Long id) {
        viiteService.removeSisalto(perusteId, id);
    }

    @RequestMapping(value = "/sisalto/{id}", method = {POST})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSisaltoViiteWithPost(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final Suoritustapakoodi suoritustapakoodi,
        @PathVariable("id") final Long id,
        @RequestBody final fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Laaja pov) {
        viiteService.reorderSubTree(perusteId, id, pov);
    }

    @RequestMapping(value = "/sisalto/{id}", method = {PUT})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSisaltoViiteWithPut(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("suoritustapa") final Suoritustapakoodi suoritustapakoodi,
            @PathVariable("id") final Long id,
            @RequestBody final fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Laaja pov) {
        viiteService.reorderSubTree(perusteId, id, pov);
    }

    @RequestMapping(value = "/sisalto/{id}/muokattavakopio", method = POST)
    public PerusteenOsaViiteDto.Laaja kloonaaTekstiKappale(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final Suoritustapakoodi suoritustapakoodi,
        @PathVariable("id") final Long id) {
       return viiteService.kloonaaTekstiKappale(perusteId, id);
    }

}
