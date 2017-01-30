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

import fi.vm.sade.eperusteet.dto.yl.AIPEKurssiDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEKurssiSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEVaiheDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEVaiheSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.yl.AIPEOpetuksenPerusteenSisaltoService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * @author nkala
 */
@RestController
@RequestMapping("/perusteet/{perusteId}/aipeopetus")
@InternalApi
public class AIPEOpetuksenSisaltoController {
    private static final Logger logger = LoggerFactory.getLogger(AIPEOpetuksenSisaltoController.class);

    @Autowired
    AIPEOpetuksenPerusteenSisaltoService sisalto;


    @RequestMapping(value = "/vaiheet", method = GET)
    public ResponseEntity<List<AIPEVaiheSuppeaDto>> getVaiheet(
            @PathVariable("perusteId") final Long perusteId) {
        return ResponseEntity.ok(sisalto.getVaiheet(perusteId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}", method = GET)
    public ResponseEntity<AIPEVaiheDto> getVaihe(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId) {
        return ResponseEntity.ok(sisalto.getVaihe(perusteId, vaiheId));
    }

    @RequestMapping(value = "/laajaalaiset", method = GET)
    public ResponseEntity<List<LaajaalainenOsaaminenDto>> getLaajaalaiset(
            @PathVariable("perusteId") final Long perusteId) {
        return ResponseEntity.ok(sisalto.getLaajaalaiset(perusteId));
    }

    @RequestMapping(value = "/laajaalaiset/{laajalainenId}", method = GET)
    public ResponseEntity<LaajaalainenOsaaminenDto> getLaajaalainen(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("laajalainenId") final Long laajalainenId) {
        return ResponseEntity.ok(sisalto.getLaajalainen(perusteId, laajalainenId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet", method = GET)
    public ResponseEntity<List<AIPEOppiaineSuppeaDto>> getOppiaineet(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId) {
        return ResponseEntity.ok(sisalto.getOppiaineet(perusteId, vaiheId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}", method = GET)
    public ResponseEntity<AIPEOppiaineDto> getOppiaine(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId,
            @PathVariable("oppiaineId") final Long oppiaineId) {
        return ResponseEntity.ok(sisalto.getOppiaine(perusteId, vaiheId, oppiaineId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit", method = GET)
    public ResponseEntity<List<AIPEKurssiSuppeaDto>> getKurssit(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId,
            @PathVariable("oppiaineId") final Long oppiaineId) {
        return ResponseEntity.ok(sisalto.getKurssit(perusteId, vaiheId, oppiaineId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit/{kurssiId}", method = GET)
    public ResponseEntity<AIPEKurssiDto> getKurssit(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId,
            @PathVariable("oppiaineId") final Long oppiaineId,
            @PathVariable("kurssiId") final Long kurssiId) {
        return ResponseEntity.ok(sisalto.getKurssi(perusteId, vaiheId, oppiaineId, kurssiId));
    }

}
