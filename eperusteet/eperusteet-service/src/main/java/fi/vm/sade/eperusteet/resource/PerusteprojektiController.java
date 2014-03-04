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

import fi.vm.sade.eperusteet.dto.PerusteprojektiDto;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author harrik
 */
@Controller
@RequestMapping("/api/perusteprojekti")
public class PerusteprojektiController {
    
    private static final Logger LOG = LoggerFactory.getLogger(PerusteprojektiController.class);
    
    @Autowired
    private PerusteprojektiService service;
    
    @RequestMapping(method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<PerusteprojektiDto> add(@RequestBody PerusteprojektiDto perusteprojektiDto, UriComponentsBuilder ucb) {
        LOG.info("add {}", perusteprojektiDto);
        perusteprojektiDto = service.save(perusteprojektiDto);
        return new ResponseEntity<>(perusteprojektiDto, buildHeadersFor(perusteprojektiDto.getId(), ucb), HttpStatus.CREATED);
    }
    
    private HttpHeaders buildHeadersFor(Long id, UriComponentsBuilder ucb) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucb.path("/perusteprojekti/{id}").buildAndExpand(id).toUri());
        return headers;
    }
    
}
