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

import fi.vm.sade.eperusteet.dto.yl.*;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.audit.EperusteetAudit;
import static fi.vm.sade.eperusteet.service.audit.EperusteetMessageFields.*;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.*;
import fi.vm.sade.eperusteet.service.audit.LogMessage;
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
    private EperusteetAudit audit;

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
        return audit.withAudit(LogMessage.builder(perusteId, VAIHE, LISAYS),
                (Void) -> ResponseEntity.ok(sisalto.addVaihe(perusteId, vaiheDto)));
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


    @RequestMapping(value = "/vaiheet/{vaiheId}", method = DELETE)
    public ResponseEntity removeVaihe(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId
    ) {
        audit.withAudit(LogMessage.builder(perusteId, VAIHE, POISTO), (Void) -> {
            sisalto.removeVaihe(perusteId, vaiheId);
            return null;
        });
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}", method = PUT)
    @ResponseBody
    public ResponseEntity<AIPEVaiheDto> updateVaihe(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("vaiheId") final Long vaiheId,
            @RequestBody AIPEVaiheDto vaiheDto
    ) {
        return audit.withAudit(LogMessage.builder(perusteId, VAIHE, MUOKKAUS),
                (Void) -> ResponseEntity.ok(sisalto.updateVaihe(perusteId, vaiheId, vaiheDto)));
    }

    @RequestMapping(value = "/vaiheet", method = PUT)
    public ResponseEntity updateVaiheetJarjestys(
            @PathVariable final Long perusteId,
            @RequestBody List<AIPEVaiheBaseDto> jarjestys
    ) {
        audit.withAudit(LogMessage.builder(perusteId, VAIHE, JARJESTA),
                (Void) -> {
                    sisalto.updateVaiheetJarjestys(perusteId, jarjestys);
                    return null;
                });
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/oppimaarat", method = PUT)
    public ResponseEntity updateOppimaaratJarjestys(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @RequestBody List<AIPEOppiaineBaseDto> jarjestys
    ) {
        audit.withAudit(LogMessage.builder(perusteId, KURSSI, JARJESTA),
                (Void) -> {
                    sisalto.updateOppimaaratJarjestys(perusteId, vaiheId, oppiaineId, jarjestys);
                    return null;
                });
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit", method = PUT)
    public ResponseEntity updateKurssitJarjestys(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @RequestBody List<AIPEKurssiBaseDto> jarjestys
    ) {
        audit.withAudit(LogMessage.builder(perusteId, KURSSI, JARJESTA),
                (Void) -> {
                    sisalto.updateKurssitJarjestys(perusteId, vaiheId, oppiaineId, jarjestys);
                    return null;
                });
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet", method = PUT)
    public ResponseEntity updateOppiaineetJarjestys(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @RequestBody List<AIPEOppiaineBaseDto> jarjestys
    ) {
        audit.withAudit(LogMessage.builder(perusteId, OPPIAINE, JARJESTA),
                (Void) -> {
                    sisalto.updateOppiaineetJarjestys(perusteId, vaiheId, jarjestys);
                    return null;
                });
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/laajaalaiset", method = GET)
    public ResponseEntity<List<LaajaalainenOsaaminenDto>> getOsaamiset(
            @PathVariable final Long perusteId
    ) {
        return ResponseEntity.ok(sisalto.getLaajaalaiset(perusteId));
    }

    @RequestMapping(value = "/laajaalaiset", method = POST)
    public ResponseEntity<LaajaalainenOsaaminenDto> addOsaaminen(
            @PathVariable final Long perusteId,
            @RequestBody LaajaalainenOsaaminenDto loDto
    ) {
        return audit.withAudit(LogMessage.builder(perusteId, LAAJAALAINENOSAAMINEN, LISAYS),
                (Void) -> ResponseEntity.ok(sisalto.addLaajaalainen(perusteId, loDto)));
    }

    @RequestMapping(value = "/laajaalaiset", method = PUT)
    public ResponseEntity updateLaajaalaisetJarjestys(
            @PathVariable final Long perusteId,
            @RequestBody List<LaajaalainenOsaaminenDto> jarjestys
    ) {
        audit.withAudit(LogMessage.builder(perusteId, LAAJAALAINENOSAAMINEN, JARJESTA),
                (Void) -> {
                    sisalto.updateLaajaalainenOsaaminenJarjestys(perusteId, jarjestys);
                    return null;
                });
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/laajaalaiset/{laajaalainenId}", method = PUT)
    public ResponseEntity<LaajaalainenOsaaminenDto> updateOsaaminen(
            @PathVariable final Long perusteId,
            @PathVariable final Long laajaalainenId,
            @RequestBody LaajaalainenOsaaminenDto loDto
    ) {
        return audit.withAudit(LogMessage.builder(perusteId, LAAJAALAINENOSAAMINEN, MUOKKAUS),
                (Void) -> ResponseEntity.ok(sisalto.updateLaajaalainen(perusteId, laajaalainenId, loDto)));
    }

    @RequestMapping(value = "/laajaalaiset/{laajalainenId}", method = GET)
    public ResponseEntity<LaajaalainenOsaaminenDto> getOsaaminen(
            @PathVariable final Long perusteId,
            @PathVariable final Long laajalainenId
    ) {
        return ResponseEntity.ok(sisalto.getLaajaalainen(perusteId, laajalainenId));
    }

    @RequestMapping(value = "/laajaalaiset/{laajaalainenId}", method = DELETE)
    public ResponseEntity deleteOsaaminen(
            @PathVariable final Long perusteId,
            @PathVariable final Long laajaalainenId
    ) {
        audit.withAudit(LogMessage.builder(perusteId, LAAJAALAINENOSAAMINEN, POISTO), (Void) -> {
            sisalto.removeLaajaalainen(perusteId, laajaalainenId);
            return null;
        });
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/kohdealueet", method = GET)
    public ResponseEntity<List<OpetuksenKohdealueDto>> getKohdealueet(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId
    ) {
        return ResponseEntity.ok(sisalto.getKohdealueet(perusteId, vaiheId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet", method = GET)
    public ResponseEntity<List<AIPEOppiaineSuppeaDto>> getOppiaineet(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId
    ) {
        return ResponseEntity.ok(sisalto.getOppiaineet(perusteId, vaiheId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet", method = POST)
    public ResponseEntity<AIPEOppiaineSuppeaDto> addOppiaine(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @RequestBody AIPEOppiaineDto oppiaineDto
    ) {
        return audit.withAudit(LogMessage.builder(perusteId, VAIHE, LISAYS),
                (Void) -> ResponseEntity.ok(sisalto.addOppiaine(perusteId, vaiheId, oppiaineDto)));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}", method = PUT)
    public ResponseEntity<AIPEOppiaineSuppeaDto> updateOppiaine(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @RequestBody AIPEOppiaineDto oppiaineDto
    ) {
        return audit.withAudit(LogMessage.builder(perusteId, VAIHE, MUOKKAUS),
                (Void) -> ResponseEntity.ok(sisalto.updateOppiaine(perusteId, vaiheId, oppiaineId, oppiaineDto)));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}", method = DELETE)
    public ResponseEntity removeOppiaine(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId
    ) {
        audit.withAudit(LogMessage.builder(perusteId, VAIHE, POISTO), (Void) -> {
            sisalto.removeOppiaine(perusteId, vaiheId, oppiaineId);
            return null;
        });
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}", method = GET)
    public ResponseEntity<AIPEOppiaineDto> getOppiaine(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId
    ) {
        return ResponseEntity.ok(sisalto.getOppiaine(perusteId, vaiheId, oppiaineId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/oppimaarat", method = GET)
    public ResponseEntity<List<AIPEOppiaineSuppeaDto>> getOppimaarat(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId
    ) {
        return ResponseEntity.ok(sisalto.getOppimaarat(perusteId, vaiheId, oppiaineId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/oppimaarat", method = POST)
    public ResponseEntity<AIPEOppiaineDto> addOppimaara(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @RequestBody AIPEOppiaineDto oppiaineDto
    ) {
        return audit.withAudit(LogMessage.builder(perusteId, OPPIMAARA, LISAYS),
                (Void) -> ResponseEntity.ok(sisalto.addOppimaara(perusteId, vaiheId, oppiaineId, oppiaineDto)));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit", method = GET)
    public ResponseEntity<List<AIPEKurssiSuppeaDto>> getKurssit(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId
    ) {
        return ResponseEntity.ok(sisalto.getKurssit(perusteId, vaiheId, oppiaineId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit/{kurssiId}", method = GET)
    public ResponseEntity<AIPEKurssiDto> getKurssit(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @PathVariable final Long kurssiId
    ) {
        return ResponseEntity.ok(sisalto.getKurssi(perusteId, vaiheId, oppiaineId, kurssiId));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit", method = POST)
    public ResponseEntity<AIPEKurssiDto> addKurssi(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @RequestBody AIPEKurssiDto kurssiDto
    ) {
        return audit.withAudit(LogMessage.builder(perusteId, KURSSI, LISAYS),
                (Void) -> ResponseEntity.ok(sisalto.addKurssi(perusteId, vaiheId, oppiaineId, kurssiDto)));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit/{kurssiId}", method = PUT)
    public ResponseEntity<AIPEKurssiDto> updateKurssi(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @PathVariable final Long kurssiId,
            @RequestBody AIPEKurssiDto kurssiDto
    ) {
        return audit.withAudit(LogMessage.builder(perusteId, KURSSI, MUOKKAUS),
                (Void) -> ResponseEntity.ok(sisalto.updateKurssi(perusteId, vaiheId, oppiaineId, kurssiId, kurssiDto)));
    }

    @RequestMapping(value = "/vaiheet/{vaiheId}/oppiaineet/{oppiaineId}/kurssit/{kurssiId}", method = DELETE)
    public ResponseEntity removeKurssi(
            @PathVariable final Long perusteId,
            @PathVariable final Long vaiheId,
            @PathVariable final Long oppiaineId,
            @PathVariable final Long kurssiId
    ) {
        audit.withAudit(LogMessage.builder(perusteId, KURSSI, POISTO), (Void) -> {
            sisalto.removeKurssi(perusteId, vaiheId, oppiaineId, kurssiId);
            return null;
        });
        return ResponseEntity.ok().build();
    }

}
