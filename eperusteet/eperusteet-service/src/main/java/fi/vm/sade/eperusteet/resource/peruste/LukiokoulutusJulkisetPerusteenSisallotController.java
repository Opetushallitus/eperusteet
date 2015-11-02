/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.resource.peruste;

import com.google.common.base.Supplier;
import com.wordnik.swagger.annotations.Api;
import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukioOppiainePuuDto;
import fi.vm.sade.eperusteet.resource.util.CacheableResponse;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.yl.LukiokoulutuksenPerusteenSisaltoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * User: tommiratamaa
 * Date: 2.11.15
 * Time: 12.39
 */
@RestController
@RequestMapping("/perusteet/{perusteId}/lukiokoulutus/julkinen")
@Api(value = "Lukioperusteen julkiset tiedot")
public class LukiokoulutusJulkisetPerusteenSisallotController {
    @Autowired
    private LukiokoulutuksenPerusteenSisaltoService sisaltoService;

    @Autowired
    private PerusteService perusteet;

    @RequestMapping(value = "/oppiainerakenne", method = RequestMethod.GET)
    public ResponseEntity<LukioOppiainePuuDto> getOppiainePuuRakenne(@PathVariable("perusteId") Long perusteId) {
        return handleGet(perusteId, () -> sisaltoService.getOppiaineTreeStructure(perusteId));
    }

    private <T> ResponseEntity<T> handleGet(Long perusteId, Supplier<T> response) {
        return CacheableResponse.create(perusteet.getLastModifiedRevision(perusteId), 1, response);
    }

}
