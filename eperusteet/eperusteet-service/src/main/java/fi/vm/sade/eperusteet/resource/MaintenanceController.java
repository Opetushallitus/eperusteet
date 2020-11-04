package fi.vm.sade.eperusteet.resource;


import fi.vm.sade.eperusteet.dto.ParsitutAmmattitaitovaatimukset;
import fi.vm.sade.eperusteet.dto.YllapitoDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiImportDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.*;
import fi.vm.sade.eperusteet.service.yl.Lops2019Service;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@InternalApi
@RestController
@RequestMapping(value = "/maintenance")
@Profile("!test")
@Api("Maintenance")
public class MaintenanceController {

    @Autowired
    private AmmattitaitovaatimusService ammattitaitovaatimusService;

    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private Lops2019Service lops2019Service;

    @Autowired
    CacheManager cacheManager;

    @RequestMapping(value = "/cacheclear/{cache}", method = GET)
    public ResponseEntity clearCache(@PathVariable final String cache) {
        cacheManager.getCache(cache).clear();
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

    @RequestMapping(value = "/validoi", method = GET)
    public void runValidointi() {
        maintenanceService.runValidointi();
    }

    @RequestMapping(value = "/julkaisut", method = GET)
    public void teeJulkaisut() {
        maintenanceService.teeJulkaisut();
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
    public List<YllapitoDto> sallitutYllapidot() {
        return maintenanceService.getSallitutYllapidot();
    }

    @ResponseBody
    @RequestMapping(value = "/ammattitaitovaatimuskoodisto", method = GET)
    public void pushAllAmmattitaitovaatimuksetToKoodisto() {
        ammattitaitovaatimusService.addAmmattitaitovaatimuskooditToKoodisto();
    }
}
