package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.KoulutustyyppiLukumaara;
import fi.vm.sade.eperusteet.dto.PerusteTekstikappaleillaDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.peruste.*;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.Ammattitaitovaatimus2019Dto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteSuppeaDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.resource.util.CacheableResponse;
import fi.vm.sade.eperusteet.service.AmmattitaitovaatimusService;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.PerusteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping(value = "/api/perusteet", produces = "application/json;charset=UTF-8")
@Tag(name = "Perusteet")
public class PerusteController {

    @Autowired
    private KoodistoClient koodistoService;

    @Autowired
    private PerusteService service;

    @Autowired
    private AmmattitaitovaatimusService ammattitaitovaatimusService;

    @RequestMapping(value = "/info", method = GET)
    @ResponseBody
    @InternalApi
    public Page<PerusteInfoDto> getAllPerusteetInfo(PerusteQuery pquery) {
        PageRequest p = PageRequest.of(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return service.findByInfo(p, pquery);
    }

    @RequestMapping(value = "/valittavatkielet", method = GET)
    @ResponseBody
    @InternalApi
    public ResponseEntity<List<String>> getValittavatKielet() {
        return new ResponseEntity<>(Kieli.vaihtoehdot(), HttpStatus.OK);
    }

    @RequestMapping(value = "/kooste", method = GET)
    @ResponseBody
    @Parameter(hidden = true)
    public ResponseEntity<List<PerusteKoosteDto>> getPerusteKooste() {
        return new ResponseEntity<>(service.getKooste(), HttpStatus.OK);
    }

    @RequestMapping(value = "/uusimmat", method = GET)
    @ResponseBody
    public ResponseEntity<List<PerusteDto>> getUusimmatPerusteet(@RequestParam(defaultValue = "fi") String kieli) {
        HashSet<Kieli> kielet = new HashSet<>();
        kielet.add(Kieli.of(kieli));
        return new ResponseEntity<>(service.getUusimmat(kielet), HttpStatus.OK);
    }

    @RequestMapping(value = "/perusopetus", method = GET)
    @ResponseBody
    @Parameter(hidden = true)
    public ResponseEntity<List<PerusteInfoDto>> getAllPerusopetus() {
        List<PerusteInfoDto> poi = service.getAllPerusopetusInfo();
        return new ResponseEntity<>(poi, HttpStatus.OK);
    }

    @RequestMapping(method = GET)
    @ResponseBody
    @Operation(summary = "perusteiden sisäinen haku")
    @Parameters({
            @Parameter(name = "sivu", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "sivukoko", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "tuleva", schema = @Schema(implementation = Boolean.class, defaultValue = "true"), in = ParameterIn.QUERY, description = "hae myös tulevatperusteet"),
            @Parameter(name = "siirtyma", schema = @Schema(implementation = Boolean.class, defaultValue = "true"), in = ParameterIn.QUERY, description = "hae myös siirtymäajalla olevat perusteet"),
            @Parameter(name = "voimassaolo", schema = @Schema(implementation = Boolean.class, defaultValue = "true"), in = ParameterIn.QUERY, description = "hae myös voimassaolevat perusteet"),
            @Parameter(name = "poistunut", schema = @Schema(implementation = Boolean.class, defaultValue = "true"), in = ParameterIn.QUERY, description = "hae myös poistuneet perusteet"),
            @Parameter(name = "nimi", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "koulutusala", in = ParameterIn.QUERY, array =  @ArraySchema(schema = @Schema(type = "string"))),
            @Parameter(name = "koulutustyyppi", in = ParameterIn.QUERY, array = @ArraySchema(schema = @Schema(type = "string")), description = "koulutustyyppi (koodistokoodi)"),
            @Parameter(name = "kieli", in = ParameterIn.QUERY, array = @ArraySchema(schema = @Schema(type = "string")), description = "perusteen kieli"),
            @Parameter(name = "opintoala", in = ParameterIn.QUERY, array = @ArraySchema(schema = @Schema(type = "string")), description = "opintoalakoodi"),
            @Parameter(name = "suoritustapa", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, description = "AM-perusteet; naytto tai ops"),
            @Parameter(name = "koulutuskoodi", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "diaarinumero", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "muokattu", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY, description = "Perustetta muokattu jälkeen (aikaleima; millisenkunteja alkaen 1970-01-01 00:00:00 UTC). Huomioi koko perusteen sisällön."),
            @Parameter(name = "tutkintonimikkeet", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY, description = "hae myös tutkintonimikkeistä"),
            @Parameter(name = "tutkinnonosat", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY, description = "hae myös tutkinnon osista"),
            @Parameter(name = "osaamisalat", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY, description = "hae myös osaamisaloista"),
            @Parameter(name = "koulutusvienti", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY, description = "Haku ainoastaan koulutusviennistä"),
            @Parameter(name = "perusteTyyppi", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, description = "Perusteen tyyppi"),
            @Parameter(name = "julkaistu", schema = @Schema(implementation = Boolean.class, defaultValue = "false"), in = ParameterIn.QUERY, description = "julkaistut perusteet"),
            @Parameter(name = "tutkinnonosaKoodit", in = ParameterIn.QUERY, array = @ArraySchema(schema = @Schema(type = "string"))),
            @Parameter(name = "osaamisalaKoodit", in = ParameterIn.QUERY, array = @ArraySchema(schema = @Schema(type = "string"))),
    })
    public Page<PerusteHakuDto> getAllPerusteet(@Parameter(hidden = true) PerusteQuery pquery) {
        PageRequest p = PageRequest.of(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return service.findJulkinenBy(p, pquery);
    }

    @RequestMapping(value = "/internal", method = GET)
    @ResponseBody
    @Operation(summary = "perusteiden sisäinen haku")
    @Parameters({
            @Parameter(name = "sivu", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "sivukoko", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "tuleva", schema = @Schema(implementation = Boolean.class, defaultValue = "true"), in = ParameterIn.QUERY, description = "hae myös tulevatperusteet"),
            @Parameter(name = "siirtyma", schema = @Schema(implementation = Boolean.class, defaultValue = "true"), in = ParameterIn.QUERY, description = "hae myös siirtymäajalla olevat perusteet"),
            @Parameter(name = "voimassaolo", schema = @Schema(implementation = Boolean.class, defaultValue = "true"), in = ParameterIn.QUERY, description = "hae myös voimassaolevat perusteet"),
            @Parameter(name = "poistunut", schema = @Schema(implementation = Boolean.class, defaultValue = "true"), in = ParameterIn.QUERY, description = "hae myös poistuneet perusteet"),
            @Parameter(name = "nimi", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "koulutusala", in = ParameterIn.QUERY, array = @ArraySchema(schema = @Schema(type = "string"))),
            @Parameter(name = "koulutustyyppi", in = ParameterIn.QUERY, array = @ArraySchema(schema = @Schema(type = "string")), description = "koulutustyyppi (koodistokoodi)"),
            @Parameter(name = "kieli", in = ParameterIn.QUERY, array = @ArraySchema(schema = @Schema(type = "string")), description = "perusteen kieli"),
            @Parameter(name = "opintoala", in = ParameterIn.QUERY, array = @ArraySchema(schema = @Schema(type = "string")), description = "opintoalakoodi"),
            @Parameter(name = "suoritustapa", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, description = "AM-perusteet; naytto tai ops"),
            @Parameter(name = "koulutuskoodi", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "diaarinumero", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "muokattu", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY, description = "Perustetta muokattu jälkeen (aikaleima; millisenkunteja alkaen 1970-01-01 00:00:00 UTC). Huomioi koko perusteen sisällön."),
            @Parameter(name = "tutkintonimikkeet", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY, description = "hae myös tutkintonimikkeistä"),
            @Parameter(name = "tutkinnonosat", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY, description = "hae myös tutkinnon osista"),
            @Parameter(name = "osaamisalat", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY, description = "hae myös osaamisaloista"),
            @Parameter(name = "koulutusvienti", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY, description = "Haku ainoastaan koulutusviennistä"),
            @Parameter(name = "tila", in = ParameterIn.QUERY, array = @ArraySchema(schema = @Schema(type = "string")), description = "Sallitut tilat"),
            @Parameter(name = "perusteTyyppi", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, description = "Perusteen tyyppi"),
            @Parameter(name = "julkaistu", schema = @Schema(implementation = Boolean.class, defaultValue = "false"), in = ParameterIn.QUERY, description = "julkaistut perusteet"),
    })
    public Page<PerusteHakuInternalDto> getAllPerusteetInternal(@Parameter(hidden = true) PerusteQuery pquery) {
        PageRequest p = PageRequest.of(pquery.getSivu(), Math.min(pquery.getSivukoko(), 1000));
        return service.findByInternal(p, pquery);
    }

    @RequestMapping(value = "/internal/pohjat", method = GET)
    @ResponseBody
    public List<PerusteKevytDto> getPohjaperusteet(@RequestParam(value = "perustetyyppi", required = false, defaultValue = "normaali") final String perustetyyppi) {
        return service.getPohjaperusteet(PerusteTyyppi.of(perustetyyppi));
    }

    @RequestMapping(value = "/internal/julkaistut", method = GET)
    @ResponseBody
    public List<PerusteKevytDto> getJulkaistutPerusteet() {
        return service.getJulkaistutPerusteet();
    }

    @RequestMapping(value = "/{perusteId}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @InternalApi
    public PerusteDto updatePeruste(@PathVariable("perusteId") final long id, @RequestBody PerusteDto perusteDto) {
        return service.updateFull(id, perusteDto);
    }

    @InternalApi
    @RequestMapping(value = "/{perusteId}/navigaatio", method = GET)
    public NavigationNodeDto getNavigation(
            @PathVariable final Long perusteId,
            @RequestParam(value = "kieli", required = false, defaultValue = "fi") final String kieli
    ) {
        return service.buildNavigation(perusteId, kieli);
    }

    @InternalApi
    @RequestMapping(value = "/{perusteId}/navigaatio/public", method = GET)
    public NavigationNodeDto getNavigationPublic(
            @PathVariable final Long perusteId,
            @RequestParam(value = "kieli", required = false, defaultValue = "fi") final String kieli,
            @RequestParam(required = false) boolean esikatselu,
            @RequestParam(required = false) Integer rev
    ) {
        return service.buildNavigationPublic(perusteId, kieli, esikatselu, rev);
    }

    @RequestMapping(value = "/{perusteId}/kvliite", method = GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public KVLiiteLaajaDto getKvLiite(
            @PathVariable("perusteId") final long id) {
        return service.getJulkinenKVLiite(id);
    }

    @RequestMapping(value = "/{perusteId}/kvliite", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @InternalApi
    public PerusteDto updateKvLiite(
            @PathVariable("perusteId") final long id,
            @RequestBody KVLiiteLaajaDto kvliiteDto
    ) {
        return service.updateKvLiite(id, kvliiteDto);
    }

    @RequestMapping(value = "/{perusteId}/tutkintonimikekoodit/{tutkintonimikeKoodiId}", method = DELETE)
    @ResponseBody
    @InternalApi
    public ResponseEntity<TutkintonimikeKoodiDto> removeTutkintonimikekoodi(
            @PathVariable("perusteId") final long id,
            @PathVariable("tutkintonimikeKoodiId") final Long tnkId) {
        service.removeTutkintonimikeKoodi(id, tnkId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{perusteId}/tutkintonimikekoodit", method = POST)
    @ResponseBody
    @InternalApi
    public ResponseEntity<TutkintonimikeKoodiDto> addTutkintonimikekoodi(
            @PathVariable("perusteId") final long id,
            @RequestBody final TutkintonimikeKoodiDto tnk) {
        TutkintonimikeKoodiDto tutkintonimikeKoodi = service.addTutkintonimikeKoodi(id, tnk);
        return new ResponseEntity<>(tutkintonimikeKoodi, HttpStatus.OK);
    }

    @RequestMapping(value = "/{perusteId}/tutkintonimikkeet", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @InternalApi
    public void updateTutkintonimikkeet(
            @PathVariable("perusteId") final long id,
            @RequestBody final List<TutkintonimikeKoodiDto> tutkintonimikkeet) {
        service.updateTutkintonimikkeet(id, tutkintonimikkeet);
    }

    // Openapi generator
    @RequestMapping(value = "/{perusteId}/tutkintonimikekoodit", method = PUT)
    @ResponseBody
    @InternalApi
    public ResponseEntity<TutkintonimikeKoodiDto> addTutkintonimikekoodiPut(
            @PathVariable("perusteId") final long id,
            @RequestBody final TutkintonimikeKoodiDto tnk) {
        return addTutkintonimikekoodi(id, tnk);
    }

    @RequestMapping(value = "/{perusteId}/tutkintonimikekoodit", method = GET)
    @ResponseBody
    @InternalApi
    public ResponseEntity<List<CombinedDto<TutkintonimikeKoodiDto, HashMap<String, KoodistoKoodiDto>>>> getTutkintonimikekoodit(@PathVariable("perusteId") final long id) {
        List<TutkintonimikeKoodiDto> tutkintonimikeKoodit = service.getTutkintonimikeKoodit(id);
        List<CombinedDto<TutkintonimikeKoodiDto, HashMap<String, KoodistoKoodiDto>>> response = new ArrayList<>();

        for (TutkintonimikeKoodiDto tkd : tutkintonimikeKoodit) {
            HashMap<String, KoodistoKoodiDto> nimet = new HashMap<>();
            if (tkd.getOsaamisalaUri() != null) {
                nimet.put(tkd.getOsaamisalaArvo(), koodistoService.get("osaamisala", tkd.getOsaamisalaUri()));
            }
            nimet.put(tkd.getTutkintonimikeArvo(), koodistoService.get("tutkintonimikkeet", tkd.getTutkintonimikeUri()));
            if (tkd.getTutkinnonOsaUri() != null) {
                nimet.put(tkd.getTutkinnonOsaArvo(), koodistoService.get("tutkinnonosat", tkd.getTutkinnonOsaUri()));
            }
            response.add(new CombinedDto<>(tkd, nimet));
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/{perusteId}/osaamisalat", method = POST)
    @InternalApi
    @ResponseStatus(HttpStatus.OK)
    public void updateOsaamisalat(
            @PathVariable("perusteId") final long id,
            @RequestBody final Set<KoodiDto> osaamisalat) {
        service.updateOsaamisalat(id, osaamisalat);
    }

    @RequestMapping(value = "/{perusteId}/meta", method = GET)
    @ResponseBody
    @Operation(summary = "perusteen tietojen haku")
    public ResponseEntity<PerusteInfoDto> getMeta(@PathVariable("perusteId") final long id) {
        return ResponseEntity.ok(service.getMeta(id));
    }

    @RequestMapping(value = "/{perusteId}", method = GET)
    @ResponseBody
    @Operation(summary = "perusteen tietojen haku")
    public ResponseEntity<PerusteDto> getPerusteenTiedot(@PathVariable("perusteId") final long id) {
        return handleGet(id, 1, () -> service.get(id));
    }

    @RequestMapping(value = "/{perusteId}/projektitila", method = GET)
    @ResponseBody
    @Operation(summary = "perusteprojektin tila")
    public ResponseEntity<ProjektiTila> getPerusteProjektiTila(@PathVariable("perusteId") final long id) {
        return ResponseEntity.ok(service.getPerusteProjektiTila(id));
    }

    @RequestMapping(value = "/{perusteId}/version", method = GET)
    @ResponseBody
    @Operation(summary = "perusteen uusin versio")
    public PerusteVersionDto getPerusteVersion(@PathVariable("perusteId") final long id) {
        return service.getPerusteVersion(id);
    }

    @RequestMapping(value = "/{perusteId}/osaamisalakuvaukset", method = GET)
    @ResponseBody
    @Operation(summary = "perusteen osaamisalojen kuvaukset koulutustarjontaa varten")
    public ResponseEntity<Map<Suoritustapakoodi, Map<String, List<TekstiKappaleDto>>>> getOsaamisalat(@PathVariable("perusteId") final long id) {
        return new ResponseEntity<>(service.getOsaamisalaKuvaukset(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/amosaapohjat", method = GET)
    @ResponseBody
    @InternalApi
    @Operation(summary = "Ammatillisien jaettujen osien pohjat")
    public ResponseEntity<List<PerusteKaikkiDto>> getAmosaaPohjat() {
        return new ResponseEntity<>(service.getAmosaaYhteisetPohjat(), HttpStatus.OK);
    }

    @GetMapping("/amosaapohja/{id}")
    public ResponseEntity<PerusteKaikkiDto> getAmosaaPohja(@PathVariable("id") Long id) {
        return new ResponseEntity<>(service.getAmosaaYhteinenPohja(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/diaari", method = GET)
    @ResponseBody
    @Parameters({
            @Parameter(name = "diaarinumero", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY)
    })
    @Operation(summary = "perusteen yksilöintietojen haku diaarinumerolla")
    public ResponseEntity<PerusteInfoDto> getByDiaari(@Parameter(hidden = true) final Diaarinumero diaarinumero) {
        PerusteInfoDto t = service.getByDiaari(diaarinumero);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/{perusteId}/kaikki", method = GET)
    @ResponseBody
    @Operation(summary = "perusteen kaikkien tietojen haku")
    public ResponseEntity<PerusteKaikkiDto> getKokoSisalto(
            @PathVariable("perusteId") final long id,
            @RequestParam(value = "rev", required = false) final Integer rev,
            @RequestParam(value = "useCurrentData", required = false, defaultValue = "false") final boolean useCurrentData) {
        return new ResponseEntity<>(service.getJulkaistuSisalto(id, rev, useCurrentData), HttpStatus.OK);
    }

    @RequestMapping(value = "/{perusteId}/kaikki/tutkinnonosat", method = GET)
    @ResponseBody
    @Operation(summary = "perusteen tutkinnon osien haku julkaistusta datasta")
    public ResponseEntity<List<TutkinnonOsaKaikkiDto>> getJulkaistutTutkinnonOsat(
            @PathVariable("perusteId") final long id,
            @RequestParam(value = "useCurrentData", required = false, defaultValue = "false") final boolean useCurrentData) {
        return handleGet(id, 3600, () -> service.getJulkaistutTutkinnonOsat(id, useCurrentData));
    }

    @RequestMapping(value = "/{perusteId}/kaikki/tutkinnonosaviitteet", method = GET)
    @ResponseBody
    @Operation(summary = "perusteen tutkinnon osien viitteiden haku julkaistusta datasta")
    public ResponseEntity<Set<TutkinnonOsaViiteSuppeaDto>> getJulkaistutTutkinnonOsaViitteet(
            @PathVariable("perusteId") final long id,
            @RequestParam(value = "useCurrentData", required = false, defaultValue = "false") final boolean useCurrentData) {
        return handleGet(id, 3600, () -> service.getJulkaistutTutkinnonOsaViitteet(id, useCurrentData));
    }

    @RequestMapping(value = "/{perusteId}/suoritustavat/{suoritustapakoodi}", method = GET)
    @ResponseBody
    @InternalApi
    public ResponseEntity<SuoritustapaDto> getSuoritustapa(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("suoritustapakoodi") final String suoritustapakoodi
    ) {
        return handleGet(perusteId, 1, () -> service.getSuoritustapa(perusteId, Suoritustapakoodi.of(suoritustapakoodi)));
    }

    private <T> ResponseEntity<T> handleGet(Long perusteId, int age, Supplier<T> response) {
        return CacheableResponse.create(service.getPerusteVersion(perusteId), age, response::get);
    }

    @ResponseBody
    @RequestMapping(value = "/{perusteId}/tutkinnonosat/ammattitaitovaatimuskoodisto", method = POST)
    public List<KoodiDto> pushAmmattitaitovaatimuksetToKoodisto(
            @PathVariable("perusteId") final Long perusteId) {
        return ammattitaitovaatimusService.addAmmattitaitovaatimuskooditToKoodisto(perusteId);
    }

    @ResponseBody
    @RequestMapping(value = "/{perusteId}/tutkinnonosat/ammattitaitovaatimukset", method = GET)
    public List<Ammattitaitovaatimus2019Dto> getAmmattitaitovaatimukset(
            @PathVariable("perusteId") final Long perusteId) {
        return ammattitaitovaatimusService.getAmmattitaitovaatimukset(perusteId);
    }

    @RequestMapping(value = "/tekstikappale", method = GET)
    @ResponseBody
    public List<PerusteTekstikappaleillaDto> getPerusteetWithTekstikappaleKoodi(@RequestParam(value = "koodi", required = true) final String koodi) {
        return service.findByTekstikappaleKoodi(koodi);
    }

    @RequestMapping(value = "/oppaiden", method = GET)
    @ResponseBody
    public ResponseEntity<List<PerusteKevytDto>> getAllOppaidenPerusteet() {
        List<PerusteKevytDto> poi = service.getAllOppaidenPerusteet();
        return new ResponseEntity<>(poi, HttpStatus.OK);
    }

    @RequestMapping(value = "/aikataululliset", method = GET)
    @ResponseBody
    @Operation(summary = "Perusteet julkisilla aikatauluillla")
    public Page<PerusteBaseDto> getJulkaisuAikatauluPerusteet(
            @RequestParam(value = "sivu") final Integer sivu,
            @RequestParam(value = "sivukoko") final Integer sivukoko,
            @RequestParam(value = "koulutustyyppi") final List<String> koulutustyypit
    ) {
        return service.getJulkaisuAikatauluPerusteet(sivu, sivukoko, koulutustyypit);
    }

    @RequestMapping(value = "/lukumaara", method = GET)
    @ResponseBody
    @Operation(summary = "Perusteiden koulutustyyppikohtaiset lukumäärät")
    public List<KoulutustyyppiLukumaara> getJulkaistutLukumaarilla(
            @RequestParam(value = "koulutustyyppi") final List<String> koulutustyypit
    ) {
        return service.getVoimassaolevatJulkaistutPerusteLukumaarat(koulutustyypit);
    }

    @RequestMapping(value = "/julkaistutkoulutustyypit", method = GET)
    @ResponseBody
    @Operation(summary = "Julkaistut perustekoulutustyypit annetulla kielellä")
    public List<KoulutusTyyppi> getJulkaistutKoulutustyypit(@RequestParam(defaultValue = "fi") String kieli) {
        return service.getJulkaistutKoulutustyyppit(Kieli.of(kieli));
    }

    @RequestMapping(value = "/julkaistutkoulutustyyppimaarat", method = GET)
    @ResponseBody
    @Operation(summary = "Julkaistut perustekoulutustyypit annetulla kielellä")
    public List<KoulutustyyppiLukumaara> getJulkaistutKoulutustyyppiLukumaarat(@RequestParam(defaultValue = "fi") String kieli) {
        return service.getJulkaistutKoulutustyyppiLukumaarat(Kieli.of(kieli));
    }

    @RequestMapping(value = "/opaskoodikiinnitys/{koodiUri}", method = GET)
    @ResponseBody
    @Operation(summary = "Oppaat joihin kiinnitetty koodiUri")
    public List<PerusteDto> getOpasKiinnitettyKoodi(@PathVariable("koodiUri") final String koodiUri) {
        return service.getOpasKiinnitettyKoodi(koodiUri);
    }

    @RequestMapping(value = "/peruste/korvattavatperusteet/{perusteId}", method = GET)
    @ResponseBody
    @Operation(summary = "Perusteet jotka peruste korvaa")
    public List<PerusteInfoDto> getKorvattavatPerusteet(@PathVariable("perusteId") final long perusteId) {
        return service.getKorvattavatPerusteet(perusteId);
    }

    @GetMapping(value = "/julkisivunkoosteperusteet")
    @ResponseBody
    public List<PerusteKevytDto> getJulkaistutKoostePerusteet() {
        return service.getJulkaistutKoostePerusteet();
    }

    @PostMapping(value = "/julkisivunkoosteperusteet")
    @ResponseBody
    public ResponseEntity<Void> updateJulkaistutKoostePerusteet(@RequestBody List<PerusteKevytDto> perusteet) {
        service.updateJulkaistutKoostePerusteet(perusteet);
        return ResponseEntity.ok().build();
    }

}
