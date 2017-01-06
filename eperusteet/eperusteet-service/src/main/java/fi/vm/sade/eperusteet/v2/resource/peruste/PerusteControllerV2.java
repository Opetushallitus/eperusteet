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

package fi.vm.sade.eperusteet.v2.resource.peruste;

import com.google.common.base.Supplier;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.resource.util.CacheableResponse;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.v2.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.v2.service.PerusteServiceV2;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 *
 * @author isaul
 */
@Controller
@RequestMapping(value = "/v2/perusteet", produces = "application/json;charset=UTF-8")
@Api(value = "Perusteet", description = "Perusteiden hallintaan liittyvät operaatiot")
public class PerusteControllerV2 {

    @Autowired
    private KoodistoClient koodistoService;

    @Autowired
    private PerusteServiceV2 service;

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

    @RequestMapping(value = "/{perusteId}/kaikki", method = GET)
    @ResponseBody
    @ApiOperation(value = "perusteen kaikkien tietojen haku")
    public ResponseEntity<PerusteKaikkiDto> getKokoSisalto(
            @PathVariable("perusteId") final long id) {

        return handleGet(id, 3600, new Supplier<PerusteKaikkiDto>() {
            @Override
            public PerusteKaikkiDto get() {
                return service.getKokoSisalto(id);
            }
        });
    }

    private <T> ResponseEntity<T> handleGet(Long perusteId, int age, Supplier<T> response) {
        return CacheableResponse.create(service.getPerusteVersion(perusteId), age, response);
    }
}