package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenJulkaisuData;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "/perusteet", produces = "application/json;charset=UTF-8")
@Api(value = "Julkaisut")
@Description("Perusteiden julkaisut")
public class JulkaisuController {

    @Autowired
    private JulkaisutService julkaisutService;

    @RequestMapping(method = GET, value = "/{perusteId}/julkaisu")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<JulkaisuBaseDto> getJulkaisut(
            @PathVariable("perusteId") final long id) {
        return julkaisutService.getJulkaisut(id);
    }

    @RequestMapping(method = GET, value = "/julkaisut")
    @ResponseBody
    @ApiOperation(value = "julkaistujen perusteiden haku")
    public ResponseEntity<Page<PerusteenJulkaisuData>> getKoulutustyyppienJulkaisut(
            @RequestParam("koulutustyyppi") final List<String> koulutustyyppi,
            @RequestParam(value = "nimi", defaultValue = "", required = false) final String nimi,
            @RequestParam(value = "kieli", defaultValue = "fi", required = false) final String kieli,
            @RequestParam(value = "tulevat", defaultValue = "true", required = false) final boolean tulevat,
            @RequestParam(value = "voimassa", defaultValue = "true", required = false) final boolean voimassa,
            @RequestParam(value = "siirtyma", defaultValue = "true", required = false) final boolean siirtyma,
            @RequestParam(value = "poistuneet", defaultValue = "false", required = false) final boolean poistuneet,
            @RequestParam(value = "koulutusvienti", defaultValue = "false", required = false) final boolean koulutusvienti,
            @RequestParam(value = "sivu", defaultValue = "0", required = false) final Integer sivu,
            @RequestParam(value = "sivukoko", defaultValue = "10", required = false) final Integer sivukoko) {
        return ResponseEntity.ok(julkaisutService.getJulkisetJulkaisut(koulutustyyppi, nimi, kieli, tulevat, voimassa, siirtyma, poistuneet, koulutusvienti, sivu, sivukoko));
    }

    @RequestMapping(method = POST, value = "/{projektiId}/julkaisu")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public JulkaisuBaseDto teeJulkaisu(
            @PathVariable("projektiId") final long projektiId,
            @RequestBody JulkaisuBaseDto julkaisuBaseDto) {
        return julkaisutService.teeJulkaisu(projektiId, julkaisuBaseDto);
    }

    @RequestMapping(method = POST, value = "/{projektiId}/aktivoi/{revision}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public JulkaisuBaseDto aktivoiJulkaisu(
            @PathVariable("projektiId") final long projektiId,
            @PathVariable("revision") final int revision) {
        return julkaisutService.aktivoiJulkaisu(projektiId, revision);
    }

}
