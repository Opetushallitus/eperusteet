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

import fi.vm.sade.eperusteet.dto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.service.KoodistoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nkala
 */
@Controller
@RequestMapping("/api/koodisto")
public class KoodistoController {
    private static final Logger LOG = LoggerFactory.getLogger(KoodistoController.class);

    @Autowired
    KoodistoService service;

    @RequestMapping(value = "/{koodisto}", method = GET)
    @ResponseBody
    public ResponseEntity<List<KoodistoKoodiDto>> kaikki(
        @PathVariable("koodisto") final String koodisto,
        @RequestParam(value = "haku", required = false) final String haku) {
        return new ResponseEntity<>(haku == null || haku.isEmpty()
                ? service.getAll(koodisto)
                : service.filterBy(koodisto, haku), HttpStatus.OK);
    }

    @RequestMapping(value = "/{koodisto}/{koodi}", method = GET)
    @ResponseBody
    public ResponseEntity<KoodistoKoodiDto> yksi(
        @PathVariable("koodisto") final String koodisto,
        @PathVariable("koodi") final String koodi) {
        return new ResponseEntity<>(service.get(koodisto, koodi), HttpStatus.OK);
    }
    
    @RequestMapping(value = "/relaatio/sisaltyy-alakoodit/{koodi}", method = GET)
    @ResponseBody
    public ResponseEntity<List<KoodistoKoodiDto>> alarelaatio(
        @PathVariable("koodi") final String koodi) {
        return new ResponseEntity<>(service.getAlarelaatio(koodi), HttpStatus.OK);
    }
}
