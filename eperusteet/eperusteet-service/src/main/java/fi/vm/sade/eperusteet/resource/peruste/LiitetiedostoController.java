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
import fi.vm.sade.eperusteet.service.audit.LogMessage;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.*;

import static fi.vm.sade.eperusteet.service.audit.EperusteetMessageFields.KUVA;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.LISAYS;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.POISTO;

/**
 *
 * @author jhyoty
 */
@RestController
@RequestMapping("/perusteet/{perusteId}")
public class LiitetiedostoController {

    @Autowired
    private EperusteetAudit audit;

    private static final int BUFSIZE = 64 * 1024;
    final Tika tika = new Tika();

    @Autowired
    private LiiteService liitteet;

    private static final Set<String> IMAGE_TYPES;

    private static final Set<String> DOCUMENT_TYPES;

    static {
        IMAGE_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                MediaType.IMAGE_JPEG_VALUE,
                MediaType.IMAGE_PNG_VALUE
        )));

        DOCUMENT_TYPES = Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(
                MediaType.APPLICATION_PDF_VALUE
        )));
    }


    @RequestMapping(value = "/kuvat", method = RequestMethod.POST)
    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    public ResponseEntity<String> uploadKuva(
        @PathVariable("perusteId")
        @P("perusteId") Long perusteId,
        @RequestParam("nimi") String nimi,
        @RequestParam("file") Part file,
        UriComponentsBuilder ucb
    ) throws IOException, HttpMediaTypeNotSupportedException {
        UUID uuid = upload(perusteId, nimi, file, IMAGE_TYPES);

        HttpHeaders h = new HttpHeaders();
        h.setLocation(ucb.path("/perusteet/{perusteId}/kuvat/{id}").buildAndExpand(perusteId, uuid.toString()).toUri());
        audit.withAudit(LogMessage.builder(perusteId, KUVA, LISAYS));

        return new ResponseEntity<>(uuid.toString(), h, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/liitteet", method = RequestMethod.POST)
    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    public ResponseEntity<String> uploadLiite(
            @PathVariable("perusteId")
            @P("perusteId") Long perusteId,
            @RequestParam("nimi") String nimi,
            @RequestParam("file") Part file,
            UriComponentsBuilder ucb
    ) throws IOException, HttpMediaTypeNotSupportedException {
        UUID uuid = upload(perusteId, nimi, file, DOCUMENT_TYPES);

        HttpHeaders h = new HttpHeaders();
        h.setLocation(ucb.path("/perusteet/{perusteId}/liitteet/{id}").buildAndExpand(perusteId, uuid.toString()).toUri());
        audit.withAudit(LogMessage.builder(perusteId, KUVA, LISAYS));

        return new ResponseEntity<>(uuid.toString(), h, HttpStatus.CREATED);
    }

    @RequestMapping(value = { "/kuvat/{id}", "/liitteet/{id}" }, method = RequestMethod.GET)
    @CacheControl(age = CacheControl.ONE_YEAR)
    public void get(
        @PathVariable("perusteId") Long perusteId,
        @PathVariable("id") UUID id,
        @RequestHeader(value = "If-None-Match", required = false) String etag,
        HttpServletResponse response
    ) throws IOException {
        LiiteDto dto = liitteet.get(perusteId, id);
        if (dto != null) {
            if (DOCUMENT_TYPES.contains(dto.getTyyppi())) {
                response.setHeader("Content-disposition", "inline; filename=\"" + dto.getNimi() + "\"");
            }

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

    @RequestMapping(value = { "/kuvat/{id}", "/liitteet/{id}"}, method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @PathVariable("perusteId") Long perusteId,
        @PathVariable("id") UUID id
    ) {
        audit.withAudit(LogMessage.builder(perusteId, KUVA, POISTO), (Void) -> {
            liitteet.delete(perusteId, id);
            return null;
        });
    }

    @RequestMapping(value = "/kuvat", method = RequestMethod.GET)
    public List<LiiteDto> getAllKuvat(@PathVariable("perusteId") Long perusteId) {
        return liitteet.getAllByTyyppi(perusteId, IMAGE_TYPES);
    }

    @RequestMapping(value = "/liitteet", method = RequestMethod.GET)
    public List<LiiteDto> getAllLiitteet(@PathVariable("perusteId") Long perusteId) {
        return liitteet.getAllByTyyppi(perusteId, DOCUMENT_TYPES);
    }

    private UUID upload(
            Long perusteId,
            String nimi,
            Part file,
            Set<String> tyypit
    ) throws IOException, HttpMediaTypeNotSupportedException {
        final long koko = file.getSize();
        try (PushbackInputStream pis = new PushbackInputStream(file.getInputStream(), BUFSIZE)) {
            byte[] buf = new byte[koko < BUFSIZE ? (int) koko : BUFSIZE];
            int len = pis.read(buf);
            if (len < buf.length) {
                throw new IOException("luku epäonnistui");
            }
            pis.unread(buf);
            String tyyppi = tika.detect(buf);
            if (!tyypit.contains(tyyppi)) {
                throw new HttpMediaTypeNotSupportedException(tyyppi + " ei ole tuettu");
            }

            return liitteet.add(perusteId, tyyppi, nimi, koko, pis);
        }
    }

}
