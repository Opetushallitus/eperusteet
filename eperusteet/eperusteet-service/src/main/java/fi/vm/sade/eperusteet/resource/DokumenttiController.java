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

import com.google.code.docbook4j.Docbook4JException;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import fi.vm.sade.eperusteet.domain.DokumenttiTila;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.resource.util.CacheControl;
import fi.vm.sade.eperusteet.service.DokumenttiService;
import fi.vm.sade.eperusteet.service.PerusteService;
import java.io.IOException;
import java.util.concurrent.Callable;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author jussini
 */
@RestController
@RequestMapping("/dokumentti")
@Api(value="Dokumentit", description = "Perustedokumentin luonti")
public class DokumenttiController {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiController.class);

    @Autowired
    DokumenttiService service;

    @Autowired
    PerusteService perusteService;

    @RequestMapping(value="/create/{perusteId}", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("luo dokumentti")
    public ResponseEntity<DokumenttiDto> create(
            @PathVariable("perusteId") final long perusteId) {

        return create(perusteId, "fi");
    }

    @RequestMapping(value="/create/{perusteId}/{kieli}", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("luo dokumentti")
    public ResponseEntity<DokumenttiDto> create(
            @PathVariable("perusteId") final long perusteId,
            @PathVariable("kieli") final String kieli) {

        try {
            final Kieli k = Kieli.of(kieli);
            final DokumenttiDto createDtoFor = service.createDtoFor(perusteId, k);

            if (createDtoFor.getTila() != DokumenttiTila.EPAONNISTUI) {
                // TODO: use executor service from threadpool
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        service.setStarted(createDtoFor);
                        service.generateWithDto(createDtoFor);
                    }
                };
                new Thread(r).start();
            }

            return new ResponseEntity<>(createDtoFor, HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            LOG.warn("{}", ex.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @RequestMapping(value="/get/{dokumenttiId}", method = RequestMethod.GET, produces = "application/pdf")
    @ResponseBody
    @CacheControl(age = CacheControl.ONE_YEAR, nonpublic = false)
    public ResponseEntity<Object> get(
            @PathVariable("dokumenttiId") final Long dokumenttiId)
    {
        byte[] pdfdata = service.get(dokumenttiId);

        if (pdfdata == null || pdfdata.length == 0) {
            LOG.error("Got null or empty data from service");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-disposition", "attachment; filename=\""+dokumenttiId+".pdf\"");
        return new ResponseEntity<Object>(pdfdata, headers, HttpStatus.OK);
    }

    @RequestMapping(value="/query/{dokumenttiId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<DokumenttiDto> query(
            @PathVariable("dokumenttiId") final Long dokumenttiId)
    {
        LOG.debug("query {}", dokumenttiId);
        DokumenttiDto dto = service.query(dokumenttiId);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value="/uusin/{perusteId}/{kieli}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<DokumenttiDto> query(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("kieli") final String kieli)
    {
        try {
            Kieli k = Kieli.of(kieli);
            DokumenttiDto dto = service.findLatest(perusteId, k);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            LOG.warn("{}", ex.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value="/{perusteId}", method = RequestMethod.GET)
    @ResponseBody
    public Callable<ResponseEntity<Object>> generateByIdAsync(
            @PathVariable("perusteId") final long perusteId) {

        return generateByIdAsync(perusteId, "fi");
    }

    @RequestMapping(value="/{perusteId}/{kieli}", method = RequestMethod.GET)
    @ResponseBody
    @ApiResponse(code = 200, response = Object.class, message = "OK")
    public Callable<ResponseEntity<Object>> generateByIdAsync(
            @PathVariable("perusteId") final long perusteId,
            @PathVariable("kieli") final String kieli) {

        Callable<ResponseEntity<Object>> callable = new Callable<ResponseEntity<Object>>() {

            @Override
            public ResponseEntity<Object> call() {
                return generate(perusteId, kieli);
            }
        };

        return callable;
    }

    private ResponseEntity<Object> generate(long perusteId, String kieli) {

        try {
            DokumenttiDto dto = service.createDtoFor(perusteId, Kieli.of(kieli));
            byte[] pdfdata = service.generateFor(dto);

            if (pdfdata == null || pdfdata.length == 0) {
                LOG.error("Got null or empty data from service");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/pdf"));
            headers.setContentLength(pdfdata.length);
            headers.set("Content-disposition", "attachment; filename=output.pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<Object>(pdfdata, headers, HttpStatus.OK);

        } catch (IllegalArgumentException ex) {
            LOG.warn("{}", ex.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (IOException | TransformerException | ParserConfigurationException | Docbook4JException ex) {
            LOG.error("Exception during document generation:", ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
