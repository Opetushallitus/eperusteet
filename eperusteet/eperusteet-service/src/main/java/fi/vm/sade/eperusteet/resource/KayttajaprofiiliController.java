package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.kayttaja.KayttajaProfiiliDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajaprofiiliPreferenssiDto;
import fi.vm.sade.eperusteet.dto.kayttaja.SuosikkiDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.KayttajaprofiiliService;
import fi.vm.sade.eperusteet.service.SuosikkiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/kayttajaprofiili")
@InternalApi
public class KayttajaprofiiliController {

    @Autowired
    private KayttajaprofiiliService service;

    @Autowired
    private SuosikkiService suosikkiService;

    @RequestMapping(value = "", method = GET)
    @ResponseBody
    public ResponseEntity<KayttajaProfiiliDto> getKayttajaprofiili() {
        KayttajaProfiiliDto k = service.get();
        if (k == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(k, HttpStatus.OK);
    }

    @RequestMapping(value = "/suosikki", method = POST, consumes="application/json")
    @ResponseBody
    public ResponseEntity<KayttajaProfiiliDto> addKayttajaprofiiliSuosikki(
            @RequestBody SuosikkiDto suosikkiDto) {
        KayttajaProfiiliDto profiiliDto = service.addSuosikki(suosikkiDto);
        return new ResponseEntity<>(profiiliDto, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/preferenssi", method = POST, consumes="application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void setKayttajaprofiiliPreferenssi(
            @RequestBody KayttajaprofiiliPreferenssiDto preferenssiDto) {
        service.setPreference(preferenssiDto);
    }

    @RequestMapping(value = "/suosikki/{suosikkiId}", method = DELETE)
    @ResponseBody
    public ResponseEntity<KayttajaProfiiliDto> deleteKayttajaprofiiliSuosikki(
            @PathVariable("suosikkiId") final Long suosikkiId) {
        SuosikkiDto suosikki = suosikkiService.get(suosikkiId);
        if (suosikki == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        KayttajaProfiiliDto k = service.deleteSuosikki(suosikkiId);
        return new ResponseEntity<>(k, HttpStatus.OK);
    }

    @RequestMapping(value = "/suosikki/{suosikkiId}", method = POST)
    @ResponseBody
    public ResponseEntity<KayttajaProfiiliDto> updateKayttajaprofiiliSuosikki(
            @RequestBody SuosikkiDto suosikkiDto,
            @PathVariable("suosikkiId") final Long suosikkiId
    ) {
        KayttajaProfiiliDto k = service.updateSuosikki(suosikkiId, suosikkiDto);
        return new ResponseEntity<>(k, HttpStatus.OK);
    }

}
