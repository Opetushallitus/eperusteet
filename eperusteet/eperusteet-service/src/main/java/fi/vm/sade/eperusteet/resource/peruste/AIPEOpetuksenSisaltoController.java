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
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.resource.util.CacheableResponse;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.yl.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private AIPEOpetuksenSisaltoService sisallot;

    @Autowired
    private OppiaineService oppiaineet;

//    @Autowired
//    private KurssiService kurssit;

//    @Autowired
//    private AihekokonaisuudetService aihekokonaisuudet;

//    @Autowired
//    private PerusteenOsaViiteService viittet;

    @Autowired
    private PerusteService perusteet;

//    @Autowired
//    private KayttajanTietoService kayttajanTietoService;

    @RequestMapping(value = "/oppiaineet", method = GET)
    public ResponseEntity<List<OppiaineSuppeaDto>> getOppiaineet(
            @PathVariable("perusteId") final Long perusteId) {
        return handleGet(perusteId, () -> sisallot.getOppiaineet(perusteId, OppiaineSuppeaDto.class));
    }

    @RequestMapping(value = "/oppiaineet", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public OppiaineDto addOppiaine(@PathVariable("perusteId") final Long perusteId,
                                   @RequestBody OppiaineDto dto) {
        return oppiaineet.addOppiaine(perusteId, dto, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS);
    }

//    @RequestMapping(value = "/kurssit", method = POST)
//    @ResponseStatus(HttpStatus.CREATED)
//    public RedirectView addKurssi(
//            @PathVariable("perusteId") final Long perusteId,
//            @RequestBody LukioKurssiLuontiDto kurssi) {
//        return new RedirectView("kurssit/" + kurssit.createLukiokurssi(perusteId, kurssi), true);
//    }
//
//    @RequestMapping(value = "/kurssit", method = GET)
//    public ResponseEntity<List<LukiokurssiListausDto>> listKurssit(
//            @PathVariable("perusteId") final Long perusteId) {
//        return handleGet(perusteId, () -> kurssit.findLukiokurssitByPerusteId(perusteId));
//    }
//
//    @RequestMapping(value = "/kurssit/{kurssiId}/versiot", method = GET)
//    public ResponseEntity<List<CombinedDto<Revision, HenkiloTietoDto>>> listKurssiVersiot(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("kurssiId") final Long kurssiId) {
//        return handleGet(perusteId, () -> withHenkilos(kurssit.listKurssiVersions(perusteId, kurssiId)));
//    }
//
//    @RequestMapping(value = "/kurssit/{id}", method = GET)
//    public ResponseEntity<LukiokurssiTarkasteleDto> getKurssi(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") Long id) {
//        return handleGet(perusteId, () -> kurssit.getLukiokurssiTarkasteleDtoById(perusteId, id));
//    }
//
//    @RequestMapping(value = "/kurssit/{id}/versiot/{version}", method = GET)
//    public ResponseEntity<LukiokurssiTarkasteleDto> getKurssiByRevision(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") Long id,
//            @PathVariable("version") Integer version) {
//        return handleGet(perusteId, () -> kurssit.getLukiokurssiTarkasteleDtoByIdAndVersion(perusteId, id, version));
//    }
//
//    @RequestMapping(value = "/kurssit/{id}/versiot/{version}/palauta", method = POST)
//    public LukiokurssiTarkasteleDto revertKurssi(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") Long id,
//            @PathVariable("version") Integer version) {
//        return kurssit.revertLukiokurssiTarkasteleDtoByIdAndVersion(perusteId, id, version);
//    }
//
//    @RequestMapping(value = "/kurssit/{id}", method = DELETE)
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void deleteKurssi(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") Long id) {
//        kurssit.deleteLukiokurssi(perusteId, id);
//    }
//
//    @RequestMapping(value = "/kurssit/{id}", method = POST)
//    public RedirectView updateKurssi(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") final Long kurssiId,
//            @RequestBody LukiokurssiMuokkausDto kurssi) {
//        assertKurssiId(kurssiId, kurssi);
//        kurssit.updateLukiokurssi(perusteId, kurssi);
//        return new RedirectView("" + kurssiId, true);
//    }
//
//    @RequestMapping(value = "/kurssit/{id}/oppiaineet", method = POST)
//    public RedirectView updateKurssiOppiaineRelations(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") final Long kurssiId,
//            @RequestBody LukiokurssiOppaineMuokkausDto kurssi) {
//        assertKurssiId(kurssiId, kurssi);
//        kurssit.updateLukiokurssiOppiaineRelations(perusteId, kurssi);
//        return new RedirectView("", true);
//    }
//
//    private void assertKurssiId(Long kurssiId, IdHolder kurssi) {
//        if (kurssi.getId() == null) {
//            kurssi.setId(kurssiId);
//        } else if (!kurssi.getId().equals(kurssiId)) {
//            throw new NotExistsException("Kurssia ei löytynyt");
//        }
//    }
//
//    @RequestMapping(value = "/oppiaineet/{id}", method = GET)
//    public ResponseEntity<OppiaineDto> getOppiaine(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") final Long id) {
//        return handleGet(perusteId, () -> oppiaineet.getOppiaine(perusteId, id, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS));
//    }
//
//    @RequestMapping(value = "/oppiaineet/{id}/kurssit", method = GET)
//    public ResponseEntity<List<LukiokurssiListausDto>> getOppiaineKurssit(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") final Long id) {
//        return handleGet(perusteId, () -> kurssit.findLukiokurssitByOppiaineId(perusteId, id));
//    }
//
//    @RequestMapping(value = "/oppiaineet/{id}/versiot", method = GET)
//    @CacheControl(age = CacheControl.ONE_YEAR)
//    public List<CombinedDto<Revision, HenkiloTietoDto>> getOpppiaineVersions(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") final Long id) {
//        return withHenkilos(oppiaineet.getOppiaineRevisions(perusteId, id, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS));
//    }
//
//    @RequestMapping(value = "/oppiaineet/{id}/versiot/{revision}", method = GET)
//    @CacheControl(age = CacheControl.ONE_YEAR)
//    public OppiaineDto getOppiaineByRevision(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") final Long id,
//            @PathVariable("revision") final Integer revision) {
//        return oppiaineet.getOppiaine(perusteId, id, revision, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS);
//    }
//
//    @RequestMapping(value = "/oppiaineet/{id}/versiot/{revisio}/palauta", method = POST)
//    @CacheControl(age = CacheControl.ONE_YEAR)
//    public OppiaineDto revertOppiaine(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") final Long id,
//            @PathVariable("revisio") final Integer revisio) {
//        return oppiaineet.revertOppiaine(perusteId, id, revisio, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS);
//    }
//
//    @RequestMapping(value = "/oppiaineet/{id}/oppimaarat", method = GET)
//    public ResponseEntity<List<OppiaineSuppeaDto>> getOppimaarat(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") final Long id) {
//        return handleGet(perusteId, () -> oppiaineet.getOppimaarat(perusteId, id, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS));
//    }
//
//    @RequestMapping(value = "/oppiaineet/{id}", method = POST)
//    public OppiaineDto updateOppiaine(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") final Long id,
//            @RequestBody UpdateDto<LukioOppiaineUpdateDto> dto) {
//        dto.getDto().setId(id);
//        return oppiaineet.updateOppiaine(perusteId, dto, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS);
//    }
//
//    @RequestMapping(value = "/oppiaineet/{id}", method = DELETE)
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void deleteOppiaine(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") final Long id) {
//        oppiaineet.deleteOppiaine(perusteId, id, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS);
//    }
//
//    @RequestMapping(value = "/rakenne", method = POST)
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void updateStructure(
//            @PathVariable("perusteId") final Long perusteId,
//            @RequestBody OppaineKurssiTreeStructureDto structureDto) {
//        kurssit.updateTreeStructure(perusteId, structureDto, null);
//    }
//
//    @RequestMapping(value = "/oppiaineet/{id}/kohdealueet", method = GET)
//    public Set<OpetuksenKohdealueDto> getKohdealueet(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") final Long id) {
//        return oppiaineet.getOppiaine(perusteId, id, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS).getKohdealueet();
//    }
//
//    @RequestMapping(value = "/sisalto/{id}", method = DELETE)
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void deleteSisalto(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") final Long id) {
//        sisallot.removeSisalto(perusteId, id);
//    }
//
//    @RequestMapping(value = "/sisalto", method = POST)
//    @ResponseStatus(HttpStatus.CREATED)
//    public PerusteenOsaViiteDto.Matala addSisalto(
//            @PathVariable("perusteId") final Long perusteId,
//            @RequestBody(required = false) PerusteenOsaViiteDto.Matala dto) {
//        if (dto == null || (dto.getPerusteenOsa() == null && dto.getPerusteenOsaRef() == null)) {
//            return sisallot.addSisalto(perusteId, null, null);
//        } else {
//            return sisallot.addSisalto(perusteId, null, dto);
//        }
//    }
//
//    @RequestMapping(value = "/sisalto/{id}/lapset", method = POST)
//    public PerusteenOsaViiteDto.Matala addSisalto(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") final Long id,
//            @RequestBody(required = false) PerusteenOsaViiteDto.Matala dto) {
//        return sisallot.addSisalto(perusteId, id, dto);
//    }
//
//    @RequestMapping(value = "/sisalto/{id}", method = POST)
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void updateSisaltoViite(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") final Long id,
//            @RequestBody final fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Suppea pov) {
//        viittet.reorderSubTree(perusteId, id, pov);
//    }
//
//    @RequestMapping(value = "/sisalto/{id}/muokattavakopio", method = POST)
//    public PerusteenOsaViiteDto.Laaja kloonaaTekstiKappale(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("id") final Long id) {
//        return viittet.kloonaaTekstiKappale(perusteId, id);
//    }

//    protected List<CombinedDto<Revision, HenkiloTietoDto>> withHenkilos(List<Revision> revisions) {
//        return revisions.stream().map(r
//                -> new CombinedDto<>(r, kayttajanTiedot(r.getMuokkaajaOid())))
//                .collect(toList());
//    }

//    private HenkiloTietoDto kayttajanTiedot(String muokkaajaOid) {
//        try {
//            return new HenkiloTietoDto(kayttajanTietoService.hae(muokkaajaOid));
//        } catch (Exception e) {
//            if (e.getMessage() != null && e.getMessage().contains("Host is down")) {
//                // Fail silently.
//                logger.warn("Käyttäjätietojen haku versiotietoihin käyttäjälle {} epäonnistui. " +
//                        "Palvelu alhaalla. Virhe: {}", muokkaajaOid, e.getMessage());
//                return null;
//            }
//            throw e;
//        }
//    }

    private <T> ResponseEntity<T> handleGet(Long perusteId, Supplier<T> response) {
        return CacheableResponse.create(perusteet.getPerusteVersion(perusteId), 1, response);
    }
}
