package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.kayttaja.HenkiloTietoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.BooleanDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.resource.util.CacheControl;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.TutkinnonOsaViiteService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import io.swagger.annotations.Api;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/tutkinnonosat")
@Api(value = "TutkinnonosatPrivate", description = "Tutkinnon osien hallinta")
@InternalApi
public class TutkinnonOsaController {

    @Autowired
    private TutkinnonOsaViiteService tutkinnonOsaViiteService;

    @Autowired
    private KayttajanTietoService kayttajanTietoService;

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @RequestMapping(value = "/koodi/uniikki/{koodiUri}", method = GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<BooleanDto> getTutkinnonOsaByKoodi(@PathVariable("koodiUri") final String koodiUri) {
        try {
            perusteenOsaService.onkoTutkinnonOsanKoodiKaytossa(koodiUri);
        } catch (BusinessRuleViolationException ex) {
            return new ResponseEntity<>(new BooleanDto(false), HttpStatus.OK);
        }

        return new ResponseEntity<>(new BooleanDto(true), HttpStatus.OK);
    }

    @RequestMapping(value = "/koodi/uniikit", method = GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> getTutkinnonOsaByKoodit(@RequestParam(value = "koodiuri") final List<String> koodiUrit) {
        return new ResponseEntity<>(perusteenOsaService.onkoTutkinnonOsanKoodiKaytossa(koodiUrit), HttpStatus.OK);
    }

    @RequestMapping(value = "/viite/{id}/versiot", method = GET)
    @ResponseBody
    public List<CombinedDto<Revision, HenkiloTietoDto>> getViiteVersiot(@PathVariable("id") final Long id) {
        List<Revision> versiot = tutkinnonOsaViiteService.getVersiot(id);
        List<CombinedDto<Revision, HenkiloTietoDto>> laajennetut = new ArrayList<>();
        for (Revision r : versiot) {
            laajennetut.add(new CombinedDto<>(r, new HenkiloTietoDto(kayttajanTietoService.hae(r.getMuokkaajaOid()))));
        }
        return laajennetut;
    }

    @RequestMapping(value = "/viite/{id}/versio/{versioId}", method = GET)
    @ResponseBody
    @CacheControl(age = CacheControl.ONE_YEAR)
    public ResponseEntity<TutkinnonOsaViiteDto> getViiteVersio(@PathVariable("id") final Long id, @PathVariable("versioId") final Integer versioId) {
        TutkinnonOsaViiteDto t = tutkinnonOsaViiteService.getVersio(id, versioId);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/palauta/viite/{id}/versio/{versioId}", method = POST)
    @ResponseBody
    public ResponseEntity<TutkinnonOsaViiteDto> revertToVersio(
            @PathVariable("id") final Long id,
            @PathVariable("versioId") final Integer versioId) {
        TutkinnonOsaViiteDto t = tutkinnonOsaViiteService.revertToVersio(id, versioId);
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/kaikki/{koodiUri}", method = GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<TutkinnonOsaKaikkiDto> getTutkinnonOsaKaikkiDtoByKoodi(
            @PathVariable("koodiUri") final String koodiUri) {
        return perusteenOsaService.getTutkinnonOsaKaikkiDtoByKoodi(koodiUri);
    }
}
