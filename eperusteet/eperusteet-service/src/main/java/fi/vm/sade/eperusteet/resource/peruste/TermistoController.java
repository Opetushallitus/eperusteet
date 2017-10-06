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

import fi.vm.sade.eperusteet.dto.peruste.TermiDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.TermistoService;
import fi.vm.sade.eperusteet.service.audit.EperusteetAudit;

import static fi.vm.sade.eperusteet.service.audit.EperusteetMessageFields.TERMI;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.LISAYS;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.MUOKKAUS;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.POISTO;

import fi.vm.sade.eperusteet.service.audit.LogMessage;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author apvilkko
 */
@RestController
@RequestMapping("/perusteet/{perusteId}")
@InternalApi
public class TermistoController {

    @Autowired
    private EperusteetAudit audit;

    @Autowired
    private TermistoService termistoService;

    @RequestMapping(value = "/termisto", method = GET)
    public List<TermiDto> getAll(
            @PathVariable("perusteId") final Long perusteId) {
        return termistoService.getTermit(perusteId);
    }

    @RequestMapping(value = "/termisto", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public TermiDto addTermi(
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody TermiDto dto) {
        return audit.withAudit(LogMessage.builder(perusteId, TERMI, LISAYS), (Void) -> {
            dto.setId(null);
            return termistoService.addTermi(perusteId, dto);
        });
    }

    @RequestMapping(value = "/termisto/{id}", method = POST)
    public TermiDto updateTermi(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id,
            @RequestBody TermiDto dto) {
        return audit.withAudit(LogMessage.builder(perusteId, TERMI, MUOKKAUS), (Void) -> {
            dto.setId(id);
            return termistoService.updateTermi(perusteId, dto);
        });
    }

    @RequestMapping(value = "/termisto/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTermi(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id) {
        audit.withAudit(LogMessage.builder(perusteId, TERMI, POISTO), (Void) -> {
            termistoService.deleteTermi(perusteId, id);
            return null;
        });
    }
}
