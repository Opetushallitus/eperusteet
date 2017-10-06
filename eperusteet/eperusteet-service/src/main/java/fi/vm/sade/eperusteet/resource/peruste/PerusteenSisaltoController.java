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
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Puu;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.resource.util.CacheableResponse;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.audit.EperusteetAudit;
import fi.vm.sade.eperusteet.service.audit.LogMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static fi.vm.sade.eperusteet.service.audit.EperusteetMessageFields.PERUSTEENOSAVIITE;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * @author jhyoty
 */
@RestController
@RequestMapping("/perusteet/{perusteId}/suoritustavat/{suoritustapa}")
@InternalApi
public class PerusteenSisaltoController {

    @Autowired
    private EperusteetAudit audit;

    @Autowired
    private PerusteService service;

    @Autowired
    private PerusteenOsaViiteService viiteService;

    /**
     * Luo perusteeseen suoritustavan alle uuden perusteenosan
     *
     * @param perusteId    perusteId
     * @param suoritustapa suoritustapa
     * @param dto          perusteenosaviite (valinnainen, luodaan tyhjänä jos puuttuu)
     * @return Luodun perusteenOsaViite entityReferencen
     */
    @RequestMapping(value = "/sisalto", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public PerusteenOsaViiteDto.Matala addSisaltoUUSI(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("suoritustapa") final String suoritustapa,
            @RequestBody(required = false) final PerusteenOsaViiteDto.Matala dto) {
        return audit.withAudit(LogMessage.builder(perusteId, PERUSTEENOSAVIITE, LISAYS),
                (Void) -> service.addSisaltoUUSI(perusteId, Suoritustapakoodi.of(suoritustapa), dto)
        );
    }

    @RequestMapping(value = "/sisalto", method = PUT)
    @ResponseStatus(HttpStatus.CREATED)
    public PerusteenOsaViiteDto.Matala addSisaltoViiteUUSI(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("suoritustapa") final String suoritustapa,
            @RequestBody final PerusteenOsaViiteDto.Matala dto
    ) {
        return audit.withAudit(LogMessage.builder(perusteId, PERUSTEENOSAVIITE, MUOKKAUS),
                (Void) -> service.addSisaltoUUSI(perusteId, Suoritustapakoodi.of(suoritustapa), dto));
    }

    @RequestMapping(value = "/sisalto/{perusteenosaViiteId}/lapsi", method = POST)
    public ResponseEntity<PerusteenOsaViiteDto.Matala> addSisaltoLapsi(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("suoritustapa") final String suoritustapa,
            @PathVariable("perusteenosaViiteId") final Long perusteenosaViiteId) {
        return audit.withAudit(LogMessage.builder(perusteId, PERUSTEENOSAVIITE, LIITOS),
                (Void) -> new ResponseEntity<>(service.addSisaltoLapsi(perusteId, perusteenosaViiteId, null), HttpStatus.CREATED));
    }

    @RequestMapping(value = "/sisalto/{parentId}/lapsi/{childId}", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public PerusteenOsaViiteDto.Matala addSisaltoLapsi(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("suoritustapa") final String suoritustapa,
            @PathVariable("parentId") final Long parentId,
            @PathVariable("childId") final Long childId) {
        return audit.withAudit(LogMessage.builder(perusteId, PERUSTEENOSAVIITE, LIITOS), (Void) -> {
            PerusteenOsaViiteDto.Matala viite = new PerusteenOsaViiteDto.Matala();
            viite.setPerusteenOsaRef(new EntityReference(childId));
            return service.addSisaltoLapsi(perusteId, parentId, viite);
        });
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
            @PathVariable("id") final Long id) {
        audit.withAudit(LogMessage.builder(perusteId, PERUSTEENOSAVIITE, POISTO), (Void) -> {
            viiteService.removeSisalto(perusteId, id);
            return null;
        });
    }


    @RequestMapping(value = "/sisalto/{id}", method = {POST, PUT})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSisaltoViite(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id,
            @RequestBody final fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Laaja pov) {
        audit.withAudit(LogMessage.builder(perusteId, PERUSTEENOSAVIITE, MUOKKAUS), (Void) -> {
            viiteService.reorderSubTree(perusteId, id, pov);
            return null;
        });
    }

    @RequestMapping(value = "/sisalto/{id}/muokattavakopio", method = POST)
    public PerusteenOsaViiteDto.Laaja kloonaaTekstiKappale(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id) {
        return audit.withAudit(LogMessage.builder(perusteId, PERUSTEENOSAVIITE, KLOONAUS),
                (Void) -> viiteService.kloonaaTekstiKappale(perusteId, id)
        );
    }

}
