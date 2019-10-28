package fi.vm.sade.eperusteet.resource;


import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiImportDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.*;
import fi.vm.sade.eperusteet.service.yl.Lops2019Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@InternalApi
@RestController
@RequestMapping(value = "/maintenance")
@Profile("!test")
public class MaintenanceController {
    @Autowired
    private PerusteDispatcher dispatcher;

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

    @RequestMapping(value = "/export/{perusteId}", method = GET)
    public PerusteprojektiImportDto viePeruste(@PathVariable final Long perusteId) {
        PerusteprojektiImportDto result = perusteService.getPerusteExport(perusteId);
        return result;
    }

    @RequestMapping(value = "/import", method = POST)
    public PerusteprojektiDto tuoPeruste(@RequestBody final PerusteprojektiImportDto importDto) {
        return dispatcher.get(importDto.getPeruste(), PerusteImport.class)
                .tuoPerusteprojekti(importDto);
    }

}
