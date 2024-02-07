package fi.vm.sade.eperusteet.resource;

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoPageDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.KoodistoPagedService;
import fi.vm.sade.eperusteet.service.UlkopuolisetService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/ulkopuoliset")
@Api("Ulkopuoliset")
@InternalApi
public class UlkopuolisetController {
    @Autowired
    private UlkopuolisetService service;

    @Autowired
    private KayttajanTietoService kayttajanTietoService;

    @Autowired
    KoodistoPagedService koodistoPagedService;

    @RequestMapping(value = "/organisaatioryhmat", method = GET)
    @ResponseBody
    public ResponseEntity<JsonNode> getOrganisaatioRyhmat() {
        JsonNode ryhmat = service.getRyhmat();
        return new ResponseEntity<>(ryhmat, HttpStatus.OK);
    }

    @RequestMapping(value = "/organisaatioryhmat/{oid}", method = GET)
    @ResponseBody
    public ResponseEntity<JsonNode> getOrganisaatioRyhmatByOid(@PathVariable(value = "oid") final String oid) {
        JsonNode ryhma = service.getRyhma(oid);
        return new ResponseEntity<>(ryhma, HttpStatus.OK);
    }

    @RequestMapping(value = "/organisaatiovirkailijat/{oid}", method = GET)
    @ResponseBody
    public ResponseEntity<JsonNode> getOrganisaatioVirkailijat(@PathVariable(value = "oid") final String oid) {
        return new ResponseEntity<>(kayttajanTietoService.getOrganisaatioVirkailijat(oid), HttpStatus.OK);
    }

    @RequestMapping(value = "/{koodisto}", method = GET)
    public ResponseEntity<Page<KoodistoKoodiDto>> getKoodisto(
            @PathVariable("koodisto") final String koodisto,
            @RequestParam(value = "haku", required = false) final String haku,
            KoodistoPageDto koodistoPageDto) {

        return new ResponseEntity<>(koodistoPagedService.getAllPaged(koodisto, haku, koodistoPageDto), HttpStatus.OK);
    }
}
