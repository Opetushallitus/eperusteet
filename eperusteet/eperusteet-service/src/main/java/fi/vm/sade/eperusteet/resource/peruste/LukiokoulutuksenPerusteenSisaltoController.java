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
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.lukiokoulutus.AihekokonaisuudetYleiskuvausDto;
import fi.vm.sade.eperusteet.dto.lukiokoulutus.AihekokonaisuusListausDto;
import fi.vm.sade.eperusteet.dto.lukiokoulutus.YleisetTavoitteetDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.*;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.resource.util.CacheControl;
import fi.vm.sade.eperusteet.resource.util.CacheableResponse;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.yl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Set;

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

    @RequestMapping(value = "/oppiaineet", method = GET)
    public ResponseEntity<List<OppiaineSuppeaDto>> getOppiaineet(
            @PathVariable("perusteId") final Long perusteId) {
        return handleGet(perusteId, new Supplier<List<OppiaineSuppeaDto>>() {
            @Override
            public List<OppiaineSuppeaDto> get() {
                return sisallot.getOppiaineet(perusteId, OppiaineSuppeaDto.class);
            }
        });
    }

    @RequestMapping(value = "/oppiaineet", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public OppiaineDto addOppiaine(
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody OppiaineDto dto) {
        return oppiaineet.addOppiaine(perusteId, dto, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS);
    }


    @RequestMapping(value = "/kurssit", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public RedirectView addKurssi(@PathVariable("perusteId") final Long perusteId,
            @RequestBody LukioKurssiLuontiDto kurssi) {
        return new RedirectView("kurssit/"+kurssit.luoLukiokurssi(perusteId, kurssi), true);
    }

    @RequestMapping(value = "/kurssit", method = GET)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<List<LukiokurssiListausDto>> listKurssit(@PathVariable("perusteId") final Long perusteId,
                                                                   @RequestParam("kieli") Kieli kieli) {
        return handleGet(perusteId, () -> kurssit.findLukiokurssitByPerusteId(perusteId, kieli));
    }

    @RequestMapping(value = "/kurssit/{id}", method = GET)
    public ResponseEntity<LukiokurssiMuokkausDto> getKurssi(@PathVariable("perusteId") final Long perusteId,
                                  @PathVariable("id") Long id) {
        return handleGet(perusteId, () -> kurssit.getLukiokurssiMuokkausById(perusteId, id));
    }

    @RequestMapping(value = "/oppiaineet/{id}", method = GET)
    public ResponseEntity<OppiaineDto> getOppiaine(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id) {
        return handleGet(perusteId, () -> oppiaineet.getOppiaine(perusteId, id, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS));
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
            @PathVariable("perusteId") final Long perusteId,
            @RequestParam("kieli") Kieli kieli) {
        return handleGet(perusteId, new Supplier<List<AihekokonaisuusListausDto>>() {
            @Override
            public List<AihekokonaisuusListausDto> get() {
                return aihekokonaisuudet.getAihekokonaisuudet(perusteId, kieli);
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

    @RequestMapping(value = "/aihekokonaisuudet/{id}", method = GET)
    public ResponseEntity<LukioAihekokonaisuusMuokkausDto> getAihekokonaisuus(@PathVariable("perusteId") final Long perusteId,
                                                                              @PathVariable("id") final Long id) {
        return handleGet(perusteId, () -> aihekokonaisuudet.getLukioAihekokobaisuusMuokkausById(perusteId, id));
    }

    @RequestMapping(value = "/aihekokonaisuudet/aihekokonaisuus", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public RedirectView addAihekokonaisuus(@PathVariable("perusteId") final Long perusteId,
                                  @RequestBody LukioAihekokonaisuusLuontiDto aihekokonaisuusLuontiDto) {
        return new RedirectView(""+aihekokonaisuudet.luoAihekokonaisuus(perusteId, aihekokonaisuusLuontiDto), true);
    }

    @RequestMapping(value = "/aihekokonaisuudet/aihekokonaisuus/{id}", method = GET)
    public ResponseEntity<LukioAihekokonaisuusMuokkausDto> getAihekokonaisuusById(@PathVariable("perusteId") final Long perusteId,
                                                                              @PathVariable("id") final Long id) {
        return handleGet(perusteId, () -> aihekokonaisuudet.getLukioAihekokobaisuusMuokkausById(perusteId, id));
    }


    @RequestMapping(value = "/yleisettavoitteet", method = GET)
    public ResponseEntity<YleisetTavoitteetDto> getYleisetTavoitteet(
            @PathVariable("perusteId") final Long perusteId) {
        return handleGet(perusteId, new Supplier<YleisetTavoitteetDto>() {
            @Override
            public YleisetTavoitteetDto get() {
                return perusteet.getYleisetTavoitteet(perusteId);
            }
        });
    }

    private <T> ResponseEntity<T> handleGet(Long perusteId, Supplier<T> response) {
        return CacheableResponse.create(perusteet.getLastModifiedRevision(perusteId), 1, response);
    }

}
