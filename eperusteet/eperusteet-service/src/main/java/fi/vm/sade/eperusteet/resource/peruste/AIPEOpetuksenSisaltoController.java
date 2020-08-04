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

import fi.vm.sade.eperusteet.dto.yl.AIPEKurssiBaseDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEKurssiDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEKurssiSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOppiaineBaseDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEVaiheBaseDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEVaiheDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEVaiheSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.OpetuksenKohdealueDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.yl.AIPEOpetuksenPerusteenSisaltoService;
import io.swagger.annotations.Api;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * @author nkala
 */
@RestController
@RequestMapping("/perusteet/{perusteId}/aipeopetus")
@Api(value = "Aipeopetuksensisalto")
@InternalApi
public class AIPEOpetuksenSisaltoController {
    private static final Logger logger = LoggerFactory.getLogger(AIPEOpetuksenSisaltoController.class);

    @Autowired
    AIPEOpetuksenPerusteenSisaltoService sisalto;

    @RequestMapping(value = "/vaiheet", method = GET)
    public ResponseEntity<List<AIPEVaiheSuppeaDto>> getVaiheet(
            @PathVariable("perusteId") final Long perusteId
    ) {
        return ResponseEntity.ok(sisalto.getVaiheet(perusteId));
    }

    @RequestMapping(value = "/vaiheet", method = POST)
    @ResponseBody
    public ResponseEntity<AIPEVaiheSuppeaDto> addVaihe(
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody AIPEVaiheDto vaiheDto
    ) {
        return ResponseEntity.ok(sisalto.addVaihe(perusteId, vaiheDto));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}", method = GET)
    public ResponseEntity<AIPEVaiheDto> getVaihe(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId,
            @RequestParam(required = false) final Integer rev
    ) {
        return ResponseEntity.ok(sisalto.getVaihe(perusteId, vaiheId, rev));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/versiot", method = GET)
    public ResponseEntity<List<Revision>> getVaiheVersio(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId
    ) {
        return ResponseEntity.ok(sisalto.getVaiheRevisions(perusteId, vaiheId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/palauta/{rev}", method = POST)
    public AIPEVaiheDto revertVaihe(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Integer rev
    ) {
        return sisalto.revertVaihe(perusteId, vaiheId, rev);
    }


    @RequestMapping(value = "/vaiheet/{vaiheId}", method = DELETE)
    public ResponseEntity removeVaihe(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId
    ) {
        sisalto.removeVaihe(perusteId, vaiheId);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}", method = PUT)
    @ResponseBody
    public ResponseEntity<AIPEVaiheDto> updateVaihe(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId,
            @RequestBody AIPEVaiheDto vaiheDto
    ) {
        return ResponseEntity.ok(sisalto.updateVaihe(perusteId, vaiheId, vaiheDto));
    }

    @RequestMapping(value = "/vaiheet", method = PUT)
    public ResponseEntity updateVaiheetJarjestys(
            @PathVariable final Long perusteId,
            @RequestBody List<AIPEVaiheBaseDto> jarjestys
    ) {
        sisalto.updateVaiheetJarjestys(perusteId, jarjestys);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/oppimaarat", method = PUT)
    public ResponseEntity updateOppimaaratJarjestys(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @RequestBody List<AIPEOppiaineBaseDto> jarjestys
    ) {
        sisalto.updateOppimaaratJarjestys(perusteId, vaiheId, oppiaineId, jarjestys);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit", method = PUT)
    public ResponseEntity updateKurssitJarjestys(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @RequestBody List<AIPEKurssiBaseDto> jarjestys
    ) {
        sisalto.updateKurssitJarjestys(perusteId, vaiheId, oppiaineId, jarjestys);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet", method = PUT)
    public ResponseEntity updateOppiaineetJarjestys(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @RequestBody List<AIPEOppiaineBaseDto> jarjestys
    ) {
        sisalto.updateOppiaineetJarjestys(perusteId, vaiheId, jarjestys);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/laajaalaiset", method = GET)
    public ResponseEntity<List<LaajaalainenOsaaminenDto>> getAipeOsaamiset(
            @PathVariable final Long perusteId
    ) {
        return ResponseEntity.ok(sisalto.getLaajaalaiset(perusteId));
    }

    @RequestMapping(value = "/laajaalaiset", method = POST)
    public ResponseEntity<LaajaalainenOsaaminenDto> addAipeOsaaminen(
            @PathVariable final Long perusteId,
            @RequestBody LaajaalainenOsaaminenDto loDto
    ) {
        return ResponseEntity.ok(sisalto.addLaajaalainen(perusteId, loDto));
    }

    @RequestMapping(value = "/laajaalaiset", method = PUT)
    public ResponseEntity updateLaajaalaisetJarjestys(
            @PathVariable final Long perusteId,
            @RequestBody List<LaajaalainenOsaaminenDto> jarjestys
    ) {
        sisalto.updateLaajaalainenOsaaminenJarjestys(perusteId, jarjestys);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/laajaalaiset/{laajaalainenId}", method = PUT)
    public ResponseEntity<LaajaalainenOsaaminenDto> updateAipeOsaaminen(
            @PathVariable final Long perusteId,
            @PathVariable final Long laajaalainenId,
            @RequestBody LaajaalainenOsaaminenDto loDto
    ) {
        return ResponseEntity.ok(sisalto.updateLaajaalainen(perusteId, laajaalainenId, loDto));
    }

    @RequestMapping(value = "/laajaalaiset/{laajalainenId}", method = GET)
    public ResponseEntity<LaajaalainenOsaaminenDto> getAipeOsaaminen(
            @PathVariable final Long perusteId,
            @PathVariable final Long laajalainenId
    ) {
        return ResponseEntity.ok(sisalto.getLaajaalainen(perusteId, laajalainenId));
    }

    @RequestMapping(value = "/laajaalaiset/{laajaalainenId}", method = DELETE)
    public ResponseEntity deleteAipeOsaaminen(
            @PathVariable final Long perusteId,
            @PathVariable final Long laajaalainenId
    ) {
        sisalto.removeLaajaalainen(perusteId, laajaalainenId);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/kohdealueet", method = GET)
    public ResponseEntity<List<OpetuksenKohdealueDto>> getAipeKohdealueet(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId
    ) {
        return ResponseEntity.ok(sisalto.getKohdealueet(perusteId, vaiheId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet", method = GET)
    public ResponseEntity<List<AIPEOppiaineSuppeaDto>> getAipeOppiaineet(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId
    ) {
        return ResponseEntity.ok(sisalto.getOppiaineet(perusteId, vaiheId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet", method = POST)
    public ResponseEntity<AIPEOppiaineSuppeaDto> addAipeOppiaine(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @RequestBody AIPEOppiaineDto oppiaineDto
    ) {
        return ResponseEntity.ok(sisalto.addOppiaine(perusteId, vaiheId, oppiaineDto));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}", method = PUT)
    public ResponseEntity<AIPEOppiaineSuppeaDto> updateAipeOppiaine(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @RequestBody AIPEOppiaineDto oppiaineDto
    ) {
        return ResponseEntity.ok(sisalto.updateOppiaine(perusteId, vaiheId, oppiaineId, oppiaineDto));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}", method = DELETE)
    public ResponseEntity removeAipeOppiaine(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId
    ) {
        sisalto.removeOppiaine(perusteId, vaiheId, oppiaineId);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}", method = GET)
    public ResponseEntity<AIPEOppiaineDto> getAipeOppiaine(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @RequestParam(required = false) final Integer rev
    ) {
        return ResponseEntity.ok(sisalto.getOppiaine(perusteId, vaiheId, oppiaineId, rev));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/versiot", method = GET)
    public ResponseEntity<List<Revision>> getAipeOppiaineVersio(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId,
            @PathVariable final Long oppiaineId
    ) {
        return ResponseEntity.ok(sisalto.getOppiaineRevisions(perusteId, vaiheId, oppiaineId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/palauta/{rev}", method = POST)
    public AIPEOppiaineDto revertAipeOppiaine(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @PathVariable final Integer rev
    ) {
        return sisalto.revertOppiaine(perusteId, vaiheId, oppiaineId, rev);
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/oppimaarat", method = GET)
    public ResponseEntity<List<AIPEOppiaineSuppeaDto>> getAipeOppimaarat(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId
    ) {
        return ResponseEntity.ok(sisalto.getOppimaarat(perusteId, vaiheId, oppiaineId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/oppimaarat", method = POST)
    public ResponseEntity<AIPEOppiaineDto> addAipeOppimaara(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @RequestBody AIPEOppiaineDto oppiaineDto
    ) {
        return ResponseEntity.ok(sisalto.addOppimaara(perusteId, vaiheId, oppiaineId, oppiaineDto));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit", method = GET)
    public ResponseEntity<List<AIPEKurssiSuppeaDto>> getAipeKurssit(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId
    ) {
        return ResponseEntity.ok(sisalto.getKurssit(perusteId, vaiheId, oppiaineId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit/{kurssiId}", method = GET)
    public ResponseEntity<AIPEKurssiDto> getAipeKurssi(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @PathVariable final Long kurssiId
    ) {
        return ResponseEntity.ok(sisalto.getKurssi(perusteId, vaiheId, oppiaineId, kurssiId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit", method = POST)
    public ResponseEntity<AIPEKurssiDto> addAipeKurssi(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @RequestBody AIPEKurssiDto kurssiDto
    ) {
        return ResponseEntity.ok(sisalto.addKurssi(perusteId, vaiheId, oppiaineId, kurssiDto));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit/{kurssiId}", method = PUT)
    public ResponseEntity<AIPEKurssiDto> updateAipeKurssi(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @PathVariable final Long kurssiId,
            @RequestBody AIPEKurssiDto kurssiDto
    ) {
        return ResponseEntity.ok(sisalto.updateKurssi(perusteId, vaiheId, oppiaineId, kurssiId, kurssiDto));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit/{kurssiId}", method = DELETE)
    public ResponseEntity removeAipeKurssi(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @PathVariable final Long kurssiId
    ) {
        sisalto.removeKurssi(perusteId, vaiheId, oppiaineId, kurssiId);
        return ResponseEntity.ok().build();
    }

}
