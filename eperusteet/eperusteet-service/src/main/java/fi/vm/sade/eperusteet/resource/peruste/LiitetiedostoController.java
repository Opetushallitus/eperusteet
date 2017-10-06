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
package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.dto.liite.LiiteDto;
import fi.vm.sade.eperusteet.resource.util.CacheControl;
import fi.vm.sade.eperusteet.service.LiiteService;
import fi.vm.sade.eperusteet.service.audit.EperusteetAudit;

import static fi.vm.sade.eperusteet.service.audit.EperusteetMessageFields.KUVA;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.LISAYS;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.POISTO;

import fi.vm.sade.eperusteet.service.audit.LogMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author jhyoty
 */
@RestController
@RequestMapping("/perusteet/{perusteId}/kuvat")
public class LiitetiedostoController {

    @Autowired
    private EperusteetAudit audit;

    private static final int BUFSIZE = 64 * 1024;
    final Tika tika = new Tika();

    @Autowired
    private LiiteService liitteet;

    private static final Set<String> SUPPORTED_TYPES;

    static {
        HashSet<String> tmp = new HashSet<>(Arrays.asList(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE));
        SUPPORTED_TYPES = Collections.unmodifiableSet(tmp);
    }


    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    public ResponseEntity<String> upload(
            @PathVariable("perusteId")
            @P("perusteId") Long perusteId,
            @RequestParam("nimi") String nimi,
            @RequestParam("file") Part file,
            UriComponentsBuilder ucb)
            throws IOException, HttpMediaTypeNotSupportedException {
        final long koko = file.getSize();
        try (PushbackInputStream pis = new PushbackInputStream(file.getInputStream(), BUFSIZE)) {
            byte[] buf = new byte[koko < BUFSIZE ? (int) koko : BUFSIZE];
            int len = pis.read(buf);
            if (len < buf.length) {
                throw new IOException("luku epäonnistui");
            }
            pis.unread(buf);
            String tyyppi = tika.detect(buf);
            if (!SUPPORTED_TYPES.contains(tyyppi)) {
                throw new HttpMediaTypeNotSupportedException(tyyppi + "ei ole tuettu");
            }
            UUID id = liitteet.add(perusteId, tyyppi, nimi, koko, pis);
            HttpHeaders h = new HttpHeaders();
            h.setLocation(ucb.path("/perusteet/{perusteId}/kuvat/{id}").buildAndExpand(perusteId, id.toString()).toUri());
            audit.withAudit(LogMessage.builder(perusteId, KUVA, LISAYS));
            return new ResponseEntity<>(id.toString(), h, HttpStatus.CREATED);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @CacheControl(age = CacheControl.ONE_YEAR)
    public void get(
            @PathVariable("perusteId") Long perusteId,
            @PathVariable("id") UUID id,
            @RequestHeader(value = "If-None-Match", required = false) String etag,
            HttpServletResponse response) throws IOException {

        LiiteDto dto = liitteet.get(perusteId, id);
        if (dto != null) {
            if (etag != null && dto.getId().toString().equals(etag)) {
                response.setStatus(HttpStatus.NOT_MODIFIED.value());
            } else {
                response.setHeader("Content-Type", dto.getTyyppi());
                response.setHeader("ETag", id.toString());
                try (OutputStream os = response.getOutputStream()) {
                    liitteet.export(perusteId, id, os);
                    os.flush();
                }
            }
        } else {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("perusteId") Long perusteId,
            @PathVariable("id") UUID id) {
        audit.withAudit(LogMessage.builder(perusteId, KUVA, POISTO), (Void) -> {
            liitteet.delete(perusteId, id);
            return null;
        });
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<LiiteDto> getAll(@PathVariable("perusteId") Long perusteId) {
        return liitteet.getAll(perusteId);
    }

    private static final Logger LOG = LoggerFactory.getLogger(LiitetiedostoController.class);

}
