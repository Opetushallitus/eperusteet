package fi.vm.sade.eperusteet.resource;


import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.AmmattitaitovaatimusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@InternalApi
@Controller
@RequestMapping(value = "/maintenance")
public class MaintenanceController {

    @Autowired
    private AmmattitaitovaatimusService ammattitaitovaatimusService;

    @RequestMapping(value = "/arvioinninammattitaitovaatimukset", method = GET)
    @ResponseBody
    public ResponseEntity createArvioinninAmmattitaitovaatimukset() {
        ammattitaitovaatimusService.addAmmattitaitovaatimuskoodit();
        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }
}
