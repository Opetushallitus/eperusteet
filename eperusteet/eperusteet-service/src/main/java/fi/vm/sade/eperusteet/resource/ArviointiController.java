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

import fi.vm.sade.eperusteet.dto.ArviointiDto;
import fi.vm.sade.eperusteet.service.ArviointiService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author teele1
 */
@Controller
@RequestMapping("/api/arvioinnit")
public class ArviointiController {
    
    private static final Logger LOG = LoggerFactory.getLogger(ArviointiController.class);
    
    @Autowired
    private ArviointiService arviointiService;
    
    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    @ResponseBody
    public ResponseEntity<ArviointiDto> findById(@PathVariable("id") Long id) {
        ArviointiDto arviointiDto = arviointiService.findById(id);
        if(arviointiDto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(arviointiDto, HttpStatus.OK);
    }
    
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<ArviointiDto>> findAll() {
        return new ResponseEntity<>(arviointiService.findAll(), HttpStatus.OK);
    }
    
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ArviointiDto> add(@RequestBody ArviointiDto arviointiDto) {
        LOG.info("add {}", arviointiDto);
        arviointiDto = arviointiService.add(arviointiDto);
        return new ResponseEntity<>(arviointiDto, HttpStatus.CREATED);
    }
}
