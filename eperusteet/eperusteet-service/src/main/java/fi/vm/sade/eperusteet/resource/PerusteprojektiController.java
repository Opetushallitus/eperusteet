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

import fi.vm.sade.eperusteet.domain.Henkilo;
import fi.vm.sade.eperusteet.domain.Rooli;
import fi.vm.sade.eperusteet.dto.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class PerusteprojektiController {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteprojektiController.class);

    // FIXME: poista kun on aika
    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteprojektiService service;

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
    public ResponseEntity<List<Henkilo>> getJasenet(@PathVariable("id") final long id) {
        List<Henkilo> t = new ArrayList<>();
        t.add(new Henkilo("1.1.1.1.1", "Olaf Omistaja", "040-1234567", "email.omistaja@joku.fi", Rooli.of("omistaja")));
        t.add(new Henkilo("1.1.1.1.1", "Sirkku Sihteeri", "040-1234567", "email.email@joku.fi", Rooli.of("sihteeri")));
        t.add(new Henkilo("1.1.1.1.1", "Jetro Jäsen", "040-1234567", "email.email@joku.fi", Rooli.of("jasen")));
        t.add(new Henkilo("1.1.1.1.1", "Jaana Jäsen", "040-1234567", "email.email@joku.fi", Rooli.of("jasen")));
        t.add(new Henkilo("1.1.1.1.1", "Erkki Esimerkki", "040-1234567", "erkkimail@joku.fi", Rooli.of("jasen")));
        t.add(new Henkilo("1.1.1.1.1", "Kalle Kommentoija", "040-1234567", "kalle@kommentoija.fi", Rooli.of("kommentoija")));
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PerusteprojektiDto update(@PathVariable("id") final long id, @RequestBody PerusteprojektiDto perusteprojektiDto) {
        LOG.info("update {}", perusteprojektiDto);
        perusteprojektiDto = service.update(id, perusteprojektiDto);
        return perusteprojektiDto;
    }

    @RequestMapping(method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<PerusteprojektiDto> add(@RequestBody PerusteprojektiLuontiDto perusteprojektiLuontiDto, UriComponentsBuilder ucb) {
        LOG.info("add {}", perusteprojektiLuontiDto);
        PerusteprojektiDto perusteprojektiDto = service.save(perusteprojektiLuontiDto);
        return new ResponseEntity<>(perusteprojektiDto, buildHeadersFor(perusteprojektiDto.getId(), ucb), HttpStatus.CREATED);
    }

    private HttpHeaders buildHeadersFor(Long id, UriComponentsBuilder ucb) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucb.path("/perusteprojektit/{id}").buildAndExpand(id).toUri());
        return headers;
    }
}
