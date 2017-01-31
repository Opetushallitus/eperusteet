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

    @RequestMapping(value = "/vaiheet", method = POST)
    @ResponseBody
    public ResponseEntity<AIPEVaiheSuppeaDto> addVaihe(
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody AIPEVaiheDto vaiheDto) {
        return ResponseEntity.ok(sisalto.addVaihe(perusteId, vaiheDto));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}", method = GET)
    public ResponseEntity<AIPEVaiheDto> getVaihe(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId) {
        return ResponseEntity.ok(sisalto.getVaihe(perusteId, vaiheId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}", method = DELETE)
    public ResponseEntity removeVaihe(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId) {
        sisalto.removeVaihe(perusteId, vaiheId);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}", method = PUT)
    @ResponseBody
    public ResponseEntity<AIPEVaiheSuppeaDto> updateVaihe(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId,
            @RequestBody AIPEVaiheDto vaiheDto) {
        return ResponseEntity.ok(sisalto.updateVaihe(perusteId, vaiheId, vaiheDto));
    }

    @RequestMapping(value = "/laajaalaiset", method = GET)
    public ResponseEntity<List<LaajaalainenOsaaminenDto>> getLaajaalaiset(
            @PathVariable("perusteId") final Long perusteId) {
        return ResponseEntity.ok(sisalto.getLaajaalaiset(perusteId));
    }

    @RequestMapping(value = "/laajaalaiset", method = POST)
    public ResponseEntity<LaajaalainenOsaaminenDto> addLaajaalainen(
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody LaajaalainenOsaaminenDto loDto) {
        return ResponseEntity.ok(sisalto.addLaajaalainen(perusteId, loDto));
    }

    @RequestMapping(value = "/laajaalaiset/{laajaalainenId}", method = PUT)
    public ResponseEntity<LaajaalainenOsaaminenDto> addLaajaalainen(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("laajaalainenId") final Long laajaalainenId,
            @RequestBody LaajaalainenOsaaminenDto loDto) {
        return ResponseEntity.ok(sisalto.updateLaajaalainen(perusteId, laajaalainenId, loDto));
    }

    @RequestMapping(value = "/laajaalaiset/{laajaalainenId}", method = DELETE)
    public ResponseEntity addLaajaalainen(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("laajaalainenId") final Long laajaalainenId) {
        sisalto.removeLaajaalainen(perusteId, laajaalainenId);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/laajaalaiset/{laajalainenId}", method = GET)
    public ResponseEntity<LaajaalainenOsaaminenDto> getLaajaalainen(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("laajalainenId") final Long laajalainenId) {
        return ResponseEntity.ok(sisalto.getLaajaalainen(perusteId, laajalainenId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet", method = GET)
    public ResponseEntity<List<AIPEOppiaineSuppeaDto>> getOppiaineet(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId) {
        return ResponseEntity.ok(sisalto.getOppiaineet(perusteId, vaiheId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet", method = POST)
    public ResponseEntity<AIPEOppiaineSuppeaDto> addOppiaine(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId,
            @RequestBody AIPEOppiaineDto oppiaineDto) {
        return ResponseEntity.ok(sisalto.addOppiaine(perusteId, vaiheId, oppiaineDto));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}", method = PUT)
    public ResponseEntity<AIPEOppiaineSuppeaDto> updateOppiaine(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId,
            @PathVariable("oppiaineId") final Long oppiaineId,
            @RequestBody AIPEOppiaineDto oppiaineDto) {
        return ResponseEntity.ok(sisalto.updateOppiaine(perusteId, vaiheId, oppiaineId, oppiaineDto));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}", method = DELETE)
    public ResponseEntity removeOppiaine(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId,
            @PathVariable("oppiaineId") final Long oppiaineId) {
        sisalto.removeOppiaine(perusteId, vaiheId, oppiaineId);
        return ResponseEntity.ok().build();
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

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit", method = POST)
    public ResponseEntity<AIPEKurssiDto> addKurssi(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId,
            @PathVariable("oppiaineId") final Long oppiaineId,
            @RequestBody AIPEKurssiDto kurssiDto) {
        return ResponseEntity.ok(sisalto.addKurssi(perusteId, vaiheId, oppiaineId, kurssiDto));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit/{kurssiId}", method = PUT)
    public ResponseEntity<AIPEKurssiDto> updateKurssi(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId,
            @PathVariable("oppiaineId") final Long oppiaineId,
            @PathVariable("kurssiId") final Long kurssiId,
            @RequestBody AIPEKurssiDto kurssiDto) {
        return ResponseEntity.ok(sisalto.updateKurssi(perusteId, vaiheId, oppiaineId, kurssiId, kurssiDto));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit/{kurssiId}", method = DELETE)
    public ResponseEntity removeKurssi(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId,
            @PathVariable("oppiaineId") final Long oppiaineId,
            @PathVariable("kurssiId") final Long kurssiId) {
        sisalto.removeKurssi(perusteId, vaiheId, oppiaineId, kurssiId);
        return ResponseEntity.ok().build();
    }

}
