package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.dto.opas.OpasDto;
import fi.vm.sade.eperusteet.dto.opas.OpasLuontiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import fi.vm.sade.eperusteet.service.OpasService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/oppaat")
@Tag(name = "Oppaat")
@InternalApi
public class OpasController {

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private OpasService service;

    private HttpHeaders buildHeadersFor(Long id, UriComponentsBuilder ucb) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucb.path("/perusteprojektit/{id}").buildAndExpand(id).toUri());
        return headers;
    }

    @RequestMapping(method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<OpasDto> addOpas(
            @RequestBody OpasLuontiDto dto,
            UriComponentsBuilder ucb) {
        OpasDto resultDto = service.save(dto);
        return new ResponseEntity<>(resultDto, buildHeadersFor(resultDto.getId(), ucb), HttpStatus.CREATED);
    }

    @RequestMapping(method = GET)
    @ResponseBody
    @Operation(summary = "oppaiden haku")
    @Parameters({
            @Parameter(name = "sivu", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "sivukoko", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
            @Parameter(name = "nimi", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "kieli", schema = @Schema(implementation = String.class, defaultValue = "fi"), array = @ArraySchema(schema = @Schema(type = "string")), in = ParameterIn.QUERY, description = "oppaan nimen kieli"),
            @Parameter(name = "muokattu", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY, description = "muokattu jälkeen (aikaleima; millisenkunteja alkaen 1970-01-01 00:00:00 UTC)"),
            @Parameter(name = "koulutustyyppi", in = ParameterIn.QUERY, array = @ArraySchema(schema = @Schema(type = "string")), description = "koulutustyyppi (koodistokoodi)"),
            @Parameter(name = "tuleva", schema = @Schema(implementation = Boolean.class, defaultValue = "true"), in = ParameterIn.QUERY, description = "hae myös tulevat perusteet"),
            @Parameter(name = "voimassaolo", schema = @Schema(implementation = Boolean.class, defaultValue = "true"), in = ParameterIn.QUERY, description = "hae myös voimassaolevat perusteet"),
    })
    public Page<PerusteHakuDto> getAllOppaat(@Parameter(hidden = true) PerusteQuery pquery) {
        PageRequest p = PageRequest.of(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return service.findBy(p, pquery);
    }

    @ResponseBody
    @RequestMapping(value = "/projektit", method = GET)
    @Parameters({
            @Parameter(name = "sivukoko", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY),
    })
    public Page<PerusteprojektiKevytDto> getAllOppaatKevyt(@Parameter(hidden = true) PerusteprojektiQueryDto pquery) {
        pquery.setTyyppi(Arrays.asList(PerusteTyyppi.OPAS));
        PageRequest p = PageRequest.of(pquery.getSivu(), Math.min(pquery.getSivukoko(), 1000));
        Page<PerusteprojektiKevytDto> page = service.findProjektiBy(p, pquery);
        return page;
    }
}
