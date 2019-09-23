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

import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoPageDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.KoodistoPagedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 *
 * @author nkala
 */
@RestController
@RequestMapping("/koodisto")
@InternalApi
@PreAuthorize("isAuthenticated()")
public class KoodistoController {
    @Autowired
    KoodistoClient service;

    @Autowired
    KoodistoPagedService koodistoPagedService;

    @RequestMapping(value = "/{koodisto}", method = GET)
    public ResponseEntity<List<KoodistoKoodiDto>> kaikki(
        @PathVariable("koodisto") final String koodisto,
        @RequestParam(value = "haku", required = false) final String haku) {
        return new ResponseEntity<>(haku == null || haku.isEmpty()
                ? service.getAll(koodisto)
                : service.filterBy(koodisto, haku), HttpStatus.OK);
    }

    @RequestMapping(value = "/sivutettu/{koodisto}", method = GET)
    public ResponseEntity<Page<KoodistoKoodiDto>> kaikkiSivutettuna(
            @PathVariable("koodisto") final String koodisto,
            @RequestParam(value = "haku", required = false)  final String haku,
            KoodistoPageDto koodistoPageDto) {

        return new ResponseEntity<>(koodistoPagedService.getAllPaged(koodisto, haku, koodistoPageDto), HttpStatus.OK);
    }

    @RequestMapping(value = "/{koodisto}/{koodi}", method = GET)
    public ResponseEntity<KoodistoKoodiDto> yksi(
        @PathVariable("koodisto") final String koodisto,
        @PathVariable("koodi") final String koodi) {
        return new ResponseEntity<>(service.get(koodisto, koodi), HttpStatus.OK);
    }

    @RequestMapping(value = "/relaatio/sisaltyy-alakoodit/{koodi}", method = GET)
    public ResponseEntity<List<KoodistoKoodiDto>> alarelaatio(
        @PathVariable("koodi") final String koodi) {
        return new ResponseEntity<>(service.getAlarelaatio(koodi), HttpStatus.OK);
    }

    @RequestMapping(value = "/relaatio/sisaltyy-ylakoodit/{koodi}", method = GET)
    public ResponseEntity<List<KoodistoKoodiDto>> ylarelaatio(
        @PathVariable("koodi") final String koodi) {
        return new ResponseEntity<>(service.getYlarelaatio(koodi), HttpStatus.OK);
    }

    @RequestMapping(value = "/{koodisto}", method = POST)
    public ResponseEntity<KoodistoKoodiDto> lisaaUusiKoodi(
            @PathVariable("koodisto") final String koodisto,
            @RequestBody LokalisoituTekstiDto koodinimi) {
        return new ResponseEntity<>(service.addKoodiNimella(koodisto, koodinimi), HttpStatus.OK);
    }

}
