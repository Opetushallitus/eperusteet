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
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author nkala
 */
@Controller
@RequestMapping("/kayttajatieto")
public class KayttajanTietoController {
    @Autowired
    KayttajanTietoService service;

    @RequestMapping(method = GET)
    @ResponseBody
    public ResponseEntity<KayttajanTietoDto> get() {
        return new ResponseEntity<>(service.hae(null), HttpStatus.OK);
    }

    @RequestMapping(value = "/{oid:.+}", method = GET)
    @ResponseBody
    public ResponseEntity<KayttajanTietoDto> get(@PathVariable("oid") final String oid) {
        return new ResponseEntity<>(service.hae(oid), HttpStatus.OK);
    }

    @RequestMapping(value = "/{oid:.+}/perusteprojektit", method = GET)
    @ResponseBody
    public ResponseEntity<List<KayttajanProjektitiedotDto>> getPerusteprojektit(@PathVariable("oid") final String oid) {
        return new ResponseEntity<>(service.haePerusteprojektit(oid), HttpStatus.OK);
    }

    @RequestMapping(value = "/{oid:.+}/perusteprojektit/{projektiId}", method = GET)
    @ResponseBody
    public ResponseEntity<KayttajanProjektitiedotDto> getPerusteprojekti(
            @PathVariable("oid") final String oid,
            @PathVariable("projektiId") final Long projektiId
    ) {
        return new ResponseEntity<>(service.haePerusteprojekti(oid, projektiId), HttpStatus.OK);
    }

    @RequestMapping(value = "/{oid:.+}/kaikki", method = GET)
    @ResponseBody
    public ResponseEntity<KayttajanTietoDto> getKaikki(@PathVariable("oid") final String oid) {
        return new ResponseEntity<>(service.hae(oid), HttpStatus.OK);
    }

    @RequestMapping(value = "/{oid:.+}/yhteystiedot", method = GET)
    @ResponseBody
    public ResponseEntity<KayttajanTietoDto> getYhteystiedot(@PathVariable("oid") final String oid) {
        return new ResponseEntity<>(service.hae(oid), HttpStatus.OK);
    }
}
