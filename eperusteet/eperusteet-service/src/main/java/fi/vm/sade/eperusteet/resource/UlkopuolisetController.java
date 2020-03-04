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

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.UlkopuolisetService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 *
 * @author nkala
 */
@RestController
@RequestMapping("/ulkopuoliset")
@Api("Ulkopuoliset")
@InternalApi
public class UlkopuolisetController {
    @Autowired
    private UlkopuolisetService service;

    @RequestMapping(value = "/organisaatioryhmat", method = GET)
    @ResponseBody
    public ResponseEntity<JsonNode> getOrganisaatioRyhmat() {
        JsonNode ryhmat = service.getRyhmat();
        return new ResponseEntity<>(ryhmat, HttpStatus.OK);
    }

    @RequestMapping(value = "/organisaatioryhmat/{oid}", method = GET)
    @ResponseBody
    public ResponseEntity<JsonNode> getOrganisaatioRyhmatByOid(@PathVariable(value = "oid") final String oid) {
        JsonNode ryhma = service.getRyhma(oid);
        return new ResponseEntity<>(ryhma, HttpStatus.OK);
    }
}
