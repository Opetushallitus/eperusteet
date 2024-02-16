package fi.vm.sade.eperusteet.resource.julkinen;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.JulkaisuSisaltoTyyppi;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiBaseDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiExternalDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenJulkaisuData;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import fi.vm.sade.eperusteet.service.OsaamismerkkiService;
import fi.vm.sade.eperusteet.service.PerusteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(value = "/external", produces = "application/json;charset=UTF-8")
@Api(value = "External")
@Description("Perusteiden julkinen rajapinta")
public class ExternalController {

    @Autowired
    private JulkaisutService julkaisutService;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private OsaamismerkkiService osaamismerkkiService;

    @RequestMapping(value = "/peruste/{perusteId:\\d+}", method = GET)
    @ResponseBody
    @ApiOperation(value = "Perusteen tietojen haku")
    public ResponseEntity<PerusteKaikkiDto> getPeruste(
            @PathVariable("perusteId") final long id) {
        PerusteKaikkiDto peruste = perusteService.getJulkaistuSisalto(id);
        if (peruste == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(peruste);
    }

    @RequestMapping(method = GET, value = "/perusteet")
    @ResponseBody
    @ApiOperation(value = "Perusteiden haku")
    public ResponseEntity<Page<PerusteenJulkaisuData>> getPerusteet(
            @RequestParam(value = "koulutustyyppi", required = false) final List<String> koulutustyyppi,
            @RequestParam(value = "nimi", defaultValue = "", required = false) final String nimi,
            @RequestParam(value = "kieli", defaultValue = "fi", required = false) final String kieli,
            @RequestParam(value = "tulevat", defaultValue = "true", required = false) final boolean tulevat,
            @RequestParam(value = "voimassa", defaultValue = "true", required = false) final boolean voimassa,
            @RequestParam(value = "siirtyma", defaultValue = "true", required = false) final boolean siirtyma,
            @RequestParam(value = "poistuneet", defaultValue = "false", required = false) final boolean poistuneet,
            @RequestParam(value = "koulutusvienti", defaultValue = "false", required = false) final boolean koulutusvienti,
            @RequestParam(value = "tyyppi", defaultValue = "normaali", required = false) final String tyyppi,
            @RequestParam(value = "diaarinumero", defaultValue = "", required = false) final String diaarinumero,
            @RequestParam(value = "koodi", defaultValue = "", required = false) final String koodi,
            @RequestParam(value = "sivu", defaultValue = "0", required = false) final Integer sivu,
            @RequestParam(value = "sivukoko", defaultValue = "10", required = false) final Integer sivukoko) {
        return ResponseEntity.ok(julkaisutService.getJulkisetJulkaisut(
                koulutustyyppi, nimi, "", kieli, tyyppi, tulevat, voimassa, siirtyma, poistuneet, koulutusvienti, diaarinumero, koodi, JulkaisuSisaltoTyyppi.PERUSTE,
                sivu, sivukoko));
    }

    @RequestMapping(value = "/peruste/{perusteId:\\d+}/perusteenosa/{perusteenOsaId}", method = GET)
    @ResponseBody
    @ApiOperation(value = "Perusteen osan haku")
    public ResponseEntity<PerusteenOsaDto> getJulkaistuPerusteenOsa(
            @PathVariable("perusteId") final long perusteId,
            @PathVariable("perusteenOsaId") final long perusteenOsaId) {
        PerusteenOsaDto perusteenOsa = perusteService.getJulkaistuPerusteenOsa(perusteId, perusteenOsaId);
        if (perusteenOsa == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(perusteenOsa);
    }

    @RequestMapping(value = "/peruste/{perusteId:\\d+}/osaamisalakuvaukset", method = GET)
    @ResponseBody
    @ApiOperation(value = "Perusteen osaamisalakuvauksien haku")
    public ResponseEntity<Map<Suoritustapakoodi, Map<String, List<TekstiKappaleDto>>>> getJulkaistutOsaamisalaKuvaukset(
            @PathVariable("perusteId") final long perusteId) {
        return ResponseEntity.ok(perusteService.getJulkaistutOsaamisalaKuvaukset(perusteId));
    }

    @RequestMapping(value = "/peruste/{perusteId:\\d+}/**", method = GET)
    @ResponseBody
    @ApiOperation(
            value = "Perusteen tietojen haku tarkalla sisältörakenteella",
            notes = "Url parametreiksi voi antaa peruste id:n lisäksi erilaisia perusteen rakenteen osia ja id-kenttien arvoja. Esim. /peruste/8505691/tutkinnonOsat/7283253/koodi/nimi/fi antaa perusteen (id: 8505691) tutkinnon osien tietueen (id: 7283253) koodi-tiedon nimen suomenkielisenä.",
            response= PerusteKaikkiDto.class
    )
    public ResponseEntity<Object> getPerusteDynamicQuery(HttpServletRequest req, @PathVariable("perusteId") final long id) {
        return getJulkaistuSisaltoObjectNodeWithQuery(id, requestToQueries(req, 4));
    }

    @RequestMapping(value = "/peruste/yto/**", method = GET)
    @ResponseBody
    @ApiOperation(
            value = "Yhteisien tutkinnon osien -perusteen(YTO) tietojen haku tarkalla sisältörakenteella. Kts 'Perusteen tietojen haku tarkalla sisältörakenteella'",
            response= PerusteKaikkiDto.class
    )
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
                false,
                "",
                "",
                JulkaisuSisaltoTyyppi.PERUSTE,
                0,
                1);

        if (amosaaPeruste.getTotalElements() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return getJulkaistuSisaltoObjectNodeWithQuery( amosaaPeruste.getContent().get(0).getId(), requestToQueries(req, 4));
    }

    @RequestMapping(value = "/peruste/koulutuskoodi/{koodi:\\d+}/**", method = GET)
    @ResponseBody
    @ApiOperation(
            value = "Perusteen haku koulutuskoodilla ja sisältörakenteella. Kts 'Perusteen tietojen haku tarkalla sisältörakenteella'.",
            notes = "Statuskoodi 409 jos löytyy useampi peruste. Statuskoodi 204, jos haettu rakenne on tyhjä.",
            response= PerusteKaikkiDto.class
    )
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

        return getJulkaistuSisaltoObjectNodeWithQuery(peruste.getContent().get(0).getId(), requestToQueries(req, 5));
    }

    @RequestMapping(value = "/osaamismerkit", method = GET)
    @ResponseBody
    @ApiOperation(value = "Hae kaikki julkaistut osaamismerkit")
    public ResponseEntity<List<OsaamismerkkiExternalDto>> getOsaamismerkit() {
        return ResponseEntity.ok(osaamismerkkiService.getOsaamismerkit());
    }

    @RequestMapping(value = "/osaamismerkki/koodi/{uri}", method = GET)
    @ResponseBody
    @ApiOperation(value = "Hae julkaistu osaamismerkki koodiurilla")
    public ResponseEntity<OsaamismerkkiDto> getOsaamismerkkiByUri(@PathVariable("uri") final String uri) {
        return ResponseEntity.ok(osaamismerkkiService.getOsaamismerkkiByUri(uri));
    }

    private List<String> requestToQueries(HttpServletRequest req, int skipCount) {
        String[] queries = req.getPathInfo().split("/");
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
