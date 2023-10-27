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

import java.io.IOException;
import java.util.List;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

    @GetMapping(value = "/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().invalidate();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        String url = request.getRequestURL().toString().replace(request.getRequestURI(),"");
        response.sendRedirect(url + "/service-provider-app/saml/logout");
    }

}
