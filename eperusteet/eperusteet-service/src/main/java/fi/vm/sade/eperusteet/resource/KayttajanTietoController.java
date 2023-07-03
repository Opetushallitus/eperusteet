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

import java.util.Base64;
import java.util.List;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 *
 * @author nkala
 */
@RestController
@RequestMapping("/kayttajatieto")
@Api("Kayttajat")
@InternalApi
public class KayttajanTietoController {

    @Autowired
    KayttajanTietoService service;

    @RequestMapping(value = "/redirect", method = RequestMethod.POST)
    public String haeLoginRedirectUrl(
            @RequestBody final String redirectUrl
    ) {
        return service.haeLoginRedirectUrl(redirectUrl);
    }

    @RequestMapping(method = RequestMethod.GET)
    public KayttajanTietoDto getKirjautunutKayttajat() {
        return service.haeKirjautaunutKayttaja();
    }

    @RequestMapping(value = "/{oid:.+}", method = GET)
    public KayttajanTietoDto getKayttaja(@PathVariable("oid") final String oid) {
        return service.hae(oid);
    }

    @RequestMapping(value = "/{oid:.+}/perusteprojektit", method = GET)
    public List<KayttajanProjektitiedotDto> getKayttajanPerusteprojektit(@PathVariable("oid") final String oid) {
        return service.haePerusteprojektit(oid);
    }

    @RequestMapping(value = "/{oid:.+}/perusteprojektit/{projektiId}", method = GET)
    public KayttajanProjektitiedotDto getKayttajanPerusteprojekti(
            @PathVariable("oid") final String oid,
            @PathVariable("projektiId") final Long projektiId
    ) {
        return service.haePerusteprojekti(oid, projektiId);
    }

}
