package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.domain.JulkaisuTila;
import fi.vm.sade.eperusteet.dto.JulkaisuSisaltoTyyppi;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenJulkaisuData;
import fi.vm.sade.eperusteet.dto.util.FieldComparisonFailureDto;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.tika.mime.MimeTypeException;
import org.skyscreamer.jsonassert.FieldComparisonFailure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "/perusteet", produces = "application/json;charset=UTF-8")
@Api(value = "Julkaisut")
@Description("Perusteiden julkaisut")
public class JulkaisuController {

    @Autowired
    private JulkaisutService julkaisutService;

    @RequestMapping(method = GET, value = "/{perusteId}/julkaisut")
    public List<JulkaisuBaseDto> getJulkaisut(
            @PathVariable("perusteId") final long id) {
        return julkaisutService.getJulkaisut(id);
    }

    @RequestMapping(method = GET, value = "/{perusteId}/julkaisut/julkinen")
    public List<JulkaisuBaseDto> getJulkisetJulkaisut(
            @PathVariable("perusteId") final long id) {
        return julkaisutService.getJulkisetJulkaisut(id);
    }

    @RequestMapping(method = GET, value = "/julkaisut")
    @ResponseBody
    @ApiOperation(value = "julkaistujen perusteiden haku")
    public ResponseEntity<Page<PerusteenJulkaisuData>> getKoulutustyyppienJulkaisut(
            @RequestParam(value = "koulutustyyppi", required = false) final List<String> koulutustyyppi,
            @RequestParam(value = "nimi", defaultValue = "", required = false) final String nimi,
            @RequestParam(value = "nimiTaiKoodi", defaultValue = "", required = false) final String nimiTaiKoodi,
            @RequestParam(value = "kieli", defaultValue = "fi", required = false) final String kieli,
            @RequestParam(value = "tulevat", defaultValue = "true", required = false) final boolean tulevat,
            @RequestParam(value = "voimassa", defaultValue = "true", required = false) final boolean voimassa,
            @RequestParam(value = "siirtyma", defaultValue = "true", required = false) final boolean siirtyma,
            @RequestParam(value = "poistuneet", defaultValue = "false", required = false) final boolean poistuneet,
            @RequestParam(value = "koulutusvienti", defaultValue = "false", required = false) final boolean koulutusvienti,
            @RequestParam(value = "tyyppi", defaultValue = "normaali", required = false) final String tyyppi,
            @RequestParam(value = "diaarinumero", defaultValue = "", required = false) final String diaarinumero,
            @RequestParam(value = "koodi", defaultValue = "", required = false) final String koodi,
            @RequestParam(value = "perusteet", defaultValue = "true", required = false) final boolean perusteet,
            @RequestParam(value = "tutkinnonosat", defaultValue = "false", required = false) final boolean tutkinnonosat,
            @RequestParam(value = "sivu", defaultValue = "0", required = false) final Integer sivu,
            @RequestParam(value = "sivukoko", defaultValue = "10", required = false) final Integer sivukoko) {
        return ResponseEntity.ok(julkaisutService.getJulkisetJulkaisut(
                koulutustyyppi, nimi, nimiTaiKoodi, kieli, tyyppi, tulevat, voimassa, siirtyma, poistuneet, koulutusvienti, diaarinumero, koodi,
                JulkaisuSisaltoTyyppi.of(perusteet, tutkinnonosat),
                sivu, sivukoko));
    }

    @RequestMapping(method = POST, value = "/{projektiId}/julkaisu")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void teeJulkaisu(
            @PathVariable("projektiId") final long projektiId,
            @RequestBody JulkaisuBaseDto julkaisuBaseDto) {
        julkaisutService.teeJulkaisu(projektiId, julkaisuBaseDto);
    }

    @RequestMapping(method = POST, value = "/{projektiId}/aktivoi/{revision}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public JulkaisuBaseDto aktivoiJulkaisu(
            @PathVariable("projektiId") final long projektiId,
            @PathVariable("revision") final int revision) throws HttpMediaTypeNotSupportedException, MimeTypeException, IOException {
        return julkaisutService.aktivoiJulkaisu(projektiId, revision);
    }

    @RequestMapping(method = GET, value = "/{perusteId}/viimeisinjulkaisutila")
    public JulkaisuTila viimeisinJulkaisuTila(
            @PathVariable("perusteId") final long perusteId) {
        return julkaisutService.viimeisinJulkaisuTila(perusteId);
    }

    @RequestMapping(method = GET, value = "/{perusteId}/viimeisinjulkaisuaika")
    public Date viimeisinJulkaisuAika(
            @PathVariable("perusteId") final long perusteId) {
        return julkaisutService.viimeisinPerusteenJulkaisuaika(perusteId);
    }

    @RequestMapping(method = GET, value = "/{perusteId}/julkaisu/muutoksia")
    public boolean julkaisemattomiaMuutoksia(
            @PathVariable("perusteId") final long perusteId) {
        return julkaisutService.julkaisemattomiaMuutoksia(perusteId);
    }

    @RequestMapping(method = GET, value = "/{perusteId}/koodita")
    public void kooditaPeruste(
            @PathVariable("perusteId") final long perusteId) {
        julkaisutService.kooditaValiaikaisetKoodit(perusteId);
    }

    @RequestMapping(method = GET, value = "/{perusteId}/nollaajulkaisutila")
    public void nollaaJulkaisuTila(
            @PathVariable("perusteId") final long perusteId) {
        julkaisutService.nollaaJulkaisuTila(perusteId);
    }

    @RequestMapping(method = POST, value = "/{perusteId}/julkaisu/update")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void updateJulkaisu(@PathVariable("perusteId") final long perusteId,
                       @RequestBody JulkaisuBaseDto julkaisuBaseDto) throws HttpMediaTypeNotSupportedException, MimeTypeException, IOException {
        julkaisutService.updateJulkaisu(perusteId, julkaisuBaseDto);
    }
}
