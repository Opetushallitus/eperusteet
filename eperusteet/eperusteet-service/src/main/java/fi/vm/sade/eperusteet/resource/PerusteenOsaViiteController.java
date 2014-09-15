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

import com.wordnik.swagger.annotations.Api;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.resource.util.PerusteenOsaMappings;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author harrik
 */
@Controller
@RequestMapping("/perusteenosaviitteet")
@Api(value="Perusteet")
public class PerusteenOsaViiteController {

    @Autowired
    private PerusteenOsaViiteService service;

    @RequestMapping(value = "/kloonaa/{id}", method = POST, params = PerusteenOsaMappings.IS_TUTKINNON_OSA_PARAM)
    @ResponseBody
    public TutkinnonOsaViiteDto kloonaaTutkinnonOsa(@PathVariable("id") final Long id) {
        return service.kloonaaTutkinnonOsa(id);
    }
}
