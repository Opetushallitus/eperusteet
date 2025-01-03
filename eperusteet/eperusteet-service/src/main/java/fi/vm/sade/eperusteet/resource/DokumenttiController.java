package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.domain.DokumenttiTila;
import fi.vm.sade.eperusteet.domain.GeneratorVersion;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.dto.pdf.PdfData;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.resource.util.CacheControl;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiService;
import fi.vm.sade.eperusteet.service.exception.DokumenttiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.Objects;

@RestController
@RequestMapping("/api/dokumentit")
@InternalApi
@Tag(name = "Dokumentit")
public class DokumenttiController {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiController.class);

    @Autowired
    PerusteRepository perusteRepository;

    @Autowired
    DokumenttiService service;

    @Autowired
    PerusteService perusteService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @Operation(summary = "luo dokumentti")
    public ResponseEntity<DokumenttiDto> createDokumentti(
            @RequestParam long perusteId,
            @RequestParam String kieli,
            @RequestParam String suoritustapakoodi,
            @RequestParam(defaultValue = "uusi") final String version
    ) throws DokumenttiException {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        DokumenttiDto viimeisinJulkaistuDokumentti = service.getJulkaistuDokumentti(perusteId, Kieli.of(kieli), null);
        if (viimeisinJulkaistuDokumentti != null && viimeisinJulkaistuDokumentti.getTila().equals(DokumenttiTila.EPAONNISTUI)) {
            service.setStarted(viimeisinJulkaistuDokumentti);
            service.generateWithDto(viimeisinJulkaistuDokumentti, perusteService.getJulkaistuSisalto(perusteId));
        }

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
        // estetään googlea indeksoimasta pdf:iä
        headers.set("X-Robots-Tag", "noindex");
        return new ResponseEntity<>(pdfdata, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/peruste", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Long> getDokumenttiId(
            @RequestParam Long perusteId,
            @RequestParam String kieli,
            @RequestParam(defaultValue = "naytto") final String suoritustapa
    ) {
        Suoritustapakoodi s = Suoritustapakoodi.of(suoritustapa);
        Long dokumenttiId = service.getDokumenttiId(perusteId, Kieli.of(kieli), s, GeneratorVersion.UUSI);
        return ResponseEntity.ok(dokumenttiId);
    }

    @RequestMapping(value = "/kvliite", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Long> getKVLiiteDokumenttiId(
            @RequestParam final Long perusteId,
            @RequestParam final String kieli,
            @RequestParam(defaultValue = "naytto") final String suoritustapa
    ) {
        Suoritustapakoodi s = Suoritustapakoodi.of(suoritustapa);
        Long dokumenttiId = service.getDokumenttiId(perusteId, Kieli.of(kieli), s, GeneratorVersion.KVLIITE);
        return ResponseEntity.ok(dokumenttiId);
    }

    @RequestMapping(method = RequestMethod.GET, params = "perusteId")
    @ResponseBody
    public ResponseEntity<DokumenttiDto> getLatestDokumentti(
            @RequestParam final Long perusteId,
            @RequestParam final String kieli,
            @RequestParam final String suoritustapa,
            @RequestParam final String version
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

    @RequestMapping(value = "/julkaistu", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<DokumenttiDto> getJulkaistuDokumentti(
            @RequestParam final Long perusteId,
            @RequestParam final String kieli,
            @RequestParam(required = false) final Integer revision
    ) {
        return ResponseEntity.ok(service.getJulkaistuDokumentti(perusteId, Kieli.of(kieli), revision));
    }

    @PostMapping(path = "/pdf/data/{dokumenttiId}")
    @ResponseBody
    public ResponseEntity<String> savePdfData(@PathVariable("dokumenttiId") Long dokumenttiId,
                                              @RequestBody PdfData pdfData) {
        service.updateDokumenttiPdfData(Base64.getDecoder().decode(pdfData.getData()), dokumenttiId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(path = "/pdf/tila/{dokumenttiId}")
    @ResponseBody
    public ResponseEntity<String> updateDokumenttiTila(@PathVariable("dokumenttiId") Long dokumenttiId,
                                                       @RequestBody PdfData pdfData) {
        service.updateDokumenttiTila(DokumenttiTila.of(pdfData.getTila()), dokumenttiId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
