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
import fi.vm.sade.eperusteet.dto.IdHolder;
import fi.vm.sade.eperusteet.dto.kayttaja.HenkiloTietoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.OpetuksenKohdealueDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.*;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.resource.util.CacheControl;
import fi.vm.sade.eperusteet.resource.util.CacheableResponse;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.yl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * User: tommiratamaa
 * Date: 21.9.15
 * Time: 13.32
 */
@RestController
@RequestMapping("/perusteet/{perusteId}/lukiokoulutus")
@InternalApi
public class LukiokoulutuksenPerusteenSisaltoController {

    @Autowired
    private LukiokoulutuksenPerusteenSisaltoService sisallot;

    @Autowired
    private OppiaineService oppiaineet;

    @Autowired
    private KurssiService kurssit;

    @Autowired
    private AihekokonaisuudetService aihekokonaisuudet;

    @Autowired
    private PerusteenOsaViiteService viittet;

    @Autowired
    private PerusteService perusteet;

    @Autowired
    private AihekokonaisuudetService aihekokonaisuudetService;

    @Autowired
    private KayttajanTietoService kayttajanTietoService;

    @RequestMapping(value = "/oppiaineet", method = GET)
    public ResponseEntity<List<OppiaineSuppeaDto>> getOppiaineet(@PathVariable("perusteId") final Long perusteId) {
        return handleGet(perusteId, () -> sisallot.getOppiaineet(perusteId, OppiaineSuppeaDto.class));
    }

