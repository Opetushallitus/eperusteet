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
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
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
@ApiIgnore
public class PerusteenSisaltoController {

    @Autowired
    private PerusteService service;

    @Autowired
    private PerusteenOsaViiteService viiteService;

    /**
     * Luo perusteeseen suoritustavan alle uuden perusteenosan
     *
     * @param perusteId
     * @param suoritustapa
     * @param dto perusteenosaviite (valinnainen, luodaan tyhjänä jos puuttuu)
     * @return Luodun perusteenOsaViite entityReferencen
     */
    @RequestMapping(value = "/sisalto", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public PerusteenOsaViiteDto.Matala addSisalto(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapa,
        @RequestBody(required = false) final PerusteenOsaViiteDto.Matala dto
    ) {
        if ( dto == null || (dto.getPerusteenOsaRef() == null && dto.getPerusteenOsa() == null)) {
            return service.addSisalto(perusteId, Suoritustapakoodi.of(suoritustapa), null);
        } else {
            return addSisaltoViite(perusteId, suoritustapa, dto);
        }
    }

    @RequestMapping(value = "/sisalto/UUSI", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public PerusteenOsaViiteDto.Matala addSisaltoUUSI(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapa,
        @RequestBody(required = false) final PerusteenOsaViiteDto.Matala dto)
    {
        if ( dto == null || (dto.getPerusteenOsaRef() == null && dto.getPerusteenOsa() == null)) {
            return service.addSisaltoUUSI(perusteId, Suoritustapakoodi.of(suoritustapa), null);
        } else {
            return addSisaltoViite(perusteId, suoritustapa, dto);
        }
    }

    @RequestMapping(value = "/sisalto", method = PUT)
    @ResponseStatus(HttpStatus.CREATED)
    public PerusteenOsaViiteDto.Matala addSisaltoViite(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapa,
        @RequestBody final PerusteenOsaViiteDto.Matala dto
    ) {
        return service.addSisalto(perusteId, Suoritustapakoodi.of(suoritustapa), dto);
    }

    @RequestMapping(value = "/sisalto/{perusteenosaViiteId}/lapsi", method = POST)
    public ResponseEntity<PerusteenOsaViiteDto.Matala> addSisaltoLapsi(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapa,
        @PathVariable("perusteenosaViiteId") final Long perusteenosaViiteId) {
        return new ResponseEntity<>(service.addSisaltoLapsi(perusteId, perusteenosaViiteId, null), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/sisalto/{parentId}/lapsi/{childId}", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public PerusteenOsaViiteDto.Matala addSisaltoLapsi(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapa,
        @PathVariable("parentId") final Long parentId,
        @PathVariable("childId") final Long childId) {
        PerusteenOsaViiteDto.Matala viite = new PerusteenOsaViiteDto.Matala();
        viite.setPerusteenOsaRef(new EntityReference(childId));
        return service.addSisaltoLapsi(perusteId, parentId, viite);
    }

    @RequestMapping(value = "/sisalto", method = GET)
    public ResponseEntity<PerusteenOsaViiteDto<?>> getSuoritustapaSisalto(
        @RequestParam(value = "muoto", required = false, defaultValue = "suppea") String view,
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final Suoritustapakoodi suoritustapakoodi) {

        PerusteenOsaViiteDto<?> dto = service.getSuoritustapaSisalto(perusteId, suoritustapakoodi, "suppea".equals(view)
                                                                     ? PerusteenOsaViiteDto.Suppea.class : PerusteenOsaViiteDto.Laaja.class);
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<PerusteenOsaViiteDto<?>>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/sisalto/UUSI", method = GET)
    public ResponseEntity<PerusteenOsaViiteDto<?>> getSuoritustapaSisaltoUUSI(
        @RequestParam(value = "muoto", required = false, defaultValue = "suppea") String view,
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final Suoritustapakoodi suoritustapakoodi) {

        PerusteenOsaViiteDto<?> dto = service.getSuoritustapaSisaltoUUSI(perusteId, suoritustapakoodi, "suppea".equals(view)
                                                                     ? PerusteenOsaViiteDto.Suppea.class : PerusteenOsaViiteDto.Laaja.class);
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<PerusteenOsaViiteDto<?>>(dto, HttpStatus.OK);
    }


    @RequestMapping(value = "/sisalto/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSisaltoViite(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {
        viiteService.removeSisalto(perusteId, id);
    }

    @RequestMapping(value = "/sisalto/{id}", method = POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSisaltoViite(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id,
        @RequestBody final fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Laaja pov) {
        viiteService.reorderSubTree(perusteId, id, pov);
    }

    @RequestMapping(value = "/sisalto/{id}/muokattavakopio", method = POST)
    public PerusteenOsaViiteDto.Laaja kloonaaTekstiKappale(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {
        return viiteService.kloonaaTekstiKappale(perusteId, id);
    }

}
