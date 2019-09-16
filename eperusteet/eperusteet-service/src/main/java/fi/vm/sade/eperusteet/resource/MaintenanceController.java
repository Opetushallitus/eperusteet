package fi.vm.sade.eperusteet.resource;


import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiImportDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.AmmattitaitovaatimusService;
import fi.vm.sade.eperusteet.service.ImportService;
import fi.vm.sade.eperusteet.service.MaintenanceService;
import fi.vm.sade.eperusteet.service.PerusteFactory;
import fi.vm.sade.eperusteet.service.yl.Lops2019Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@InternalApi
@Controller
@RestController
@RequestMapping(value = "/maintenance")
@Profile("!test")
public class MaintenanceController {
    @Autowired
    private PerusteFactory<ImportService> importService;

    @Autowired
    private AmmattitaitovaatimusService ammattitaitovaatimusService;

    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired
    private Lops2019Service lops2019Service;

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

    @RequestMapping(value = "/tuo", method = POST)
    public PerusteprojektiDto tuoPeruste(@RequestBody final PerusteprojektiImportDto importDto) {
        return importService.getStrategy(importDto.getProjekti().getToteutus())
                .tuoPerusteprojekti(importDto);
    }

}
