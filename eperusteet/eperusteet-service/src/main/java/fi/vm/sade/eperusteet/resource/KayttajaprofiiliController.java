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

import fi.vm.sade.eperusteet.dto.KayttajaProfiiliDto;
import fi.vm.sade.eperusteet.dto.PerusteDto;
import fi.vm.sade.eperusteet.dto.SuosikkiDto;
import fi.vm.sade.eperusteet.service.KayttajaprofiiliService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.SuosikkiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author harrik
 */
@Controller
@RequestMapping("/kayttajaprofiili")
public class KayttajaprofiiliController {

    private static final Logger LOG = LoggerFactory.getLogger(KayttajaprofiiliController.class);

    @Autowired
    private KayttajaprofiiliService service;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private SuosikkiService suosikkiService;



    @RequestMapping(value = "", method = GET)
    @ResponseBody
    public ResponseEntity<KayttajaProfiiliDto> get() {
        KayttajaProfiiliDto k = service.get();
        if (k == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(k, HttpStatus.OK);
    }

    @RequestMapping(value = "/suosikki", method = POST, consumes="application/json")
    @ResponseBody
    public ResponseEntity<KayttajaProfiiliDto> addSuosikki(@RequestBody SuosikkiDto suosikkiDto) {
        LOG.info("addSuosikki {}", suosikkiDto.getPerusteId());

        PerusteDto peruste = perusteService.getByIdAndSuoritustapa(suosikkiDto.getPerusteId(), suosikkiDto.getSuoritustapakoodi());
        if (peruste == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        KayttajaProfiiliDto profiiliDto = service.addSuosikki(suosikkiDto);
        return new ResponseEntity<>(profiiliDto, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/suosikki/{suosikkiId}", method = DELETE)
    @ResponseBody
    public ResponseEntity<KayttajaProfiiliDto> delete(@PathVariable("suosikkiId") final Long suosikkiId) {
        SuosikkiDto suosikki = suosikkiService.get(suosikkiId);
        if (suosikki == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        KayttajaProfiiliDto k = service.deleteSuosikki(suosikkiId);
        return new ResponseEntity<>(k, HttpStatus.OK);
    }
}