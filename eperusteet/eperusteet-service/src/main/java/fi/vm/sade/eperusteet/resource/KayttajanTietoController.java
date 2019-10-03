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

import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 *
 * @author nkala
 */
@RestController
@RequestMapping("/kayttajatieto")
@InternalApi
public class KayttajanTietoController {
    @Autowired
    KayttajanTietoService service;

    @RequestMapping(method = GET)
    @ResponseBody
    public ResponseEntity<KayttajanTietoDto> getOmaKayttaja() {
        return new ResponseEntity<>(service.hae(null), HttpStatus.OK);
    }

    @RequestMapping(value = "/{oid:.+}", method = GET)
    @ResponseBody
    public ResponseEntity<KayttajanTietoDto> getKayttaja(@PathVariable("oid") final String oid) {
        return new ResponseEntity<>(service.hae(oid), HttpStatus.OK);
    }

    @RequestMapping(value = "/{oid:.+}/perusteprojektit", method = GET)
    @ResponseBody
    public ResponseEntity<List<KayttajanProjektitiedotDto>> getKayttajanPerusteprojektit(@PathVariable("oid") final String oid) {
        return new ResponseEntity<>(service.haePerusteprojektit(oid), HttpStatus.OK);
    }

    @RequestMapping(value = "/{oid:.+}/perusteprojektit/{projektiId}", method = GET)
    @ResponseBody
    public ResponseEntity<KayttajanProjektitiedotDto> getKayttajanPerusteprojekti(
            @PathVariable("oid") final String oid,
            @PathVariable("projektiId") final Long projektiId
    ) {
        KayttajanProjektitiedotDto kayttajanProjektitiedot = service.haePerusteprojekti(oid, projektiId);
        if (kayttajanProjektitiedot == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(kayttajanProjektitiedot, HttpStatus.OK);
    }

}
