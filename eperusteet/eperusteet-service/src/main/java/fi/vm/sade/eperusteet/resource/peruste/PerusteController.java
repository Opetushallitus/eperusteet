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

import com.wordnik.swagger.annotations.Api;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteInfoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.peruste.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.service.KoodistoService;
import fi.vm.sade.eperusteet.service.PerusteService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@Controller
@RequestMapping("/perusteet")
@Api(value = "Perusteet", description = "Perusteiden hallintaan liittyvät operaatiot")
public class PerusteController {

    @Autowired
    private KoodistoService koodistoService;

    @Autowired
    private PerusteService service;

    @RequestMapping(value = "/info", method = GET)
    @ResponseBody
    public Page<PerusteInfoDto> getAllInfo(PerusteQuery pquery) {
        // Vain valmiita perusteita voi hakea tämän rajapinnan avulla
        pquery.setTila(PerusteTila.VALMIS.toString());
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return service.findByInfo(p, pquery);
    }

    @RequestMapping(method = GET)
    @ResponseBody
    public Page<PerusteDto> getAll(PerusteQuery pquery) {
        // Vain valmiita perusteita voi hakea tämän rajapinnan avulla
        pquery.setTila(PerusteTila.VALMIS.toString());
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return service.findBy(p, pquery);
    }

    @RequestMapping(value = "/{perusteId}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PerusteDto update(@PathVariable("perusteId") final long id, @RequestBody PerusteDto perusteDto) {
        perusteDto = service.update(id, perusteDto);
        return perusteDto;
    }

    @RequestMapping(value = "/{perusteId}/tutkintonimikekoodit/{tutkintonimikeKoodiId}", method = DELETE)
    @ResponseBody
    public ResponseEntity<TutkintonimikeKoodiDto> addTutkintonimikekoodi(
            @PathVariable("perusteId") final long id,
            @PathVariable("tutkintonimikeKoodiId") final Long tnkId) {
        service.removeTutkintonimikeKoodi(id, tnkId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{perusteId}/tutkintonimikekoodit", method = { POST, PUT })
    @ResponseBody
    public ResponseEntity<TutkintonimikeKoodiDto> addTutkintonimikekoodi(
            @PathVariable("perusteId") final long id,
            @RequestBody final TutkintonimikeKoodiDto tnk) {
        TutkintonimikeKoodiDto tutkintonimikeKoodi = service.addTutkintonimikeKoodi(id, tnk);
        return new ResponseEntity<>(tutkintonimikeKoodi, HttpStatus.OK);
    }

    @RequestMapping(value = "/{perusteId}/tutkintonimikekoodit", method = GET)
    @ResponseBody
    public ResponseEntity<List<CombinedDto<TutkintonimikeKoodiDto, HashMap<String, KoodistoKoodiDto>>>> getTutkintonimikekoodit(@PathVariable("perusteId") final long id) {
        List<TutkintonimikeKoodiDto> tutkintonimikeKoodit = service.getTutkintonimikeKoodit(id);
        List<CombinedDto<TutkintonimikeKoodiDto, HashMap<String, KoodistoKoodiDto>>> response = new ArrayList<>();

        for (TutkintonimikeKoodiDto tkd : tutkintonimikeKoodit) {
            KoodistoKoodiDto get = koodistoService.get("osaamisala", tkd.getOsaamisalaUri());
            HashMap<String, KoodistoKoodiDto> nimet = new HashMap<>();
            nimet.put(tkd.getOsaamisalaArvo(), koodistoService.get("osaamisala", tkd.getOsaamisalaUri()));
            nimet.put(tkd.getTutkintonimikeArvo(), koodistoService.get("tutkintonimikkeet", tkd.getTutkintonimikeUri()));
            if (tkd.getTutkinnonOsaUri() != null) {
                nimet.put(tkd.getTutkinnonOsaArvo(), koodistoService.get("tutkinnonosat", tkd.getTutkinnonOsaUri()));
            }
            response.add(new CombinedDto<>(tkd, nimet));
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/{perusteId}", method = GET)
    @ResponseBody
    public ResponseEntity<PerusteDto> get(@PathVariable("perusteId") final long id) {
        PerusteDto t = service.get(id);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/{perusteId}/kaikki", method = GET)
    @ResponseBody
    public ResponseEntity<PerusteKaikkiDto> getKokoSisalto(@PathVariable("perusteId") final long id) {
        PerusteKaikkiDto kokoSisalto = service.getKokoSisalto(id);
        return new ResponseEntity<>(kokoSisalto, HttpStatus.OK);
    }

    @RequestMapping(value = "/{perusteId}/suoritustavat/{suoritustapakoodi}", method = GET)
    @ResponseBody
    public ResponseEntity<SuoritustapaDto> getSuoritustapa(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapakoodi") final String suoritustapakoodi) {

        SuoritustapaDto dto = service.getSuoritustapa(perusteId, Suoritustapakoodi.of(suoritustapakoodi));
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}
