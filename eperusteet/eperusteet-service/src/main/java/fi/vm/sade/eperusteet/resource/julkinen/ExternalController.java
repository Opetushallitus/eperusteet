package fi.vm.sade.eperusteet.resource.julkinen;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.JulkaisuSisaltoTyyppi;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiExternalDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenJulkaisuData;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaKaikkiDto;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import fi.vm.sade.eperusteet.service.OsaamismerkkiService;
import fi.vm.sade.eperusteet.service.PerusteService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@CrossOrigin
@RestController
@RequestMapping(value = "/api/external", produces = "application/json;charset=UTF-8")
@Tag(name = "External")
@Description("Perusteiden julkinen rajapinta")
public class ExternalController {

    @Autowired
    private JulkaisutService julkaisutService;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private OsaamismerkkiService osaamismerkkiService;

    private static final int DEFAULT_PATH_SKIP_VALUE = 5;
    @Autowired
    private JulkaisutRepository julkaisutRepository;

    @RequestMapping(value = "/peruste/{perusteId:\\d+}", method = GET)
    @ResponseBody
    @Operation(summary = "Perusteen tietojen haku")
    public ResponseEntity<PerusteKaikkiDto> getPeruste(
            @PathVariable("perusteId") final long id) {
        PerusteKaikkiDto peruste = perusteService.getJulkaistuSisalto(id);
        if (peruste == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(peruste);
    }

    @GetMapping(value = { "/perusteet"})
    @ResponseBody
    @Operation(summary = "Perusteiden haku")
    public ResponseEntity<Page<PerusteenJulkaisuData>> getPerusteet(
            @Parameter(description = "Koulutustyypin arvot. "
                    + "Jos parametreja ei anneta, haetaan kaikista tunnetuista koulutustyypeistä. "
                    + "Jos perusteen tyyppi on digitaalinen_osaaminen, koulutustyyppisuodatusta ei sovelleta.")
            @RequestParam(value = "koulutustyyppi", required = false) final List<String> koulutustyyppi,
            @Parameter(description = "Osittainen tekstihaku perusteen nimeen sekä julkaisutietoihin tallennettuihin "
                    + "osaamisalan, tutkintonimikkeen ja tutkinnon osan nimiin valitulla kielellä.")
            @RequestParam(value = "nimi", defaultValue = "", required = false) final String nimi,
            @Parameter(description = "Kieli, jolla nimivertailu tehdään ja johon nimitieto näytetään tuloksissa (esim. fi, sv, en). Oletus fi.")
            @RequestParam(value = "kieli", defaultValue = "fi", required = false) final String kieli,
            @Parameter(description = "Sisällytä perusteet, joiden voimaantuloajankohta on tulevaisuudessa suhteessa hakuhetkeen. Oletus true.")
            @RequestParam(value = "tulevat", defaultValue = "true", required = false) final boolean tulevat,
            @Parameter(description = "Sisällytä voimassa olevat perusteet (voimaantulo mennyt ja voimassaolo ei ole päättynyt hakuhetkeen). Oletus true.")
            @RequestParam(value = "voimassa", defaultValue = "true", required = false) final boolean voimassa,
            @Parameter(description = "Sisällytä perusteet, jotka ovat siirtymäajalla: virallinen voimassaolo on päättynyt mutta siirtymäpäättymispäivä ei ole vielä mennyt. Oletus true.")
            @RequestParam(value = "siirtyma", defaultValue = "true", required = false) final boolean siirtyma,
            @Parameter(description = "Sisällytä perusteet, joiden voimassaolo ja siirtymäaika ovat päättyneet hakuhetkeen mennessä. Oletus false.")
            @RequestParam(value = "poistuneet", defaultValue = "false", required = false) final boolean poistuneet,
            @Parameter(description = "Julkaistun perusteen tyyppi (esim. normaali, opas, digitaalinen_osaaminen). Oletus normaali.")
            @RequestParam(value = "tyyppi", defaultValue = "normaali", required = false) final String tyyppi,
            @Parameter(description = "Osittainen, kirjainkoosta riippumaton haku Opetushallituksen antamaan diaarinumeroon.")
            @RequestParam(value = "diaarinumero", defaultValue = "", required = false) final String diaarinumero,
            @Parameter(description = "Rajaa tulokset annettuun koodiin (täsmäosuma perusteen julkaisudatassa oleviin kooditietoihin, esim. koulutus- tai muun koodin URI). Tyhjä arvo ei rajaa.")
            @RequestParam(value = "koodi", defaultValue = "", required = false) final String koodi,
            @Parameter(description = "Sivutus: haettavan sivun numero (0-indeksoitu). Oletusarvo 0.")
            @RequestParam(value = "sivu", defaultValue = "0", required = false) final Integer sivu,
            @Parameter(description = "Sivutus: yhdellä sivulla palautettavien tulosten määrä. Oletusarvo 10. Maksimi 50.")
            @RequestParam(value = "sivukoko", defaultValue = "10", required = false) final Integer sivukoko) {
        return ResponseEntity.ok(julkaisutService.getJulkisetJulkaisut(
                koulutustyyppi, nimi, "", kieli, tyyppi, tulevat, voimassa, siirtyma, poistuneet, diaarinumero, koodi, JulkaisuSisaltoTyyppi.PERUSTE,
                sivu, Math.min(sivukoko, 50)));
    }

    @GetMapping(value = "/peruste/{perusteId:\\d+}/perusteenosa/{perusteenOsaId}")
    @ResponseBody
    @Operation(summary = "Perusteen osan haku")
    public ResponseEntity<PerusteenOsaDto> getJulkaistuPerusteenOsa(
            @PathVariable("perusteId") final long perusteId,
            @PathVariable("perusteenOsaId") final long perusteenOsaId) {
        PerusteenOsaDto perusteenOsa = perusteService.getJulkaistuPerusteenOsa(perusteId, perusteenOsaId);
        if (perusteenOsa == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(perusteenOsa);
    }

    @GetMapping(value = "/peruste/{perusteId:\\d+}/osaamisalakuvaukset")
    @ResponseBody
    @Operation(summary = "Perusteen osaamisalakuvauksien haku")
    public ResponseEntity<Map<Suoritustapakoodi, Map<String, List<TekstiKappaleDto>>>> getJulkaistutOsaamisalaKuvaukset(
            @PathVariable("perusteId") final long perusteId) {
        return ResponseEntity.ok(perusteService.getJulkaistutOsaamisalaKuvaukset(perusteId));
    }

    @GetMapping(value = "/peruste/{perusteId:\\d+}/{custompath}")
    @ResponseBody
    @Operation(
            summary = "Perusteen tietojen haku tarkalla sisältörakenteella",
            description = "Url parametreiksi voi antaa peruste id:n lisäksi erilaisia perusteen rakenteen osia ja id-kenttien arvoja. Esim. /peruste/8505691/tutkinnonOsat/7283253/koodi/nimi/fi antaa perusteen (id: 8505691) tutkinnon osien tietueen (id: 7283253) koodi-tiedon nimen suomenkielisenä."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PerusteKaikkiDto.class))}),
    })
    public ResponseEntity<Object> getPerusteDynamicQuery(HttpServletRequest req, @PathVariable("perusteId") final long id, @PathVariable("custompath") final String custompath ) {
        return getJulkaistuSisaltoObjectNodeWithQuery(id, requestToQueries(req, DEFAULT_PATH_SKIP_VALUE));
    }

    @Hidden
    // Springdoc ei generoi rajapintoja /** poluille, joten tämä on tehty erikseen
    @GetMapping(value = "/peruste/{perusteId:\\d+}/{custompath}/**")
    public ResponseEntity<Object> getPerusteDynamicQueryHidden(HttpServletRequest req, @PathVariable("perusteId") final long id) {
        return getJulkaistuSisaltoObjectNodeWithQuery(id, requestToQueries(req, DEFAULT_PATH_SKIP_VALUE));
    }

    @GetMapping(value = "/peruste/yto/**")
    @ResponseBody
    @Operation(summary = "Yhteisien tutkinnon osien -perusteen(YTO) tietojen haku tarkalla sisältörakenteella. Kts 'Perusteen tietojen haku tarkalla sisältörakenteella'")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PerusteKaikkiDto.class))}),
    })
    public ResponseEntity<Object> getYtoPerusteDynamicQuery(HttpServletRequest req) {
        Page<PerusteenJulkaisuData> amosaaPeruste = julkaisutService.getJulkisetJulkaisut(
                Collections.emptyList(),
                "",
                "",
                Kieli.FI.toString(),
                PerusteTyyppi.AMOSAA_YHTEINEN.toString(),
                false,
                false,
                false,
                false,
                "",
                "",
                JulkaisuSisaltoTyyppi.PERUSTE,
                0,
                10);

        if (amosaaPeruste.getTotalElements() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return getJulkaistuSisaltoObjectNodeWithQuery( amosaaPeruste.getContent().get(0).getId(), requestToQueries(req, DEFAULT_PATH_SKIP_VALUE));
    }

    @GetMapping(value = "/peruste/koulutuskoodi/{koodi:\\d+}/**")
    @ResponseBody
    @Operation(
            summary = "Perusteen haku koulutuskoodilla ja sisältörakenteella. Kts 'Perusteen tietojen haku tarkalla sisältörakenteella'.",
            description = "Statuskoodi 409 jos löytyy useampi peruste. Statuskoodi 204, jos haettu rakenne on tyhjä."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PerusteKaikkiDto.class))}),
    })
    public ResponseEntity<Object> getPerusteWithKoodi(
            HttpServletRequest req,
            @PathVariable("koodi") final long koodi) {
        Page<PerusteenJulkaisuData> peruste = julkaisutService.getJulkisetJulkaisut(
                Collections.emptyList(),
                "",
                "",
                Kieli.FI.toString(),
                PerusteTyyppi.NORMAALI.toString(),
                false,
                true,
                false,
                false,
                "",
                "koulutus_"+koodi,
                JulkaisuSisaltoTyyppi.PERUSTE,
                0,
                1);

        if (peruste.getTotalElements() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (peruste.getTotalElements() > 1) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        return getJulkaistuSisaltoObjectNodeWithQuery(peruste.getContent().get(0).getId(), requestToQueries(req, DEFAULT_PATH_SKIP_VALUE + 1));
    }

    @GetMapping(value = "/osaamismerkit")
    @ResponseBody
    @Operation(summary = "Hae kaikki julkaistut osaamismerkit")
    public ResponseEntity<List<OsaamismerkkiExternalDto>> getOsaamismerkit() {
        return ResponseEntity.ok(osaamismerkkiService.getOsaamismerkit());
    }

    @GetMapping(value = "/osaamismerkki/koodi/{uri}")
    @ResponseBody
    @Operation(summary = "Hae julkaistu osaamismerkki koodiurilla")
    public ResponseEntity<OsaamismerkkiDto> getOsaamismerkkiByUri(@PathVariable("uri") final String uri) {
        return ResponseEntity.ok(osaamismerkkiService.getOsaamismerkkiByUri(uri));
    }

    @GetMapping(value = "/peruste/tutkinnonosa/{tutkinnonOsaKoodiUri}")
    @ResponseBody
    @Operation(
            summary = "Tutkinnon osan haku tutkinnon osan koodin URI:lla."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = TutkinnonOsaKaikkiDto.class))}),
    })
    public ResponseEntity<Object> getTutkinnonOsaByKoodi(
            HttpServletRequest req,
            @PathVariable("tutkinnonOsaKoodiUri") final String tutkinnonOsaKoodiUri) {
        return ResponseEntity.ofNullable(julkaisutRepository.findTutkinnonOsaByTutkinnonOsaKoodi(tutkinnonOsaKoodiUri));
    }

    private List<String> requestToQueries(HttpServletRequest req, int skipCount) {
        String[] queries = req.getServletPath().split("/");
        return Arrays.stream(queries).skip(skipCount).collect(Collectors.toList());
    }

    private ResponseEntity<Object> getJulkaistuSisaltoObjectNodeWithQuery(long id, List<String> queries) {
        Object result = perusteService.getJulkaistuSisaltoObjectNode(id, queries);
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(result);
    }
}
