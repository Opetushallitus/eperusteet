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

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.dto.PerusteDto;
import fi.vm.sade.eperusteet.service.DokumenttiService;
import fi.vm.sade.eperusteet.service.PerusteService;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author jussini
 */
@Controller
@RequestMapping("/dokumentti")
public class DokumenttiController {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiController.class);

    @Autowired
    DokumenttiService service;

    @Autowired
    PerusteService perusteService;

    @RequestMapping(value="/create/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<DokumenttiDto> create(
            @PathVariable("id") final long id) {

        return create(id, "fi");
    }

    @RequestMapping(value="/create/{id}/{kieli}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<DokumenttiDto> create(
            @PathVariable("id") final long id,
            @PathVariable("kieli") final String kieli) {

        try {
            PerusteDto perusteDto = perusteService.get(new Long(id));

            DokumenttiDto dto = new DokumenttiDto();
            final Kieli k = Kieli.of(kieli);
            final String token = service.getNewTokenFor(id);
            dto.setToken(token);
            dto.setTila(DokumenttiDto.Tila.LUODAAN);

            // TODO: use executor service from threadpool
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    service.generateWithToken(id, token, k);
                }
            };
            new Thread(r).start();

        LOG.info("after thread start");
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            LOG.warn("{}", ex.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @RequestMapping(value="/get/{token}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> get(@PathVariable("token") final String token) {
        LOG.debug("get: {}", token);

        byte[] pdfdata = service.getWithToken(token);

        if (pdfdata == null || pdfdata.length == 0) {
            LOG.error("Got null or empty data from service");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        headers.setContentLength(pdfdata.length);
        headers.set("Content-disposition", "attachment; filename="+token+".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfdata, headers, HttpStatus.OK);
    }

    @RequestMapping(value="/query/{token}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<DokumenttiDto> query(@PathVariable("token") final String token) {
        LOG.debug("query: {}", token);

        DokumenttiDto dto = service.query(token);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Callable<ResponseEntity<byte[]>> generateByIdAsync(
            @PathVariable("id") final long id) {

        return generateByIdAsync(id, "fi");
    }

    @RequestMapping(value="/{id}/{kieli}", method = RequestMethod.GET)
    @ResponseBody
    public Callable<ResponseEntity<byte[]>> generateByIdAsync(
            @PathVariable("id") final long id,
            @PathVariable("kieli") final String kieli) {

        Callable<ResponseEntity<byte[]>> callable = new Callable<ResponseEntity<byte[]>>() {

            @Override
            public ResponseEntity<byte[]> call() {
                LOG.debug("Callable.call: {}", id);
                return generate(id, kieli);
            }
        };

        return callable;
    }

    private ResponseEntity<byte[]> generate(long id, String kieli) {
        LOG.debug("generate: {}", id);

        try {
            byte[] pdfdata = service.generateFor(id, Kieli.of(kieli));

            if (pdfdata == null || pdfdata.length == 0) {
                LOG.error("Got null or empty data from service");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/pdf"));
            headers.setContentLength(pdfdata.length);
            headers.set("Content-disposition", "attachment; filename=output.pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfdata, headers, HttpStatus.OK);

        } catch (IllegalArgumentException ex) {
            LOG.warn("{}", ex.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
