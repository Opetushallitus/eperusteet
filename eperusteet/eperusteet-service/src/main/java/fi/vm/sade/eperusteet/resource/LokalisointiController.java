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

import fi.vm.sade.eperusteet.dto.LokalisointiDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.LokalisointiService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author isaul
 */

@InternalApi
@RestController
@RequestMapping("/lokalisointi")
@Api("Lokalisointi")
public class LokalisointiController {

    @Autowired
    private LokalisointiService lokalisointiService;

    @RequestMapping(value = "/eperusteet-opintopolku", method = GET)
    public List<LokalisointiDto> getAllKaannokset(@RequestParam(value = "locale", defaultValue = "fi") final String kieli) {
        return lokalisointiService.getAllByCategoryAndLocale("eperusteet-opintopolku", kieli);
    }

    @RequestMapping(value = "/kaannokset", method = GET)
    public List<LokalisointiDto> getEperusteKaannokset() {
        ArrayList<LokalisointiDto> kaannokset = new ArrayList<>();
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet", "fi"));
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet", "sv"));
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet", "en"));
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet-opintopolku", "fi"));
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet-opintopolku", "sv"));
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet-opintopolku", "en"));
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet-ylops", "fi"));
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet-ylops", "sv"));
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet-ylops", "en"));
        return kaannokset;
    }

    @RequestMapping(value = "/kaannokset", method = POST)
    public void updateKaannokset(@RequestBody final List<LokalisointiDto> lokalisoinnit) {
        lokalisointiService.save(lokalisoinnit);
    }

}
