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
package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.peruste.*;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.resource.util.CacheableResponse;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.audit.EperusteetAudit;
import static fi.vm.sade.eperusteet.service.audit.EperusteetMessageFields.PERUSTE;
import static fi.vm.sade.eperusteet.service.audit.EperusteetMessageFields.TUTKINTONIMIKEKOODI;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.MUOKKAUS;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.POISTO;
import fi.vm.sade.eperusteet.service.audit.LogMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@RequestMapping(value = "/perusteet", produces = "application/json;charset=UTF-8")
@Api(value = "Perusteet", description = "Perusteiden hallintaan liittyvät operaatiot")
public class PerusteController {

    @Autowired
    private EperusteetAudit audit;

    @Autowired
    private KoodistoClient koodistoService;

    @Autowired
    private PerusteService service;

    @RequestMapping(value = "/info", method = GET)
    @ResponseBody
    @InternalApi
    public Page<PerusteInfoDto> getAllInfo(PerusteQuery pquery) {
        // Vain valmiita perusteita voi hakea tämän rajapinnan avulla
        pquery.setTila(PerusteTila.VALMIS.toString());
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return service.findByInfo(p, pquery);
    }

    @RequestMapping(value = "/valittavatkielet", method = GET)
    @ResponseBody
    @InternalApi
    public ResponseEntity<List<String>> getValittavatKielet() {
        return new ResponseEntity<>(Kieli.vaihtoehdot(), HttpStatus.OK);
    }

    @RequestMapping(value = "/kooste", method = GET)
    @ResponseBody
    @ApiIgnore
    public ResponseEntity<List<PerusteExcelDto>> getKooste() {
        return new ResponseEntity<>(service.getKooste(), HttpStatus.OK);
    }

    @RequestMapping(value = "/uusimmat", method = GET)
    @ResponseBody
    public ResponseEntity<List<PerusteDto>> getUusimmat() {
        return new ResponseEntity<>(service.getUusimmat(), HttpStatus.OK);
    }

    @RequestMapping(value = "/perusopetus", method = GET)
    @ResponseBody
    @ApiIgnore
    public ResponseEntity<List<PerusteInfoDto>> getAllPerusopetus() {
        List<PerusteInfoDto> poi = service.getAllPerusopetusInfo();
        return new ResponseEntity<>(poi, HttpStatus.OK);
    }

