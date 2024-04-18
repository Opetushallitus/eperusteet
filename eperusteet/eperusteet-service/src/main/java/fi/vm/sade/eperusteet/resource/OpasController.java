package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.dto.opas.OpasDto;
import fi.vm.sade.eperusteet.dto.opas.OpasLuontiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.service.OpasService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
import springfox.documentation.annotations.ApiIgnore;

import java.util.Arrays;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/oppaat")
@Api("Oppaat")
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
    @ApiOperation(value = "oppaiden haku")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sivu", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "sivukoko", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "nimi", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "kieli", dataType = "string", paramType = "query", defaultValue = "fi", value = "oppaan nimen kieli"),
            @ApiImplicitParam(name = "muokattu", dataType = "long", paramType = "query", value = "muokattu jälkeen (aikaleima; millisenkunteja alkaen 1970-01-01 00:00:00 UTC)"),
            @ApiImplicitParam(name = "koulutustyyppi", dataType = "string", paramType = "query", allowMultiple = true, value = "koulutustyyppi (koodistokoodi)"),
            @ApiImplicitParam(name = "tuleva", dataType = "boolean", paramType = "query", defaultValue = "true", value = "hae myös tulevat perusteet"),
            @ApiImplicitParam(name = "voimassaolo", dataType = "boolean", paramType = "query", defaultValue = "true", value = "hae myös voimassaolevat perusteet"),
    })
    public Page<PerusteHakuDto> getAllOppaat(@ApiIgnore PerusteQuery pquery) {
        PageRequest p = PageRequest.of(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return service.findBy(p, pquery);
    }

    @ResponseBody
    @RequestMapping(value = "/projektit", method = GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sivukoko", dataType = "long", paramType = "query"),
    })
    public Page<PerusteprojektiKevytDto> getAllOppaatKevyt(PerusteprojektiQueryDto pquery) {
        pquery.setTyyppi(Arrays.asList(PerusteTyyppi.OPAS));
        PageRequest p = PageRequest.of(pquery.getSivu(), Math.min(pquery.getSivukoko(), 1000));
        Page<PerusteprojektiKevytDto> page = service.findProjektiBy(p, pquery);
        return page;
    }
}
