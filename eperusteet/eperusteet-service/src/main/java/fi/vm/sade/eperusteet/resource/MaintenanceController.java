package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.dto.ParsitutAmmattitaitovaatimukset;
import fi.vm.sade.eperusteet.dto.YllapitoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.service.AmmattitaitovaatimusService;
import fi.vm.sade.eperusteet.service.AmosaaClient;
import fi.vm.sade.eperusteet.service.MaintenanceService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@InternalApi
@RestController
@RequestMapping(value = "/api/maintenance")
@Profile("!test")
@Tag(name = "Maintenance")
public class MaintenanceController {

    @Autowired
    private AmmattitaitovaatimusService ammattitaitovaatimusService;

    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PermissionManager permissionManager;

    @Autowired
    private AmosaaClient amosaaClient;

    @RequestMapping(value = "/cacheclear/{cache}", method = GET)
    public ResponseEntity clearCache(@PathVariable final String cache) {
        maintenanceService.clearCache(cache);
        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }

    @RequestMapping(value = "/arvioinninammattitaitovaatimukset", method = GET)
    public ResponseEntity createArvioinninAmmattitaitovaatimukset() {
        ammattitaitovaatimusService.addAmmattitaitovaatimuskoodit();
        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }

    @RequestMapping(value = "/puuttuvatosaamisalakuvaukset", method = GET)
    public void addMissingOsaamisalakuvaukset() {
        maintenanceService.addMissingOsaamisalakuvaukset();
    }

    @RequestMapping(value = "/julkaisut", method = GET)
    public ResponseEntity<String> teeJulkaisut(
            @RequestParam(value = "julkaisekaikki", defaultValue = "false") boolean julkaiseKaikki,
            @RequestParam(value = "tyyppi", defaultValue = "NORMAALI") String tyyppi,
            @RequestParam(value = "koulutustyyppi", required = false) String koulutustyyppi,
            @RequestParam(value = "tiedote", defaultValue = "Ylläpidon suorittama julkaisu") String tiedote
    ) {
        if (!permissionManager.isUserAdmin()) {
            return ResponseEntity.badRequest().body("virhe käynnistyksessä");
        }
        maintenanceService.teeJulkaisut(julkaiseKaikki, tyyppi, koulutustyyppi, tiedote);
        return ResponseEntity.ok("julkaisut käynnistetty");
    }

    @RequestMapping(value = "/julkaise/{perusteId}", method = POST)
    public void teeMaintenanceJulkaisu(@PathVariable final Long perusteId, @RequestParam(value = "tiedote", defaultValue = "Ylläpidon suorittama julkaisu") String tiedote) {
        maintenanceService.teeJulkaisu(perusteId, tiedote);
    }

    @RequestMapping(value = "/export/{perusteId}", method = GET, produces = "application/zip")
    public ResponseEntity<StreamingResponseBody> viePeruste(@PathVariable final Long perusteId) {
        String archiveFilename = new SimpleDateFormat("yyyyMMddHHmmss'.zip'").format(new Date());
        return ResponseEntity
                .ok()
                .header("Content-Disposition", "attachment; filename=\"peruste-" + perusteId + "-" + archiveFilename + "\"")
                .body(out -> {
                    ZipOutputStream zipOutputStream = new ZipOutputStream(out);
                    perusteService.exportPeruste(perusteId, zipOutputStream);
                    zipOutputStream.close();
                });
    }

    @RequestMapping(value = "/export/{perusteId}/json", method = GET, produces = "application/json")
    public ResponseEntity<PerusteKaikkiDto> viePerusteJson(@PathVariable final Long perusteId) {
        return ResponseEntity.ok(perusteService.getKaikkiSisalto(perusteId));
    }

    @RequestMapping(value = "/import", method = POST)
    public void tuoPeruste(MultipartHttpServletRequest request) throws IOException {
        perusteService.importPeruste(request);
    }

    @RequestMapping(value = "/lisaaAmmattitaitovaatimukset2019/{perusteId}", method = GET)
    public void lisaaAmmattitaitovaatimuksetPerusteelle(@PathVariable final Long perusteId) {
        ammattitaitovaatimusService.updateAmmattitaitovaatimukset(perusteId);
    }

    @RequestMapping(value = "/virheellisetAmmattitaitovaatimukset", method = GET)
    public List<ParsitutAmmattitaitovaatimukset> virheellisetAmmattitaitovaatimukset() {
        return ammattitaitovaatimusService.virheellisetAmmattitaitovaatimukset();
    }

    @RequestMapping(value = "/yllapito", method = GET)
    public ResponseEntity<List<YllapitoDto>> getYllapidot() {
        return ResponseEntity.ok(maintenanceService.getYllapidot());
    }

    @RequestMapping(value = "/yllapito/{key}", method = GET)
    public ResponseEntity<String> getYllapito(@PathVariable final String key) {
        return ResponseEntity.ok(maintenanceService.getYllapitoValue(key));
    }

    @RequestMapping(value = "/yllapito/update", method = POST)
    public ResponseEntity updateYllapito(@RequestBody List<YllapitoDto> yllapitoDtoList) {
        maintenanceService.updateYllapito(yllapitoDtoList);
        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }

    @ResponseBody
    @RequestMapping(value = "/ammattitaitovaatimuskoodisto", method = GET)
    public void pushAllAmmattitaitovaatimuksetToKoodisto() {
        ammattitaitovaatimusService.addAmmattitaitovaatimuskooditToKoodisto();
    }

    @GetMapping(value = "/maarayksetperusteille")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> maarayksetperusteille() {
        maintenanceService.teeMaarayksetPerusteille();
        return ResponseEntity.ok("Määräykset luotu");
    }

    @GetMapping(value = "/amosaa/koulutustoimija/paivitys")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> paivitaAmosaaKoulutustoimijat() {
        amosaaClient.paivitaAmosaaKoulutustoimijat();
        return ResponseEntity.ok("paivitys kaynnistetty");
    }
}
