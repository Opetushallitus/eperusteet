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
package fi.vm.sade.eperusteet.resource.peruste;

import com.mangofactory.swagger.annotations.ApiIgnore;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineenVuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import fi.vm.sade.eperusteet.service.yl.PerusopetuksenPerusteenSisaltoService;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkakokonaisuusService;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 *
 * @author jhyoty
 */
@RestController
@RequestMapping("/perusteet/{perusteId}/perusopetus")
@ApiIgnore
public class PerusopetuksenPerusteenSisaltoController {

    @Autowired
    private PerusopetuksenPerusteenSisaltoService sisaltoService;

    @Autowired
    private OppiaineService oppiaineService;

    @Autowired
    private VuosiluokkakokonaisuusService kokonaisuusService;

    @RequestMapping(value = "/oppiaineet", method = GET)
    public List<OppiaineSuppeaDto> getOppiaineet(
        @PathVariable("perusteId") final Long perusteId) {
        return sisaltoService.getOppiaineet(perusteId);
    }

    @RequestMapping(value = "/oppiaineet", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public OppiaineDto addOppiaine(
        @PathVariable("perusteId") final Long perusteId,
        @RequestBody OppiaineDto dto) {
        return oppiaineService.addOppiaine(perusteId, dto);
    }

    @RequestMapping(value = "/oppiaineet/{id}", method = GET)
    public OppiaineDto getOppiaine(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {
        return oppiaineService.getOppiaine(perusteId, id);
    }

    @RequestMapping(value = "/oppiaineet/{id}", method = POST)
    public OppiaineDto updateOppiaine(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id,
        @RequestBody OppiaineDto dto) {
        dto.setId(id);
        return oppiaineService.updateOppiaine(perusteId, dto);
    }

    @RequestMapping(value = "/oppiaineet/{id}/vuosiluokkakokonaisuudet", method = GET)
    public Collection<OppiaineenVuosiluokkaKokonaisuusDto> getOppiaineenVuosiluokkaKokonaisuudet(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long oppiaineId) {
        return oppiaineService.getOppiaine(perusteId, oppiaineId).getVuosiluokkakokonaisuudet();
    }

    @RequestMapping(value = "/oppiaineet/{id}/vuosiluokkakokonaisuudet", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public OppiaineenVuosiluokkaKokonaisuusDto addOppiaineenVuosiluokkakokonaisuus(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long oppiaineId,
        @RequestBody OppiaineenVuosiluokkaKokonaisuusDto dto) {
        return oppiaineService.addOppiaineenVuosiluokkaKokonaisuus(perusteId, oppiaineId, dto);
    }

    @RequestMapping(value = "/oppiaineet/{oppiaineId}/vuosiluokkakokonaisuudet/{id}", method = GET)
    public OppiaineenVuosiluokkaKokonaisuusDto getOppiaineenVuosiluokkakokonaisuus(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("oppiaineId") final Long oppiaineId,
        @PathVariable("id") final Long id) {
        return oppiaineService.getOppiaineenVuosiluokkaKokonaisuus(perusteId, oppiaineId, id);
    }

    @RequestMapping(value = "/oppiaineet/{oppiaineId}/vuosiluokkakokonaisuudet/{id}", method = POST)
    public OppiaineenVuosiluokkaKokonaisuusDto updateOppiaineenVuosiluokkakokonaisuus(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("oppiaineId") final Long oppiaineId,
        @PathVariable("id") final Long id,
        @RequestBody OppiaineenVuosiluokkaKokonaisuusDto dto) {
        dto.setId(id);
        return oppiaineService.updateOppiaineenVuosiluokkaKokonaisuus(perusteId, oppiaineId, dto);
    }

    @RequestMapping(value = "/vuosiluokkakokonaisuudet", method = GET)
    public List<VuosiluokkaKokonaisuusDto> getVuosiluokkaKokonaisuudet(
        @PathVariable("perusteId") final Long perusteId) {
        return sisaltoService.getVuosiluokkaKokonaisuudet(perusteId);
    }

    @RequestMapping(value = "/vuosiluokkakokonaisuudet", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public VuosiluokkaKokonaisuusDto addVuosiluokkaKokonaisuus(
        @PathVariable("perusteId") final Long perusteId,
        @RequestBody VuosiluokkaKokonaisuusDto dto) {
        return kokonaisuusService.addVuosiluokkaKokonaisuus(perusteId, dto);
    }

    @RequestMapping(value = "/vuosiluokkakokonaisuudet/{id}", method = GET)
    public VuosiluokkaKokonaisuusDto getVuosiluokkaKokonaisuus(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {
        return kokonaisuusService.getVuosiluokkaKokonaisuus(perusteId, id);
    }

    @RequestMapping(value = "/vuosiluokkakokonaisuudet/{id}/oppiaineet", method = GET)
    public List<OppiaineSuppeaDto> getVuosiluokkaKokonaisuudenOppiaineet(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {
        return kokonaisuusService.getOppiaineet(perusteId, id);
    }

    @RequestMapping(value = "/vuosiluokkakokonaisuudet/{id}", method = POST)
    public VuosiluokkaKokonaisuusDto updateVuosiluokkaKokonaisuus(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id,
        @RequestBody VuosiluokkaKokonaisuusDto dto) {
        dto.setId(id);
        return kokonaisuusService.updateVuosiluokkaKokonaisuus(perusteId, dto);
    }

    @RequestMapping(value = "/laajaalaisetosaamiset", method = GET)
    public List<LaajaalainenOsaaminenDto> getOsaamiset(
        @PathVariable("perusteId") final Long perusteId) {
        return sisaltoService.getLaajaalaisetOsaamiset(perusteId);
    }

    @RequestMapping(value = "/laajaalaisetosaamiset", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public List<LaajaalainenOsaaminenDto> updateOsaamiset(
        @PathVariable("perusteId") final Long perusteId,
        @RequestBody List<LaajaalainenOsaaminenDto> dto) {
        return sisaltoService.updateLaajaalaisetOsaamiset(perusteId, dto);
    }

}
