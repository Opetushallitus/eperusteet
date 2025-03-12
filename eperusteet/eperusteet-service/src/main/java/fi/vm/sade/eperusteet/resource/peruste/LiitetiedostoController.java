package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.domain.liite.LiiteTyyppi;
import fi.vm.sade.eperusteet.dto.liite.LiiteDto;
import fi.vm.sade.eperusteet.resource.util.CacheControl;
import fi.vm.sade.eperusteet.service.LiiteService;
import fi.vm.sade.eperusteet.service.LiiteTiedostoService;
import fi.vm.sade.eperusteet.service.util.Pair;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/perusteet/{perusteId}")
@Tag(name = "Liitetiedostot")
public class LiitetiedostoController {

    @Autowired
    private LiiteService liitteet;

    @Autowired
    private LiiteTiedostoService liiteTiedostoService;
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
    ) throws IOException, MimeTypeException, HttpMediaTypeNotSupportedException {
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
    ) throws IOException, MimeTypeException, HttpMediaTypeNotSupportedException {
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
    ) throws MimeTypeException, IOException, HttpMediaTypeNotSupportedException {
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
        getLiite(perusteId, fileName, etag,  response);
    }

    @RequestMapping(value = "/liitteet/{fileName}", method = RequestMethod.GET)
    @CacheControl(age = CacheControl.ONE_YEAR)
    public void getLiite(
            @PathVariable("perusteId") Long perusteId,
            @PathVariable("fileName") String fileName,
            @RequestHeader(value = "If-None-Match", required = false) String etag,
            HttpServletResponse response
    ) throws IOException {
        UUID id = UUID.fromString(FilenameUtils.removeExtension(fileName));
        LiiteDto dto = liitteet.get(perusteId, id);
        getFile(id, dto, response, etag, perusteId);
    }

    @RequestMapping(value = "/julkaisu/liitteet/{fileName}", method = RequestMethod.GET)
    public void getJulkaisuLiite(
            @PathVariable("perusteId") Long perusteId,
            @PathVariable("fileName") String fileName,
            @RequestHeader(value = "If-None-Match", required = false) String etag,
            HttpServletResponse response
    ) throws IOException {
        UUID id = UUID.fromString(FilenameUtils.removeExtension(fileName));
        LiiteDto dto = liitteet.get(id);
        getFile(id, dto, response, etag, perusteId);
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
            Set<String> tyypit) throws MimeTypeException, IOException, HttpMediaTypeNotSupportedException {
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
    ) throws MimeTypeException, IOException, HttpMediaTypeNotSupportedException {
        return liiteTiedostoService.uploadFile(perusteId, nimi, is, koko, tyyppi, tyypit, width, height, file);
    }

    private void getFile(UUID id, LiiteDto dto, HttpServletResponse response, String etag, Long perusteId) throws IOException {
        if (dto != null) {
            if (DOCUMENT_TYPES.contains(dto.getMime())) {
                response.setHeader("Content-Disposition", "inline;filename*=UTF-8''" + encodeFilename(dto.getNimi(), id.toString()));
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

    private String encodeFilename(String filename, String fallBack) {
        String fileName;

        try {
            fileName =  URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        } catch (Exception e) {
            fileName = fallBack + ".pdf";
        }

        if (!fileName.contains(".pdf")) {
            fileName += ".pdf";
        }

        return fileName;
    }
}
