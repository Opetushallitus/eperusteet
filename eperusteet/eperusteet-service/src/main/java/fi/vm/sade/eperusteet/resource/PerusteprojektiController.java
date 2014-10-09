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

import com.mangofactory.swagger.annotations.ApiIgnore;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiInfoDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.TyoryhmaHenkiloDto;
import fi.vm.sade.eperusteet.dto.util.BooleanDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author harrik
 */
@Controller
@RequestMapping("/perusteprojektit")
@ApiIgnore
public class PerusteprojektiController {

    @Autowired
    private PerusteprojektiService service;

    @Autowired
    private PermissionManager permission;

    @RequestMapping(value = "/info", method = GET)
    @ResponseBody
    public ResponseEntity<List<PerusteprojektiInfoDto>> getAll() {
        return new ResponseEntity<>(service.getBasicInfo(), HttpStatus.OK);
    }

    @RequestMapping(value = "/omat", method = GET)
    @ResponseBody
    public ResponseEntity<List<PerusteprojektiInfoDto>> getOmat() {
        return new ResponseEntity<>(service.getOmatProjektit(), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<PerusteprojektiDto> get(@PathVariable("id") final long id) {
        PerusteprojektiDto t = service.get(id);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/jasenet", method = GET)
    @ResponseBody
    public ResponseEntity<List<KayttajanTietoDto>> getJasenet(@PathVariable("id") final long id) {
        return new ResponseEntity<>(service.getJasenet(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/jasenet/tiedot", method = GET)
    @ResponseBody
    public ResponseEntity<List<CombinedDto<KayttajanTietoDto, KayttajanProjektitiedotDto>>> getJasenetTiedot(@PathVariable("id") final long id) {
        return new ResponseEntity<>(service.getJasenetTiedot(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/tilat", method = GET)
    @ResponseBody
    public ResponseEntity<Set<ProjektiTila>> getTilat(@PathVariable("id") final long id) {
        return new ResponseEntity<>(service.getTilat(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PerusteprojektiDto update(@PathVariable("id") final long id, @RequestBody PerusteprojektiDto perusteprojektiDto) {
        perusteprojektiDto = service.update(id, perusteprojektiDto);
        return perusteprojektiDto;
    }

    @RequestMapping(value = "/{id}/tila/{tila}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public TilaUpdateStatus updateTila(@PathVariable("id") final long id, @PathVariable("tila") final String tila) {
        return service.updateTila(id, ProjektiTila.of(tila));
    }

    @RequestMapping(method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<PerusteprojektiDto> add(@RequestBody PerusteprojektiLuontiDto perusteprojektiLuontiDto, UriComponentsBuilder ucb) {
        PerusteprojektiDto perusteprojektiDto = service.save(perusteprojektiLuontiDto);
        return new ResponseEntity<>(perusteprojektiDto, buildHeadersFor(perusteprojektiDto.getId(), ucb), HttpStatus.CREATED);
    }

    private HttpHeaders buildHeadersFor(Long id, UriComponentsBuilder ucb) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucb.path("/perusteprojektit/{id}").buildAndExpand(id).toUri());
        return headers;
    }

    @RequestMapping(value = "/diaarinumero/uniikki/{diaarinumero}", method = GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<BooleanDto> get(@PathVariable("diaarinumero") final String diaarinumero) {
        try {
            service.onkoDiaarinumeroKaytossa(diaarinumero);
        } catch (BusinessRuleViolationException ex) {
            return new ResponseEntity<>(new BooleanDto(false), HttpStatus.OK);
        }

        return new ResponseEntity<>(new BooleanDto(true), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/tyoryhma", method = GET)
    @ResponseBody
    public ResponseEntity<List<TyoryhmaHenkiloDto>> getTyoryhmat(@PathVariable("id") final Long id) {
        List<TyoryhmaHenkiloDto> tyoryhmat = service.getTyoryhmaHenkilot(id);
        return new ResponseEntity<>(tyoryhmat, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/tyoryhma/{nimi}", method = GET)
    @ResponseBody
    public ResponseEntity<List<TyoryhmaHenkiloDto>> getTyoryhmaByNimi(
            @PathVariable("nimi") final String nimi,
            @PathVariable("id") final Long id
    ) {
        List<TyoryhmaHenkiloDto> tyoryhmat = service.getTyoryhmaHenkilot(id, nimi);
        return new ResponseEntity<>(tyoryhmat, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/tyoryhma", method = POST)
    @ResponseBody
    public ResponseEntity<List<TyoryhmaHenkiloDto>> postMultipleTyoryhmaHenkilot(
            @PathVariable("id") final Long id,
            @RequestBody List<TyoryhmaHenkiloDto> tyoryhma
    ) {
        List<TyoryhmaHenkiloDto> res = new ArrayList<>();
        for (TyoryhmaHenkiloDto thd : tyoryhma) {
            res.add(service.saveTyoryhma(id, thd));
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/tyoryhma/{nimi}", method = POST)
    @ResponseBody
    public ResponseEntity<List<TyoryhmaHenkiloDto>> postMultipleTyoryhmaHenkilotToTyoryhma(
            @PathVariable("id") final Long id,
            @PathVariable("nimi") final String nimi,
            @RequestBody List<String> tyoryhma
    ) {
        List<TyoryhmaHenkiloDto> res = service.saveTyoryhma(id, nimi, tyoryhma);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/tyoryhma/{nimi}", method = DELETE)
    public ResponseEntity<TyoryhmaHenkiloDto> removeTyoryhmat(
            @PathVariable("id") final Long id,
            @PathVariable("nimi") final String nimi
    ) {
        service.removeTyoryhma(id, nimi);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/perusteenosat/{pid}/tyoryhmat", method = POST)
    @ResponseBody
    public ResponseEntity<List<String>> postPerusteenOsaTyoryhma(
            @PathVariable("id") final Long id,
            @PathVariable("pid") final Long pid,
            @RequestBody List<String> tyoryhmat
    ) {
        return new ResponseEntity<>(service.setPerusteenOsaViiteTyoryhmat(id, pid, tyoryhmat), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/perusteenosat/{pid}/tyoryhmat", method = GET)
    public ResponseEntity<List<String>> getPerusteenOsaTyoryhma(
            @PathVariable("id") final Long id,
            @PathVariable("pid") final Long pid
    ) {
        return new ResponseEntity<>(service.getPerusteenOsaViiteTyoryhmat(id, pid), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/oikeudet", method = GET)
    public ResponseEntity<Map<PermissionManager.Target,Set<PermissionManager.Permission>>> getOikeudet(
            @PathVariable("id") final Long id
    ) {
        return new ResponseEntity<>(permission.getProjectPermissions(id), HttpStatus.OK);
    }
}
