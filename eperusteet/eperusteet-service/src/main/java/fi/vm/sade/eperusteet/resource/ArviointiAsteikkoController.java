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

import fi.vm.sade.eperusteet.dto.arviointi.ArviointiAsteikkoDto;
import fi.vm.sade.eperusteet.service.ArviointiAsteikkoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 *
 * @author jhyoty
 */
@RestController
@RequestMapping("/arviointiasteikot")
@Api(value="Arviointiasteikot")
public class ArviointiAsteikkoController {

    @Autowired
    ArviointiAsteikkoService service;


    @RequestMapping(method = GET)
    public List<ArviointiAsteikkoDto> getAll() {
        return service.getAll();
    }

    @RequestMapping(value = "/{id}", method = GET)
    public ResponseEntity<ArviointiAsteikkoDto> get(@PathVariable("id") final Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @RequestMapping(method = PUT)
    public ResponseEntity<List<ArviointiAsteikkoDto>> updateArviointiasteikot(
            @RequestBody List<ArviointiAsteikkoDto> arviointiasteikotDtos
    ) {
        List<ArviointiAsteikkoDto> arviointiasteikot = new ArrayList<>();
        arviointiasteikotDtos.forEach(arviointiAsteikkoDto
                -> arviointiasteikot.add(service.update(arviointiAsteikkoDto)));
        return ResponseEntity.ok(arviointiasteikot);
    }
}
