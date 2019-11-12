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

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.resource.util.CacheControl;

import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiService;
import fi.vm.sade.eperusteet.service.exception.DokumenttiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 *
 * @author jussini
 */
@RestController
@RequestMapping("/dokumentit")
@InternalApi
@Api(value = "Dokumentit")
public class DokumenttiController {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiController.class);

    @Autowired
    PerusteRepository perusteRepository;

    @Autowired
    DokumenttiService service;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("luo dokumentti")
    public ResponseEntity<DokumenttiDto> createDokumentti(
            @RequestParam("perusteId") final long perusteId,
            @RequestParam(value = "kieli", defaultValue = "fi") final String kieli,
            @RequestParam(value = "suoritustapakoodi") final String suoritustapakoodi,
            @RequestParam(value = "version", defaultValue = "uusi") final String version
    ) throws DokumenttiException {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        final DokumenttiDto createDtoFor = service.createDtoFor(
                perusteId,
                Kieli.of(kieli),
                Suoritustapakoodi.of(suoritustapakoodi),
                GeneratorVersion.of(version));

        if (createDtoFor != null && createDtoFor.getTila() != DokumenttiTila.EPAONNISTUI) {
            service.setStarted(createDtoFor);
            service.generateWithDto(createDtoFor);
            status = HttpStatus.ACCEPTED;
        }

        return new ResponseEntity<>(createDtoFor, status);
    }

    @RequestMapping(value = "/{fileName}", method = RequestMethod.GET, produces = "application/pdf")
    @ResponseBody
    @CacheControl(age = CacheControl.ONE_YEAR, nonpublic = false)
    public ResponseEntity<Object> getDokumentti(
            @PathVariable("fileName") String fileName
    ) {
        Long dokumenttiId = Long.valueOf(FilenameUtils.removeExtension(fileName));
        String extension = FilenameUtils.getExtension(fileName);

        byte[] pdfdata = service.get(dokumenttiId);

        // Tarkistetaan tiedostopääte jos asetettu kutsuun
        if (!ObjectUtils.isEmpty(extension) && !Objects.equals(extension, "pdf")) {
            LOG.error("Got wrong file extension");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (pdfdata == null || pdfdata.length == 0) {
            LOG.error("Got null or empty data from service");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        DokumenttiDto dokumenttiDto = service.query(dokumenttiId);
        Peruste peruste = perusteRepository.findOne(dokumenttiDto.getPerusteId());
        if (peruste != null) {
            TekstiPalanen nimi = peruste.getNimi();
            if (nimi != null && nimi.getTeksti().containsKey(dokumenttiDto.getKieli())) {
                headers.set("Content-disposition", "inline; filename=\""
                        + nimi.getTeksti().get(dokumenttiDto.getKieli()) + ".pdf\"");
            } else {
                headers.set("Content-disposition", "inline; filename=\"" + dokumenttiId + ".pdf\"");
            }
        }

        return new ResponseEntity<>(pdfdata, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/peruste", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Long> getDokumenttiId(
            @RequestParam final Long perusteId,
            @RequestParam(defaultValue = "fi") final String kieli,
            @RequestParam(value = "suoritustapa", defaultValue = "naytto") final String suoritustapa
    ) {
        Suoritustapakoodi s = Suoritustapakoodi.of(suoritustapa);
        Long dokumenttiId = service.getDokumenttiId(perusteId, Kieli.of(kieli), s, GeneratorVersion.UUSI);
        return ResponseEntity.ok(dokumenttiId);
    }

    @RequestMapping(value = "/kvliite", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Long> getKVLiiteDokumenttiId(
            @RequestParam final Long perusteId,
            @RequestParam(defaultValue = "fi") final String kieli,
            @RequestParam(value = "suoritustapa", defaultValue = "naytto") final String suoritustapa
    ) {
        Suoritustapakoodi s = Suoritustapakoodi.of(suoritustapa);
        Long dokumenttiId = service.getDokumenttiId(perusteId, Kieli.of(kieli), s, GeneratorVersion.KVLIITE);
        return ResponseEntity.ok(dokumenttiId);
    }

    @RequestMapping(method = RequestMethod.GET, params = "perusteId")
    @ResponseBody
    public ResponseEntity<DokumenttiDto> getLatestDokumentti(
            @RequestParam("perusteId") final Long perusteId,
            @RequestParam(defaultValue = "fi") final String kieli,
            @RequestParam("suoritustapa") final String suoritustapa,
            @RequestParam(defaultValue = "") final String version
    ) {
        try {
            Kieli kielikoodi = Kieli.of(kieli);
            Suoritustapakoodi suoritustapakoodi = Suoritustapakoodi.of(suoritustapa);
            GeneratorVersion gversion = null;
            if (!"".equals(version)) {
                gversion = GeneratorVersion.of(version);
            }
            DokumenttiDto dto = service.findLatest(perusteId, kielikoodi, suoritustapakoodi, gversion);

            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            LOG.warn("{}", ex.getMessage());

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/{dokumenttiId}/tila", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<DokumenttiDto> queryDokumenttiTila(@PathVariable("dokumenttiId") final Long dokumenttiId) {
        DokumenttiDto dto = service.query(dokumenttiId);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}
