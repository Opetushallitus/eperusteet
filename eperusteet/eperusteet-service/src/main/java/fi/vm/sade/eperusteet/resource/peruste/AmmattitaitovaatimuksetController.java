package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.dto.AmmattitaitovaatimusQueryDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteKontekstiDto;
import fi.vm.sade.eperusteet.service.AmmattitaitovaatimusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(value = "/api/ammattitaitovaatimukset")
@Tag(name = "Ammattitaitovaatimukset")
public class AmmattitaitovaatimuksetController {

    @Autowired
    private AmmattitaitovaatimusService ammattitaitovaatimusService;

    @RequestMapping(value = "/perusteet", method = GET)
    @Operation(summary = "Ammattitaitovaatimuksen sisältävien perusteiden haku")
    @Parameters({
            @Parameter(name = "sivu", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "sivukoko", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "uri", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY)
    })
    public Page<PerusteBaseDto> getPerusteetByAmmattitaitovaatimus(@Parameter(hidden = true) AmmattitaitovaatimusQueryDto pquery) {
        PageRequest p = PageRequest.of(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        Page<PerusteBaseDto> result = ammattitaitovaatimusService.findPerusteet(p, pquery);
        return result;
    }

    @RequestMapping(value = "/tutkinnonosat", method = GET)
    @Operation(summary = "Ammattitaitovaatimuksen sisältävien perusteiden haku")
    @Parameters({
            @Parameter(name = "sivu", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "sivukoko", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "uri", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "kaikki", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY),
    })
    public Page<TutkinnonOsaViiteKontekstiDto> getTutkinnonOsatByAmmattitaitovaatimus(@Parameter(hidden = true) AmmattitaitovaatimusQueryDto pquery) {
        PageRequest p = PageRequest.of(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return ammattitaitovaatimusService.findTutkinnonOsat(p, pquery);
    }

}
