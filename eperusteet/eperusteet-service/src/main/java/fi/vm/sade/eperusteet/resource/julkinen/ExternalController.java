package fi.vm.sade.eperusteet.resource.julkinen;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenJulkaisuData;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import fi.vm.sade.eperusteet.service.PerusteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
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
@Api(value = "Julkinen")
@Description("Perusteiden julkinen rajapinta")
public class ExternalController {

    @Autowired
    private JulkaisutService julkaisutService;

    @Autowired
    private PerusteService perusteService;

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
        return ResponseEntity.ok(julkaisutService.getJulkisetJulkaisut(koulutustyyppi, nimi, kieli, tyyppi, tulevat, voimassa, siirtyma, poistuneet, koulutusvienti, diaarinumero, koodi, sivu, sivukoko));
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

    @RequestMapping(value = "/peruste/{perusteId:\\d+}/query", method = GET)
    @ResponseBody
    @ApiOperation(value = "Perusteen tietojen haku JsonPathilla")
    public ResponseEntity<Object> getPerusteWithQuery(
            @PathVariable("perusteId") final long id,
            @RequestParam(value = "query") final String query) {
        Object result = perusteService.getJulkaistuSisaltoObjectNode(id, query);
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/peruste/{perusteId:\\d+}/**", method = GET)
    @ResponseBody
    @ApiOperation(value = "Perusteen tietojen haku tarkalla sisältörakenteella")
    public ResponseEntity<Object> getPerusteDynamicQuery(HttpServletRequest req, @PathVariable("perusteId") final long id) {
        return getJulkaistuSisaltoObjectNodeWithQuery(req, id);
    }

    @RequestMapping(value = "/peruste/yto/**", method = GET)
    @ResponseBody
    @ApiOperation(value = "Yhteisien tutkinnon osa -perusteen tietojen haku tarkalla sisältörakenteella")
    public ResponseEntity<Object> getYtoPerusteDynamicQuery(HttpServletRequest req) {
        Page<PerusteenJulkaisuData> amosaaPeruste = julkaisutService.getJulkisetJulkaisut(
                Collections.emptyList(),
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
                0,
                1);

        if (amosaaPeruste.getTotalElements() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return getJulkaistuSisaltoObjectNodeWithQuery(req, amosaaPeruste.getContent().get(0).getId());
    }


    private ResponseEntity<Object> getJulkaistuSisaltoObjectNodeWithQuery(HttpServletRequest req, long id) {
        String[] queries = req.getPathInfo().split("/");
        Object result = perusteService.getJulkaistuSisaltoObjectNode(id, Arrays.stream(queries).skip(4).collect(Collectors.toList()));
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }


}
