/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.opas.OpasDto;
import fi.vm.sade.eperusteet.dto.opas.OpasLuontiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
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

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 *
 * @author nkala
 */
@RestController
@RequestMapping("/oppaat")
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
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return service.findBy(p, pquery);
    }

    @ResponseBody
    @RequestMapping(value = "/projektit", method = GET)
    public Page<PerusteprojektiKevytDto> getAllOppaatKevyt(PerusteprojektiQueryDto pquery) {
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 20));
        Page<PerusteprojektiKevytDto> page = service.findProjektiBy(p, pquery);
        return page;
    }
}
