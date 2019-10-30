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

import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.peruste.*;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.Ammattitaitovaatimus2019Dto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.resource.util.CacheableResponse;
import fi.vm.sade.eperusteet.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping(value = "/perusteet", produces = "application/json;charset=UTF-8")
@Api(value = "Perusteet")
public class PerusteController {

    @Autowired
    private KoodistoClient koodistoService;

    @Autowired
    private PerusteService service;

    @Autowired
    private AmmattitaitovaatimusService ammattitaitovaatimusService;

    @RequestMapping(value = "/info", method = GET)
    @ResponseBody
    @InternalApi
    public Page<PerusteInfoDto> getAllPerusteetInfo(PerusteQuery pquery) {
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
    public ResponseEntity<List<PerusteKoosteDto>> getPerusteKooste() {
        return new ResponseEntity<>(service.getKooste(), HttpStatus.OK);
    }

    @RequestMapping(value = "/uusimmat", method = GET)
    @ResponseBody
    public ResponseEntity<List<PerusteDto>> getUusimmatPerusteet(@RequestParam(defaultValue = "fi") String kieli) {
        HashSet<Kieli> kielet = new HashSet<>();
        kielet.add(Kieli.of(kieli));
        return new ResponseEntity<>(service.getUusimmat(kielet), HttpStatus.OK);
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
            @ApiImplicitParam(name = "sivu", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "sivukoko", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "tuleva", dataType = "boolean", paramType = "query", defaultValue = "true", value = "hae myös tulevatperusteet"),
            @ApiImplicitParam(name = "siirtyma", dataType = "boolean", paramType = "query", defaultValue = "true", value = "hae myös siirtymäajalla olevat perusteet"),
            @ApiImplicitParam(name = "voimassaolo", dataType = "boolean", paramType = "query", defaultValue = "true", value = "hae myös voimassaolevat perusteet"),
            @ApiImplicitParam(name = "poistunut", dataType = "boolean", paramType = "query", defaultValue = "true", value = "hae myös poistuneet perusteet"),
            @ApiImplicitParam(name = "nimi", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "koulutusala", dataType = "string", paramType = "query", allowMultiple = true),
            @ApiImplicitParam(name = "koulutustyyppi", dataType = "string", paramType = "query", allowMultiple = true, value = "koulutustyyppi (koodistokoodi)"),
            @ApiImplicitParam(name = "kieli", dataType = "string", paramType = "query", allowMultiple = true, value = "perusteen kieli"),
            @ApiImplicitParam(name = "opintoala", dataType = "string", paramType = "query", allowMultiple = true, value = "opintoalakoodi"),
            @ApiImplicitParam(name = "suoritustapa", dataType = "string", paramType = "query", value = "AM-perusteet; naytto tai ops"),
            @ApiImplicitParam(name = "koulutuskoodi", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "diaarinumero", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "muokattu", dataType = "long", paramType = "query", value = "Perustetta muokattu jälkeen (aikaleima; millisenkunteja alkaen 1970-01-01 00:00:00 UTC). Huomioi koko perusteen sisällön."),
            @ApiImplicitParam(name = "tutkintonimikkeet", dataType = "boolean", paramType = "query", value = "hae myös tutkintonimikkeistä"),
            @ApiImplicitParam(name = "tutkinnonosat", dataType = "boolean", paramType = "query", value = "hae myös tutkinnon osista"),
            @ApiImplicitParam(name = "osaamisalat", dataType = "boolean", paramType = "query", value = "hae myös osaamisaloista"),
            @ApiImplicitParam(name = "koulutusvienti", dataType = "boolean", paramType = "query", value = "Haku ainoastaan koulutusviennistä")
    })
    public Page<PerusteHakuDto> getAllPerusteet(@ApiIgnore PerusteQuery pquery) {
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return service.findJulkinenBy(p, pquery);
    }

    @RequestMapping(value = "/internal", method = GET)
    @ResponseBody
    @InternalApi
    @ApiOperation(value = "perusteiden sisäinen haku")
    public Page<PerusteHakuInternalDto> getAllPerusteetInternal(@ApiIgnore PerusteQuery pquery) {
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return service.findByInternal(p, pquery);
    }

    @RequestMapping(value = "/{perusteId}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @InternalApi
    public PerusteDto updatePeruste(@PathVariable("perusteId") final long id, @RequestBody PerusteDto perusteDto) {
        return service.updateFull(id, perusteDto);
    }

    @InternalApi
    @RequestMapping(value = "/{perusteId}/navigaatio", method = GET)
    public NavigationNodeDto getNavigation(
            @PathVariable final Long perusteId
    ) {
        return service.buildNavigation(perusteId);
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
    public ResponseEntity<TutkintonimikeKoodiDto> removeTutkintonimikekoodi(
            @PathVariable("perusteId") final long id,
            @PathVariable("tutkintonimikeKoodiId") final Long tnkId) {
        service.removeTutkintonimikeKoodi(id, tnkId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{perusteId}/tutkintonimikekoodit", method = POST)
    @ResponseBody
    @InternalApi
    public ResponseEntity<TutkintonimikeKoodiDto> addTutkintonimikekoodi(
            @PathVariable("perusteId") final long id,
            @RequestBody final TutkintonimikeKoodiDto tnk) {
        TutkintonimikeKoodiDto tutkintonimikeKoodi = service.addTutkintonimikeKoodi(id, tnk);
        return new ResponseEntity<>(tutkintonimikeKoodi, HttpStatus.OK);
    }

    // Openapi generator
    @RequestMapping(value = "/{perusteId}/tutkintonimikekoodit", method = PUT)
    @ResponseBody
    @InternalApi
    public ResponseEntity<TutkintonimikeKoodiDto> addTutkintonimikekoodiPut(
            @PathVariable("perusteId") final long id,
            @RequestBody final TutkintonimikeKoodiDto tnk) {
        return addTutkintonimikekoodi(id, tnk);
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
    public ResponseEntity<PerusteDto> getPerusteenTiedot(@PathVariable("perusteId") final long id) {

        return handleGet(id, 1, () -> service.get(id));
    }

    @RequestMapping(value = "/{perusteId}/version", method = GET)
    @ResponseBody
    @ApiOperation(value = "perusteen uusin versio")
    public PerusteVersionDto getPerusteVersion(@PathVariable("perusteId") final long id) {
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
            @ApiImplicitParam(name = "diaarinumero", dataType = "string", paramType = "query")
    })
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
    public ResponseEntity<PerusteKaikkiDto> getKokoSisalto(
            @PathVariable("perusteId") final long id,
            @RequestParam(value = "rev", required = false) final Integer rev) {
        return handleGet(id, 3600, () -> service.getKokoSisalto(id, rev));
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

    @ResponseBody
    @RequestMapping(value = "/{perusteId}/tutkinnonosat/ammattitaitovaatimuskoodisto", method = POST)
    public List<KoodiDto> pushAmmattitaitovaatimuksetToKoodisto(
            @PathVariable("perusteId") final Long perusteId) {
        return ammattitaitovaatimusService.addAmmattitaitovaatimuskooditToKoodisto(perusteId);
    }

    @ResponseBody
    @RequestMapping(value = "/{perusteId}/tutkinnonosat/ammattitaitovaatimukset", method = GET)
    public List<Ammattitaitovaatimus2019Dto> getAmmattitaitovaatimukset(
            @PathVariable("perusteId") final Long perusteId) {
        return ammattitaitovaatimusService.getAmmattitaitovaatimukset(perusteId);
    }

}
