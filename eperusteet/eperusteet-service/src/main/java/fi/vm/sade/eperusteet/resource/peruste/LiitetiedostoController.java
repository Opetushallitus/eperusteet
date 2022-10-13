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

import fi.vm.sade.eperusteet.domain.liite.LiiteTyyppi;
import fi.vm.sade.eperusteet.dto.liite.LiiteDto;
import fi.vm.sade.eperusteet.resource.util.CacheControl;
import fi.vm.sade.eperusteet.service.LiiteService;
import fi.vm.sade.eperusteet.service.util.Pair;
import io.swagger.annotations.Api;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
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
 *
 * @author jhyoty
 */
@Slf4j
@RestController
@RequestMapping("/perusteet/{perusteId}")
@Api("Liitetiedostot")
public class LiitetiedostoController {

    private static final int BUFSIZE = 64 * 1024;
    final Tika tika = new Tika();

    @Autowired
    private LiiteService liitteet;

    public static final Set<String> IMAGE_TYPES;

    public static final Set<String> DOCUMENT_TYPES;

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
            @RequestParam Integer width,
            @RequestParam Integer height,
            UriComponentsBuilder ucb
    ) throws IOException, HttpMediaTypeNotSupportedException, MimeTypeException {
        Pair<UUID, String> res = upload(perusteId, nimi, file.getInputStream(), file.getSize(), LiiteTyyppi.KUVA, IMAGE_TYPES, width, height, file);
        UUID uuid = res.getFirst();
        String extension = res.getSecond();

        HttpHeaders h = new HttpHeaders();
        h.setLocation(ucb.path("/perusteet/{perusteId}/kuvat/{id}" + extension).buildAndExpand(perusteId, uuid.toString()).toUri());

        return new ResponseEntity<>(uuid.toString(), h, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/liitteet", method = RequestMethod.POST)
    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    public ResponseEntity<String> uploadLiite(
            @PathVariable("perusteId")
            @P("perusteId") Long perusteId,
            @RequestParam("nimi") String nimi,
            @RequestParam("file") Part file,
            @RequestParam("tyyppi") String tyyppi,
            UriComponentsBuilder ucb
    ) throws IOException, HttpMediaTypeNotSupportedException, MimeTypeException {
        Pair<UUID, String> res = upload(perusteId, nimi, file.getInputStream(), file.getSize(), LiiteTyyppi.of(tyyppi), DOCUMENT_TYPES);
        UUID uuid = res.getFirst();
        String extension = res.getSecond();

        HttpHeaders h = new HttpHeaders();
        h.setLocation(ucb.path("/perusteet/{perusteId}/liitteet/{id}" + extension).buildAndExpand(perusteId, uuid.toString()).toUri());

        return new ResponseEntity<>(uuid.toString(), h, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/liitteet/b64", method = RequestMethod.POST)
    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    public ResponseEntity<String> uploadLiiteBase64(
            @PathVariable("perusteId")
            @P("perusteId") Long perusteId,
            @RequestParam("nimi") String nimi,
            @RequestParam("file") String b64file,
            @RequestParam("tyyppi") String tyyppi,
            UriComponentsBuilder ucb
    ) throws IOException, HttpMediaTypeNotSupportedException, MimeTypeException {
        byte[] decoder = Base64.getDecoder().decode(b64file);
        InputStream is = new ByteArrayInputStream(decoder);
        Pair<UUID, String> res = upload(perusteId, nimi, is, decoder.length, LiiteTyyppi.of(tyyppi), DOCUMENT_TYPES);
        UUID uuid = res.getFirst();
        String extension = res.getSecond();

        HttpHeaders h = new HttpHeaders();
        h.setLocation(ucb.path("/perusteet/{perusteId}/liitteet/{id}" + extension).buildAndExpand(perusteId, uuid.toString()).toUri());

        return new ResponseEntity<>(uuid.toString(), h, HttpStatus.CREATED);
    }

    // For swagger
    @RequestMapping(value = "/kuvat/{fileName}", method = RequestMethod.GET)
    public void getKuva(
            @PathVariable("perusteId") Long perusteId,
            @PathVariable("fileName") String fileName,
            @RequestHeader(value = "If-None-Match", required = false) String etag,
            //HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        getLiite(perusteId, fileName, etag, "image", /*request,*/ response);
    }

    @RequestMapping(value = "/liitteet/{fileName}", method = RequestMethod.GET)
    @CacheControl(age = CacheControl.ONE_YEAR)
    public void getLiite(
            @PathVariable("perusteId") Long perusteId,
            @PathVariable("fileName") String fileName,
            @RequestHeader(value = "If-None-Match", required = false) String etag,
            String topLevelMediaType,
            //HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        UUID id = UUID.fromString(FilenameUtils.removeExtension(fileName));
        String extension = FilenameUtils.getExtension(fileName);

        if (topLevelMediaType == null) {
            topLevelMediaType = "application";
        }

        LiiteDto dto = liitteet.get(perusteId, id);

        // Liitteen tiedostopääte voidaan pakottaa uudelleenohjauksella
        /*
        String requestURI = request.getRequestURI();
        if (dto != null && ObjectUtils.isEmpty(extension) && !ObjectUtils.isEmpty(requestURI)) {
            try {
                MimeTypes mimeTypes = MimeTypes.getDefaultMimeTypes();
                String realExtension = mimeTypes.forName(dto.getMime()).getExtension();
                response.addHeader("Location", requestURI + realExtension);
                response.setStatus(HttpStatus.MOVED_PERMANENTLY.value());
                return;
            } catch (MimeTypeException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
        */

        boolean isCorrectExtension = true;

        // Tarkistetaan tiedostopääte jos asetettu kutsuun
        if (!ObjectUtils.isEmpty(extension) && dto != null) {
            isCorrectExtension = Objects.equals(dto.getMime(), new MimetypesFileTypeMap().getContentType(fileName));
        }

        if (dto != null && isCorrectExtension) {
            if (DOCUMENT_TYPES.contains(dto.getMime())) {
                response.setHeader("Content-disposition",
                        "inline; filename=\"" + dto.getNimi() + ".pdf\"");
            }

            if (dto.getId().toString().equals(etag)) {
                response.setStatus(HttpStatus.NOT_MODIFIED.value());
            } else {
                response.setHeader("Content-Type", dto.getMime());
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
        liitteet.delete(perusteId, id);
    }

    @RequestMapping(value = "/kuvat", method = RequestMethod.GET)
    public List<LiiteDto> getAllKuvat(@PathVariable("perusteId") Long perusteId) {
        return liitteet.getAllByTyyppi(perusteId, IMAGE_TYPES);
    }

    @RequestMapping(value = "/liitteet", method = RequestMethod.GET)
    public List<LiiteDto> getAllLiitteet(@PathVariable("perusteId") Long perusteId) {
        return liitteet.getAllByTyyppi(perusteId, DOCUMENT_TYPES);
    }

    @RequestMapping(value = "/lisatieto", method = RequestMethod.POST)
    public void paivitaLisatieto(@PathVariable("perusteId") Long perusteId,
                                 @RequestParam("liiteId") String liiteId,
                                 @RequestParam("lisatieto") String lisatieto) {
        liitteet.paivitaLisatieto(perusteId, UUID.fromString(liiteId), lisatieto);
    }

    private Pair<UUID, String> upload(
            Long perusteId,
            String nimi,
            InputStream is,
            long koko,
            LiiteTyyppi tyyppi,
            Set<String> tyypit) throws IOException, HttpMediaTypeNotSupportedException, MimeTypeException {
        return upload(perusteId, nimi, is, koko, tyyppi, tyypit, null, null, null);
    }

    private Pair<UUID, String> upload(
            Long perusteId,
            String nimi,
            InputStream is,
            long koko,
            LiiteTyyppi tyyppi,
            Set<String> tyypit,
            Integer width,
            Integer height,
            Part file
    ) throws IOException, HttpMediaTypeNotSupportedException, MimeTypeException {
        try (PushbackInputStream pis = new PushbackInputStream(is, BUFSIZE)) {
            byte[] buf = new byte[koko < BUFSIZE ? (int) koko : BUFSIZE];
            int len = pis.read(buf);
            if (len < buf.length) {
                throw new IOException("luku epäonnistui");
            }
            pis.unread(buf);
            String mime = tika.detect(buf);
            MimeTypes mimeTypes = MimeTypes.getDefaultMimeTypes();
            String extension = mimeTypes.forName(mime).getExtension();

            if (!tyypit.contains(mime)) {
                throw new HttpMediaTypeNotSupportedException(mime + " ei ole tuettu");
            }

            if (width != null && height != null && file != null) {
                String mediaType = tika.detect(buf);
                ByteArrayOutputStream os = scaleImage(file, mediaType, width, height);
                return Pair.of(liitteet.add(perusteId, tyyppi, mime, nimi, os.size(), new PushbackInputStream(new ByteArrayInputStream(os.toByteArray()))), extension);
            } else {
                return Pair.of(liitteet.add(perusteId, tyyppi, mime, nimi, koko, pis), extension);
            }

        }
    }

    private ByteArrayOutputStream scaleImage(@RequestParam("file") Part file, String tyyppi, Integer width, Integer height) throws IOException {
        BufferedImage a = ImageIO.read(file.getInputStream());
        BufferedImage preview = new BufferedImage(width, height, a.getType());
        preview.createGraphics().drawImage(a.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(preview, tyyppi.replace("image/", ""), os);

        return os;
    }


}
