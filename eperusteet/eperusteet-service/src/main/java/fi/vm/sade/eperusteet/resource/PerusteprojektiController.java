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

import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.OmistajaDto;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.validointi.ValidationDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaTyoryhmaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.*;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.audit.EperusteetAudit;
import static fi.vm.sade.eperusteet.service.audit.EperusteetMessageFields.PERUSTEPROJEKTI;
import static fi.vm.sade.eperusteet.service.audit.EperusteetMessageFields.TYORYHMA;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.LISAYS;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.LUONTI;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.MUOKKAUS;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.POISTO;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.TILAMUUTOS;
import fi.vm.sade.eperusteet.service.audit.LogMessage;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author harrik
 */
@Slf4j
@Controller
@RequestMapping("/perusteprojektit")
@InternalApi
public class PerusteprojektiController {

    @Autowired
    private EperusteetAudit audit;

    @Autowired
    private PerusteprojektiService service;

    @Autowired
    private PermissionManager permission;

    @RequestMapping(value = "/info", method = GET)
    @ResponseBody
    public ResponseEntity<List<PerusteprojektiInfoDto>> getAll() {
        return new ResponseEntity<>(service.getBasicInfo(), HttpStatus.OK);
    }

    @RequestMapping(value = "/perusteHaku", method = GET)
    @ResponseBody
    public Page<PerusteprojektiKevytDto> getAllKevyt(PerusteprojektiQueryDto pquery) {
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 20));
        Page<PerusteprojektiKevytDto> page = service.findBy(p, pquery);
        return page;
    }

    @RequestMapping(value = "/omat", method = GET)
    @ResponseBody
    public ResponseEntity<List<PerusteprojektiListausDto>> getOmat() {
        return new ResponseEntity<>(service.getOmatProjektit(), HttpStatus.OK);
    }

    @RequestMapping(value = "/virheelliset", method = GET)
    @ResponseBody
    @Description("Lista julkaistujen perusteprojektien virheistä. Tätä käytetään helpottamaan perusteiden korjausta validointisääntöjen muuttuessa.")
    public ResponseEntity<Page<ValidationDto>> getVirheelliset(
            @RequestParam(defaultValue = "0") Integer sivu,
            @RequestParam(defaultValue = "10") Integer sivukoko
    ) {
        PageRequest p = new PageRequest(sivu, Math.min(sivukoko, 20));
        return new ResponseEntity<>(service.getVirheelliset(p), HttpStatus.OK);
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

    @RequestMapping(value = "/{id}/validoi", method = GET)
    @ResponseBody
    public ResponseEntity<TilaUpdateStatus> getPerusteprojektiValidointi(@PathVariable("id") final long id) {
        return new ResponseEntity<>(service.validoiProjekti(id, ProjektiTila.JULKAISTU), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PerusteprojektiDto update(
            @PathVariable("id") final long id,
            @RequestBody PerusteprojektiDto perusteprojektiDto) {
        return audit.withAudit(LogMessage.builder(null, PERUSTEPROJEKTI, LISAYS).add("perusteprojektiId", id),
                (Void) -> service.update(id, perusteprojektiDto));
    }

    @RequestMapping(value = "/{id}/tila/{tila}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public TilaUpdateStatus updateTila(
            @PathVariable("id") final long id,
            @PathVariable("tila") final String tila,
            @RequestBody TiedoteDto tiedoteDto
    ) {
        return audit.withAudit(LogMessage.builder(null, PERUSTEPROJEKTI, TILAMUUTOS).add("perusteprojektiId", id),
                Void -> service.updateTila(id, ProjektiTila.of(tila), tiedoteDto));
    }

    @RequestMapping(method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<PerusteprojektiDto> add(
            @RequestBody PerusteprojektiLuontiDto perusteprojektiLuontiDto,
            UriComponentsBuilder ucb
    ) {
        return audit.withAudit(LogMessage.builder(null, PERUSTEPROJEKTI, LUONTI), (Void) -> {
            PerusteprojektiDto perusteprojektiDto = service.save(perusteprojektiLuontiDto);
            return new ResponseEntity<>(perusteprojektiDto, buildHeadersFor(perusteprojektiDto.getId(), ucb), HttpStatus.CREATED);
        });
    }

    private HttpHeaders buildHeadersFor(Long id, UriComponentsBuilder ucb) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucb.path("/perusteprojektit/{id}").buildAndExpand(id).toUri());
        return headers;
    }

    @RequestMapping(value = "/diaarinumero/uniikki/{diaarinumero}", method = GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<DiaarinumeroHakuDto> get(@PathVariable("diaarinumero") final Diaarinumero diaarinumero) {
        return new ResponseEntity<>(service.onkoDiaarinumeroKaytossa(diaarinumero), HttpStatus.OK);
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
            @PathVariable("id") final Long id) {
        List<TyoryhmaHenkiloDto> tyoryhmat = service.getTyoryhmaHenkilot(id, nimi);
        return new ResponseEntity<>(tyoryhmat, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/tyoryhma", method = POST)
    @ResponseBody
    public ResponseEntity<List<TyoryhmaHenkiloDto>> postMultipleTyoryhmaHenkilot(
            @PathVariable("id") final Long id,
            @RequestBody List<TyoryhmaHenkiloDto> tyoryhma) {
        return audit.withAudit(LogMessage.builder(null, TYORYHMA, LUONTI).add("perusteprojektiId", id), (Void) -> {
            List<TyoryhmaHenkiloDto> res = tyoryhma.stream()
                    .map(thd -> service.saveTyoryhma(id, thd))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(res, HttpStatus.OK);
        });
    }

    @RequestMapping(value = "/{id}/tyoryhma/{nimi}", method = POST)
    @ResponseBody
    public ResponseEntity<List<TyoryhmaHenkiloDto>> postMultipleTyoryhmaHenkilotToTyoryhma(
            @PathVariable("id") final Long id,
            @PathVariable("nimi") final String nimi,
            @RequestBody List<String> tyoryhma) {
        return audit.withAudit(LogMessage.builder(null, TYORYHMA, MUOKKAUS).add("perusteprojektiId", id), (Void) -> {
            List<TyoryhmaHenkiloDto> res = service.saveTyoryhma(id, nimi, tyoryhma);
            return new ResponseEntity<>(res, HttpStatus.OK);
        });
    }

    @RequestMapping(value = "/{id}/tyoryhma/{nimi}", method = DELETE)
    public ResponseEntity<TyoryhmaHenkiloDto> removeTyoryhmat(
            @PathVariable("id") final Long id,
            @PathVariable("nimi") final String nimi) {
        return audit.withAudit(LogMessage.builder(null, PERUSTEPROJEKTI, POISTO).add("perusteprojektiId", id), (Void) -> {
            service.removeTyoryhma(id, nimi);
            return new ResponseEntity<>(HttpStatus.OK);
        });
    }

    @RequestMapping(value = "/{id}/perusteenosat/{pid}/tyoryhmat", method = POST)
    @ResponseBody
    public ResponseEntity<List<String>> postPerusteenOsaTyoryhma(
            @PathVariable("id") final Long id,
            @PathVariable("pid") final Long pid,
            @RequestBody List<String> tyoryhmat) {
        return audit.withAudit(LogMessage.builder(null, TYORYHMA, MUOKKAUS).add("perusteprojektiId", id), (Void) -> {
            return new ResponseEntity<>(service.setPerusteenOsaViiteTyoryhmat(id, pid, tyoryhmat), HttpStatus.OK);
        });
    }

    @RequestMapping(value = "/{id}/perusteenosat/{pid}/tyoryhmat", method = GET)
    public ResponseEntity<List<String>> getPerusteenOsaTyoryhma(
            @PathVariable("id") final Long id,
            @PathVariable("pid") final Long pid) {
        return new ResponseEntity<>(service.getPerusteenOsaViiteTyoryhmat(id, pid), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/perusteenosientyoryhmat", method = GET)
    public ResponseEntity<List<PerusteenOsaTyoryhmaDto>> getPerusteenOsaTyoryhma(
            @PathVariable("id") final Long id) {
        List<PerusteenOsaTyoryhmaDto> sisallonTyoryhmat = service.getSisallonTyoryhmat(id);
        return new ResponseEntity<>(sisallonTyoryhmat, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/oikeudet", method = GET)
    public ResponseEntity<Map<PermissionManager.Target, Set<PermissionManager.Permission>>> getOikeudet(
            @PathVariable("id") final Long id) {
        return new ResponseEntity<>(permission.getProjectPermissions(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/oikeudet/{perusteenOsaId}", method = GET)
    public ResponseEntity<OmistajaDto> omistaaOsan(
            @PathVariable("id") final Long id,
            @PathVariable("perusteenOsaId") final Long perusteenOsaId) {
        return new ResponseEntity<>(service.isOwner(id, perusteenOsaId), HttpStatus.OK);
    }
}