    @RequestMapping(method = GET)
    @ResponseBody
    @ApiOperation(value = "perusteiden sisäinen haku")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sivu", dataType = "integer", paramType = "query"),
            @ApiImplicitParam(name = "sivukoko", dataType = "integer", paramType = "query"),
            @ApiImplicitParam(name = "tuleva", dataType = "boolean", paramType = "query", defaultValue = "true", value = "hae myös tulevatperusteet"),
            @ApiImplicitParam(name = "siirtyma", dataType = "boolean", paramType = "query", defaultValue = "true", value = "hae myös siirtymäajalla olevat perusteet"),
            @ApiImplicitParam(name = "voimassaolo", dataType = "boolean", paramType = "query", defaultValue = "true", value = "hae myös voimassaolevat perusteet"),
            @ApiImplicitParam(name = "poistunut", dataType = "boolean", paramType = "query", defaultValue = "true", value = "hae myös poistuneet perusteet"),
            @ApiImplicitParam(name = "nimi", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "koulutusala", dataType = "string", paramType = "query", allowMultiple = true),
            @ApiImplicitParam(name = "koulutustyyppi", dataType = "string", paramType = "query", allowMultiple = true, value = "koulutustyyppi (koodistokoodi)"),
            @ApiImplicitParam(name = "kieli", dataType = "string", paramType = "query", defaultValue = "fi", value = "perusteen nimen kieli"),
            @ApiImplicitParam(name = "opintoala", dataType = "string", paramType = "query", allowMultiple = true, value = "opintoalakoodi"),
            @ApiImplicitParam(name = "suoritustapa", dataType = "string", paramType = "query", value = "AM-perusteet; naytto tai ops"),
            @ApiImplicitParam(name = "koulutuskoodi", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "diaarinumero", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "muokattu", dataType = "integer", paramType = "query", value = "muokattu jälkeen (aikaleima; millisenkunteja alkaen 1970-01-01 00:00:00 UTC)"),
            @ApiImplicitParam(name = "tutkintonimikkeet", dataType = "boolean", paramType = "query", value = "hae myös tutkintonimikkeistä"),
            @ApiImplicitParam(name = "tutkinnonosat", dataType = "boolean", paramType = "query", value = "hae myös tutkinnon osista"),
            @ApiImplicitParam(name = "osaamisalat", dataType = "boolean", paramType = "query", value = "hae myös osaamisaloista"),
            @ApiImplicitParam(name = "koulutusvienti", dataType = "boolean", paramType = "query", value = "Haku ainoastaan koulutusviennistä")
    })
    public Page<PerusteHakuDto> getAll(@ApiIgnore PerusteQuery pquery) {
        // Vain valmiita perusteita voi hakea tämän rajapinnan avulla
        pquery.setTila(PerusteTila.VALMIS.toString());
        // Oletuksena älä palauta pohjia
        if (pquery.getPerusteTyyppi() == null) {
            pquery.setPerusteTyyppi(PerusteTyyppi.NORMAALI.toString());
        }
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return service.findBy(p, pquery);
    }

    @RequestMapping(value = "/internal", method = GET)
    @ResponseBody
    @InternalApi
    @ApiOperation(value = "perusteiden sisäinen haku")
    public Page<PerusteHakuDto> getAllInternal(@ApiIgnore PerusteQuery pquery) {
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return service.findByInternal(p, pquery);
    }

    @RequestMapping(value = "/{perusteId}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @InternalApi
    public PerusteDto update(@PathVariable("perusteId") final long id, @RequestBody PerusteDto perusteDto) {
        return audit.withAudit(LogMessage.builder(id, PERUSTE, MUOKKAUS), (Void) -> service.updateFull(id, perusteDto));
    }

    @RequestMapping(value = "/{perusteId}/kvliite", method = GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public KVLiiteJulkinenDto getKvLiite(
            @PathVariable("perusteId") final long id) {
        return service.getJulkinenKVLiite(id);
    }

    @RequestMapping(value = "/{perusteId}/tutkintonimikekoodit/{tutkintonimikeKoodiId}", method = DELETE)
    @ResponseBody
    @InternalApi
    public ResponseEntity<TutkintonimikeKoodiDto> addTutkintonimikekoodi(
            @PathVariable("perusteId") final long id,
            @PathVariable("tutkintonimikeKoodiId") final Long tnkId) {
        return audit.withAudit(LogMessage.builder(id, TUTKINTONIMIKEKOODI, POISTO), (Void) -> {
            service.removeTutkintonimikeKoodi(id, tnkId);
            return new ResponseEntity<>(HttpStatus.OK);
        });
    }

    @RequestMapping(value = "/{perusteId}/tutkintonimikekoodit", method = {POST, PUT})
    @ResponseBody
    @InternalApi
    public ResponseEntity<TutkintonimikeKoodiDto> addTutkintonimikekoodi(
            @PathVariable("perusteId") final long id,
            @RequestBody final TutkintonimikeKoodiDto tnk) {
        return audit.withAudit(LogMessage.builder(id, TUTKINTONIMIKEKOODI, MUOKKAUS), (Void) -> {
            TutkintonimikeKoodiDto tutkintonimikeKoodi = service.addTutkintonimikeKoodi(id, tnk);
            return new ResponseEntity<>(tutkintonimikeKoodi, HttpStatus.OK);
        });
    }

    @RequestMapping(value = "/{perusteId}/tutkintonimikekoodit", method = GET)
    @ResponseBody
    @InternalApi
    public ResponseEntity<List<CombinedDto<TutkintonimikeKoodiDto, HashMap<String, KoodistoKoodiDto>>>> getTutkintonimikekoodit(@PathVariable("perusteId") final long id) {
        List<TutkintonimikeKoodiDto> tutkintonimikeKoodit = service.getTutkintonimikeKoodit(id);
        List<CombinedDto<TutkintonimikeKoodiDto, HashMap<String, KoodistoKoodiDto>>> response = new ArrayList<>();

        for (TutkintonimikeKoodiDto tkd : tutkintonimikeKoodit) {
            HashMap<String, KoodistoKoodiDto> nimet = new HashMap<>();
            if (tkd.getOsaamisalaUri() != null) {
                nimet.put(tkd.getOsaamisalaArvo(), koodistoService.get("osaamisala", tkd.getOsaamisalaUri()));
            }
            nimet.put(tkd.getTutkintonimikeArvo(), koodistoService.get("tutkintonimikkeet", tkd.getTutkintonimikeUri()));
            if (tkd.getTutkinnonOsaUri() != null) {
                nimet.put(tkd.getTutkinnonOsaArvo(), koodistoService.get("tutkinnonosat", tkd.getTutkinnonOsaUri()));
            }
            response.add(new CombinedDto<>(tkd, nimet));
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/{perusteId}/meta", method = GET)
    @ResponseBody
    @ApiOperation(value = "perusteen tietojen haku")
    public ResponseEntity<PerusteInfoDto> getMeta(@PathVariable("perusteId") final long id) {
        return ResponseEntity.ok(service.getMeta(id));
    }

    @RequestMapping(value = "/{perusteId}", method = GET)
    @ResponseBody
    @ApiOperation(value = "perusteen tietojen haku")
    public ResponseEntity<PerusteDto> get(@PathVariable("perusteId") final long id) {

        return handleGet(id, 1, () -> service.get(id));
    }

    @RequestMapping(value = "/{perusteId}/version", method = GET)
    @ResponseBody
    @ApiOperation(value = "perusteen uusin versio")
    public PerusteVersionDto getVersion(@PathVariable("perusteId") final long id) {
        return service.getPerusteVersion(id);
    }

    @RequestMapping(value = "/{perusteId}/osaamisalakuvaukset", method = GET)
    @ResponseBody
    @ApiOperation(value = "perusteen osaamisalojen kuvaukset koulutustarjontaa varten")
    public ResponseEntity<Map<Suoritustapakoodi, Map<String, List<TekstiKappaleDto>>>> getOsaamisalat(@PathVariable("perusteId") final long id) {
        return new ResponseEntity<>(service.getOsaamisalaKuvaukset(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/amosaapohja", method = GET)
    @ResponseBody
    @InternalApi
    @ApiOperation(value = "Amosaa jaetun tutkinnon pohja")
    public ResponseEntity<PerusteKaikkiDto> getAmosaaPohja() {
        PerusteKaikkiDto t = service.getAmosaaYhteinenPohja();
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/amosaaops", method = GET)
    @ResponseBody
    @InternalApi
    @ApiOperation(value = "Paikallisen puolen ammatillista laadintaa tukevat perusteet")
    public ResponseEntity<List<PerusteHakuDto>> getAmosaaOpsit() {
        List<PerusteHakuDto> perusteet = service.getAmosaaOpsit();
        return new ResponseEntity<>(perusteet, HttpStatus.OK);
    }

    @RequestMapping(value = "/diaari", method = GET)
    @ResponseBody
    @ApiImplicitParams({
        @ApiImplicitParam(name = "diaarinumero", dataType = "string", paramType = "query"),})
    @ApiOperation(value = "perusteen yksilöintietojen haku diaarinumerolla")
    public ResponseEntity<PerusteInfoDto> getByDiaari(@ApiIgnore final Diaarinumero diaarinumero) {
        PerusteInfoDto t = service.getByDiaari(diaarinumero);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/{perusteId}/kaikki", method = GET)
    @ResponseBody
    @ApiOperation(value = "perusteen kaikkien tietojen haku")
    public ResponseEntity<PerusteKaikkiDto> getKokoSisalto(@PathVariable("perusteId") final long id) {
        return handleGet(id, 3600, () -> service.getKokoSisalto(id));
    }

    @RequestMapping(value = "/{perusteId}/suoritustavat/{suoritustapakoodi}", method = GET)
    @ResponseBody
    @InternalApi
    public ResponseEntity<SuoritustapaDto> getSuoritustapa(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("suoritustapakoodi") final String suoritustapakoodi
    ) {
        return handleGet(perusteId, 1, () -> service.getSuoritustapa(perusteId, Suoritustapakoodi.of(suoritustapakoodi)));
    }

    private <T> ResponseEntity<T> handleGet(Long perusteId, int age, Supplier<T> response) {
        return CacheableResponse.create(service.getPerusteVersion(perusteId), age, response::get);
    }
}
