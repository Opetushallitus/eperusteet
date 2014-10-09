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
import com.mangofactory.swagger.annotations.ApiIgnore;
import fi.vm.sade.eperusteet.service.UlkopuolisetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author nkala
 */
@Controller
@RequestMapping("/ulkopuoliset")
@ApiIgnore
public class UlkopuolisetController {
    @Autowired
    private UlkopuolisetService service;

    @RequestMapping(value = "/organisaatioryhmat", method = GET)
    @ResponseBody
    public ResponseEntity<JsonNode> getOrganisaatioRyhmat() {
        JsonNode ryhmat = service.getRyhmat();
        return new ResponseEntity<>(ryhmat, HttpStatus.OK);
    }
}