    @RequestMapping(value = "/oppiaineet", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public OppiaineDto addOppiaine(
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody OppiaineDto dto) {
        return oppiaineet.addOppiaine(perusteId, dto, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS);
    }

    @RequestMapping(value = "/rakenne/{rakenneId}/versiot/{revision}/kurssit", method = GET)
    public ResponseEntity<List<LukiokurssiListausDto>> listKurssitInRakenneVersio(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("rakenneId") final Long rakenneId,
            @PathVariable("revision") final Integer revision) {
        return handleGet(perusteId, () -> kurssit.findLukiokurssitByRakenneRevision(perusteId, rakenneId, revision));
    }

    @RequestMapping(value = "/kurssit", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public RedirectView addKurssi(@PathVariable("perusteId") final Long perusteId,
            @RequestBody LukioKurssiLuontiDto kurssi) {
        return new RedirectView("kurssit/"+kurssit.createLukiokurssi(perusteId, kurssi), true);
    }

    @RequestMapping(value = "/kurssit", method = GET)
    public ResponseEntity<List<LukiokurssiListausDto>> listKurssit(@PathVariable("perusteId") final Long perusteId) {
        return handleGet(perusteId, () -> kurssit.findLukiokurssitByPerusteId(perusteId));
    }

    @RequestMapping(value = "/kurssit/{kurssiId}/versiot", method = GET)
    public ResponseEntity<List<CombinedDto<Revision, HenkiloTietoDto>>> listKurssiVersiot(@PathVariable("perusteId") final Long perusteId,
            @PathVariable("kurssiId") final Long kurssiId) {
        return handleGet(perusteId, () -> withHenkilos(kurssit.listKurssiVersions(perusteId, kurssiId)));
    }

    @RequestMapping(value = "/kurssit/{id}", method = GET)
    public ResponseEntity<LukiokurssiTarkasteleDto> getKurssi(@PathVariable("perusteId") final Long perusteId,
                                  @PathVariable("id") Long id) {
        return handleGet(perusteId, () -> kurssit.getLukiokurssiTarkasteleDtoById(perusteId, id));
    }

    @RequestMapping(value = "/kurssit/{id}/versiot/{version}", method = GET)
    public ResponseEntity<LukiokurssiTarkasteleDto> getKurssiByRevision(@PathVariable("perusteId") final Long perusteId,
                                                              @PathVariable("id") Long id,
                                                              @PathVariable("version") Integer version) {
        return handleGet(perusteId, () -> kurssit.getLukiokurssiTarkasteleDtoByIdAndVersion(perusteId, id, version));
    }

    @RequestMapping(value = "/kurssit/{id}/versiot/{version}/palauta", method = POST)
    public LukiokurssiTarkasteleDto revertKurssi(@PathVariable("perusteId") final Long perusteId,
                                                              @PathVariable("id") Long id,
                                                              @PathVariable("version") Integer version) {
        return kurssit.revertLukiokurssiTarkasteleDtoByIdAndVersion(perusteId, id, version);
    }

    @RequestMapping(value = "/kurssit/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteKurssi(@PathVariable("perusteId") final Long perusteId,
                                                              @PathVariable("id") Long id) {
        kurssit.deleteLukiokurssi(perusteId, id);
    }

    @RequestMapping(value = "/kurssit/{id}", method = POST)
    public RedirectView updateKurssi(@PathVariable("perusteId") final Long perusteId,
                                     @PathVariable("id") final Long kurssiId,
                                  @RequestBody LukiokurssiMuokkausDto kurssi) {
        assertKurssiId(kurssiId, kurssi);
        kurssit.updateLukiokurssi(perusteId, kurssi);
        return new RedirectView(""+kurssiId,true);
    }

    @RequestMapping(value = "/kurssit/{id}/oppiaineet", method = POST)
    public RedirectView updateKurssiOppiaineRelations(@PathVariable("perusteId") final Long perusteId,
                                     @PathVariable("id") final Long kurssiId,
                                     @RequestBody LukiokurssiOppaineMuokkausDto kurssi) {
        assertKurssiId(kurssiId, kurssi);
        kurssit.updateLukiokurssiOppiaineRelations(perusteId, kurssi);
        return new RedirectView("",true);
    }

    private void assertKurssiId(Long kurssiId, IdHolder kurssi) {
        if (kurssi.getId() == null) {
            kurssi.setId(kurssiId);
        } else if(!kurssi.getId().equals(kurssiId)) {
            throw new NotExistsException();
        }
    }

    @RequestMapping(value = "/oppiaineet/{id}", method = GET)
    public ResponseEntity<OppiaineDto> getOppiaine(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id) {
        return handleGet(perusteId, () -> oppiaineet.getOppiaine(perusteId, id, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS));
    }

    @RequestMapping(value = "/oppiaineet/{id}/kurssit", method = GET)
    public ResponseEntity<List<LukiokurssiListausDto>> getOppiaineKurssit(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id) {
        return handleGet(perusteId, () -> kurssit.findLukiokurssitByOppiaineId(perusteId, id));
    }

    @RequestMapping(value = "/oppiaineet/{id}/versiot/{revisio}", method = GET)
    @CacheControl(age = CacheControl.ONE_YEAR)
    public OppiaineDto getOppiaine(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id,
            @PathVariable("revisio") final Integer revisio) {
        return oppiaineet.getOppiaine(perusteId, id, revisio, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS);
    }

    @RequestMapping(value = "/oppiaineet/{id}/oppimaarat", method = GET)
    public ResponseEntity<List<OppiaineSuppeaDto>> getOppimaarat(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id) {
        return handleGet(perusteId, () -> oppiaineet.getOppimaarat(perusteId, id, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS));
    }

    @RequestMapping(value = "/oppiaineet/{id}", method = POST)
    public OppiaineDto updateOppiaine(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id,
            @RequestBody UpdateDto<OppiaineDto> dto) {
        dto.getDto().setId(id);
        return oppiaineet.updateOppiaine(perusteId, dto, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS);
    }

    @RequestMapping(value = "/oppiaineet/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOppiaine(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id) {
        oppiaineet.deleteOppiaine(perusteId, id, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS);
    }

    @RequestMapping(value = "/rakenne", method = POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStructure(@PathVariable("perusteId") final Long perusteId,
                                @RequestBody OppaineKurssiTreeStructureDto structureDto) {
        kurssit.updateTreeStructure(perusteId, structureDto);
    }

    @RequestMapping(value = "/oppiaineet/{id}/kohdealueet", method = GET)
    public Set<OpetuksenKohdealueDto> getKohdealueet(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id) {
        return oppiaineet.getOppiaine(perusteId, id, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS).getKohdealueet();
    }

    @RequestMapping(value = "/sisalto/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSisalto(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id) {
        sisallot.removeSisalto(perusteId, id);
    }

    @RequestMapping(value = "/sisalto", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public PerusteenOsaViiteDto.Matala addSisalto(
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody(required = false) PerusteenOsaViiteDto.Matala dto) {
        if (dto == null || (dto.getPerusteenOsa() == null && dto.getPerusteenOsaRef() == null)) {
            return sisallot.addSisalto(perusteId, null, null);
        } else {
            return sisallot.addSisalto(perusteId, null, dto);
        }
    }

    @RequestMapping(value = "/sisalto/{id}/lapset", method = POST)
    public PerusteenOsaViiteDto.Matala addSisalto(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id,
            @RequestBody(required = false) PerusteenOsaViiteDto.Matala dto) {
        PerusteenOsaViiteDto.Matala uusiSisalto = sisallot.addSisalto(perusteId, id, dto);
        return uusiSisalto;
    }

    @RequestMapping(value = "/sisalto/{id}", method = POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSisaltoViite(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id,
            @RequestBody final fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Suppea pov) {
        viittet.reorderSubTree(perusteId, id, pov);
    }

    @RequestMapping(value = "/sisalto/{id}/muokattavakopio", method = POST)
    public PerusteenOsaViiteDto.Laaja kloonaaTekstiKappale(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id) {
        return viittet.kloonaaTekstiKappale(perusteId, id);
    }


    @RequestMapping(value = "/aihekokonaisuudet", method = GET)
    public ResponseEntity<List<AihekokonaisuusListausDto>> getAihekokonaisuudet(
            @PathVariable("perusteId") final Long perusteId) {
        return handleGet(perusteId, new Supplier<List<AihekokonaisuusListausDto>>() {
            @Override
            public List<AihekokonaisuusListausDto> get() {
                return aihekokonaisuudet.getAihekokonaisuudet(perusteId);
            }
        });
    }

    @RequestMapping(value = "/aihekokonaisuudet/yleiskuvaus", method = GET)
    public ResponseEntity<AihekokonaisuudetYleiskuvausDto> getAihekokonaisuudetYleiskuvaus(
            @PathVariable("perusteId") final Long perusteId) {
        return handleGet(perusteId, new Supplier<AihekokonaisuudetYleiskuvausDto>() {
            @Override
            public AihekokonaisuudetYleiskuvausDto get() {
                return aihekokonaisuudet.getAihekokonaisuudetYleiskuvaus(perusteId);
            }
        });
    }

    @RequestMapping(value = "/aihekokonaisuudet/yleiskuvaus", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public RedirectView updateAihekokonaisuudetYleiskuvaus(
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody AihekokonaisuudetYleiskuvausDto AihekokonaisuudetYleiskuvausDto) {
        aihekokonaisuudet.tallennaYleiskuvaus(perusteId, AihekokonaisuudetYleiskuvausDto);
        return new RedirectView("yleiskuvaus", true);
    }

    @RequestMapping(value = "/aihekokonaisuudet/yleiskuvaus/versiot", method = GET)
    public List<CombinedDto<Revision, HenkiloTietoDto>> getAihekokonaisuudetYleiskuvausVersiot(
            @PathVariable("perusteId") final Long perusteId) {
        return withHenkilos(aihekokonaisuudet.getAihekokonaisuudetYleiskuvausVersiot(perusteId));
    }

    protected List<CombinedDto<Revision, HenkiloTietoDto>> withHenkilos(List<Revision> revisions) {
        return revisions.stream().map(r
                    -> new CombinedDto<>(r, new HenkiloTietoDto(kayttajanTietoService.hae(r.getMuokkaajaOid()))))
                .collect(toList());
    }

    @RequestMapping(value = "/aihekokonaisuudet/yleiskuvaus/versio/{revisio}", method = GET)
    public ResponseEntity<AihekokonaisuudetYleiskuvausDto> getAihekokonaisuudetYleiskuvausByVersio(
            @PathVariable("perusteId") final long perusteId,
            @PathVariable("revisio") final int revisio) {
        return handleGet(perusteId, new Supplier<AihekokonaisuudetYleiskuvausDto>() {
            @Override
            public AihekokonaisuudetYleiskuvausDto get() {
                return aihekokonaisuudet.getAihekokonaisuudetYleiskuvausByVersion(perusteId, revisio);
            }
        });
    }

    @RequestMapping(value = "/aihekokonaisuudet/yleiskuvaus/palauta/{revisio}", method = POST)
    public ResponseEntity<AihekokonaisuudetYleiskuvausDto> palautaAihekokonaisuudetYleiskuvaus(
            @PathVariable("perusteId") final long perusteId,
            @PathVariable("revisio") final int revisio) {
        AihekokonaisuudetYleiskuvausDto dto = aihekokonaisuudet.palautaAihekokonaisuudetYleiskuvaus(perusteId, revisio);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/aihekokonaisuudet/{id}", method = GET)
    public ResponseEntity<LukioAihekokonaisuusMuokkausDto> getAihekokonaisuus(@PathVariable("perusteId") final Long perusteId,
                                                                              @PathVariable("id") final Long id) {
        return handleGet(perusteId, () -> aihekokonaisuudet.getLukioAihekokobaisuusMuokkausById(perusteId, id));
    }

    @RequestMapping(value = "/aihekokonaisuudet/{id}", method = POST)
    public RedirectView updateAihekokonaisuus(@PathVariable("perusteId") final Long perusteId,
                                     @PathVariable("id") final Long aihekokonaisuusId,
                                     @RequestBody LukioAihekokonaisuusMuokkausDto aihekokonaisuus) {
        if(!aihekokonaisuus.getId().equals(aihekokonaisuusId)) {
            throw new NotExistsException();
        }
        aihekokonaisuudet.muokkaaAihekokonaisuutta(perusteId, aihekokonaisuus);
        return new RedirectView(""+aihekokonaisuusId,true);
    }

    @RequestMapping(value = "/aihekokonaisuudet/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAihekokonaisuus(@PathVariable("perusteId") final Long perusteId,
                                      @PathVariable("id") final Long aihekokonaisuusId) {
        aihekokonaisuudet.poistaAihekokonaisuus(perusteId, aihekokonaisuusId);
    }

    @RequestMapping(value = "/aihekokonaisuudet/aihekokonaisuus", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public RedirectView addAihekokonaisuus(@PathVariable("perusteId") final Long perusteId,
                                  @RequestBody LukioAihekokonaisuusLuontiDto aihekokonaisuusLuontiDto) {
        return new RedirectView(""+aihekokonaisuudet.luoAihekokonaisuus(perusteId, aihekokonaisuusLuontiDto), true);
    }

    @RequestMapping(value = "/aihekokonaisuudet/{id}/versiot", method = GET)
    public List<CombinedDto<Revision, HenkiloTietoDto>> getAihekokonaisuusVersiot(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long aihekokonaisuusId) {
        List<Revision> revisiot = aihekokonaisuudet.getAihekokonaisuusVersiot(perusteId, aihekokonaisuusId);
        List<CombinedDto<Revision, HenkiloTietoDto>> versiot = new ArrayList<>();
        for (Revision r : revisiot) {
            versiot.add(new CombinedDto<>(r, new HenkiloTietoDto(kayttajanTietoService.hae(r.getMuokkaajaOid()))));
        }
        return versiot;
    }

    @RequestMapping(value = "/aihekokonaisuudet/{id}/versio/{revisio}", method = GET)
    public ResponseEntity<LukioAihekokonaisuusMuokkausDto> getAihekokonaisuusByVersion(@PathVariable("perusteId") final Long perusteId,
                                                                                       @PathVariable("id") final Long id,
                                                                                       @PathVariable("revisio") final int revisio) {
        return handleGet(perusteId, () -> aihekokonaisuudet.getAihekokonaisuusByVersion(perusteId, id, revisio));
    }

    @RequestMapping(value = "/aihekokonaisuudet/{id}/palauta/{revisio}", method = POST)
    public ResponseEntity<LukioAihekokonaisuusMuokkausDto> palautaAihekokonaisuudetYleiskuvaus(
            @PathVariable("perusteId") final long perusteId,
            @PathVariable("id") final Long id,
            @PathVariable("revisio") final int revisio) {

        LukioAihekokonaisuusMuokkausDto dto = aihekokonaisuudet.palautaAihekokonaisuus (perusteId, id, revisio);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/yleisettavoitteet", method = GET)
    public ResponseEntity<LukiokoulutuksenYleisetTavoitteetDto> getYleisetTavoitteet(
            @PathVariable("perusteId") final Long perusteId) {
        return handleGet(perusteId, new Supplier<LukiokoulutuksenYleisetTavoitteetDto>() {
            @Override
            public LukiokoulutuksenYleisetTavoitteetDto get() {
                return perusteet.getYleisetTavoitteet(perusteId);
            }
        });
    }

    @RequestMapping(value = "/yleisettavoitteet", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public RedirectView updateYleisetTavoitteet(
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody LukiokoulutuksenYleisetTavoitteetDto lukiokoulutuksenYleisetTavoitteetDto) {
        perusteet.tallennaYleisetTavoitteet(perusteId, lukiokoulutuksenYleisetTavoitteetDto);
        return new RedirectView("yleisettavoitteet", true);
    }

    @RequestMapping(value = "/yleisettavoitteet/versiot", method = GET)
    public List<CombinedDto<Revision, HenkiloTietoDto>> getYleisetTavoitteetVersiot(
            @PathVariable("perusteId") final Long perusteId) {

        List<Revision> revisiot = perusteet.getYleisetTavoitteetVersiot(perusteId);
        List<CombinedDto<Revision, HenkiloTietoDto>> versiot = new ArrayList<>();
        for (Revision r : revisiot) {
            versiot.add(new CombinedDto<>(r, new HenkiloTietoDto(kayttajanTietoService.hae(r.getMuokkaajaOid()))));
        }
        return versiot;
    }

    @RequestMapping(value = "/yleisettavoitteet/versio/{revisio}", method = GET)
    public ResponseEntity<LukiokoulutuksenYleisetTavoitteetDto> getYleisetTavoitteetByVersio(
            @PathVariable("perusteId") final long perusteId,
            @PathVariable("revisio") final int revisio) {
        return handleGet(perusteId, new Supplier<LukiokoulutuksenYleisetTavoitteetDto>() {
            @Override
            public LukiokoulutuksenYleisetTavoitteetDto get() {
                return perusteet.getYleisetTavoitteetByVersion(perusteId, revisio);
            }
        });
    }

    @RequestMapping(value = "/yleisettavoitteet/palauta/{revisio}", method = POST)
    public ResponseEntity<LukiokoulutuksenYleisetTavoitteetDto> palautaYleisetTavoitteet(
            @PathVariable("perusteId") final long perusteId,
            @PathVariable("revisio") final int revisio) {

        LukiokoulutuksenYleisetTavoitteetDto dto = perusteet.palautaYleisetTavoitteet(perusteId, revisio);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }


    private <T> ResponseEntity<T> handleGet(Long perusteId, Supplier<T> response) {
        return CacheableResponse.create(perusteet.getLastModifiedRevision(perusteId), 1, response);
    }

}
