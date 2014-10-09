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
import fi.vm.sade.eperusteet.dto.KommenttiDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.KommenttiService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 *
 * @author nkala
 */
@RestController
@RequestMapping("/kommentit")
@ApiIgnore
public class KommenttiController {

    @Autowired
    KommenttiService service;

    @Autowired
    KayttajanTietoService kayttajanTietoService;

    private List<KommenttiDto> rikastaKommentit(List<KommenttiDto> kommentit) {
        for (KommenttiDto k : kommentit) {
            KayttajanTietoDto kayttaja = kayttajanTietoService.hae(k.getMuokkaaja());
            if (kayttaja != null) {
                String kutsumanimi = kayttaja.getKutsumanimi();
                String etunimet = kayttaja.getEtunimet();
                String etunimi = kutsumanimi != null ? kutsumanimi : etunimet;
                k.setNimi(etunimi + " " + kayttaja.getSukunimi());
            }
        }
        return kommentit;
    }

    @RequestMapping(value = "/perusteprojekti/{id}/suoritustapa/{suoritustapa}", method = GET)
    public ResponseEntity<List<KommenttiDto>> getAll(@PathVariable("id") final long id, @PathVariable("suoritustapa") final String suoritustapa) {
        List<KommenttiDto> t = service.getAllBySuoritustapa(id, suoritustapa);
        return new ResponseEntity<>(rikastaKommentit(t), HttpStatus.OK);
    }

    @RequestMapping(value = "/perusteprojekti/{id}/perusteenosa/{perusteenOsaId}", method = GET)
    public ResponseEntity<List<KommenttiDto>> getAll(@PathVariable("id") final long id, @PathVariable("perusteenOsaId") final long perusteenOsaId) {
        List<KommenttiDto> t = service.getAllByPerusteenOsa(id, perusteenOsaId);
        return new ResponseEntity<>(rikastaKommentit(t), HttpStatus.OK);
    }

    @RequestMapping(value = "/perusteprojekti/{id}", method = GET)
    public ResponseEntity<List<KommenttiDto>> getAll(@PathVariable("id") final long id) {
        List<KommenttiDto> t = service.getAllByPerusteprojekti(id);
        return new ResponseEntity<>(rikastaKommentit(t), HttpStatus.OK);
    }

    @RequestMapping(value = "/ylin/{id}", method = GET)
    public ResponseEntity<List<KommenttiDto>> getAllByYlin(@PathVariable("id") final long id) {
        List<KommenttiDto> t = service.getAllByYlin(id);
        return new ResponseEntity<>(rikastaKommentit(t), HttpStatus.OK);
    }

    @RequestMapping(value = "/parent/{id}", method = GET)
    public ResponseEntity<List<KommenttiDto>> getAllByParent(@PathVariable("id") final long id) {
        List<KommenttiDto> t = service.getAllByParent(id);
        return new ResponseEntity<>(rikastaKommentit(t), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = GET)
    public ResponseEntity<KommenttiDto> get(@PathVariable("id") final long id) {
        KommenttiDto t = service.get(id);
        return new ResponseEntity<>(t, t == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @RequestMapping(method = {POST, PUT})
    public ResponseEntity<KommenttiDto> add(@RequestBody KommenttiDto body) {
        return new ResponseEntity<>(service.add(body), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}", method = {POST, PUT})
    public ResponseEntity<KommenttiDto> update(@PathVariable("id") final long id, @RequestBody KommenttiDto body) {
        return new ResponseEntity<>(service.update(id, body), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = DELETE)
    public void delete(@PathVariable("id") final long id) {
        service.delete(id);
    }
}
