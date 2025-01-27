package fi.vm.sade.eperusteet.resource.julkinen;

import fi.vm.sade.eperusteet.dto.peruste.TutkinnonOsaQueryDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteKontekstiDto;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/tutkinnonosat")
@Tag(name = "Tutkinnonosat", description = "Tutkinnon osat")
public class TutkinnonOsaJulkinenController {
    @Autowired
    private PerusteenOsaService service;

    @RequestMapping(method = GET)
    @ResponseBody
    @Operation(summary = "hae tutkinnon osia")
    @Parameters({
            @Parameter(name = "sivu", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "sivukoko", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(
                    name = "koodiUri",
                    schema = @Schema(implementation = String.class),
                    required = true,
                    in = ParameterIn.QUERY,
                    description = "tutkinnonosakoodi",
                    example = "tutkinnonosat_123456"),
    })
    public Page<TutkinnonOsaDto> getAllTutkinnonOsatByKoodiUri(@Parameter(hidden = true) TutkinnonOsaQueryDto pquery) {
        return service.findTutkinnonOsatBy(pquery);
    }

    @RequestMapping(method = GET, value = "/all")
    @ResponseBody
    @Operation(summary = "hae tutkinnon osia")
    @Parameters({
            @Parameter(name = "sivu", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "sivukoko", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "nimi", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "perusteId", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "vanhentuneet", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY),
            @Parameter(name = "kieli", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
    })
    public Page<TutkinnonOsaViiteKontekstiDto> getAllTutkinnonOsatBy(@Parameter(hidden = true) TutkinnonOsaQueryDto pquery) {
        return service.findAllTutkinnonOsatBy(pquery);
    }

    @RequestMapping(method = GET, value = "/{tutkinnonOsaId}/viitteet")
    @ResponseBody
    @Operation(summary = "hae tutkinnon osiin liittyvät viitteet")
    public List<TutkinnonOsaViiteKontekstiDto> getAllTutkinnonOsaViitteet(
            @PathVariable("tutkinnonOsaId") Long tutkinnonOsaId) {
        return service.findTutkinnonOsaViitteetByTutkinnonOsa(tutkinnonOsaId);
    }


}
