package fi.vm.sade.eperusteet.resource.peruste;

import com.google.common.base.Supplier;
import fi.vm.sade.eperusteet.dto.IdHolder;
import fi.vm.sade.eperusteet.dto.kayttaja.HenkiloTietoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.LukioOppiaineUpdateDto;
import fi.vm.sade.eperusteet.dto.yl.OpetuksenKohdealueDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.AihekokonaisuudetYleiskuvausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.AihekokonaisuusListausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioAihekokonaisuusLuontiDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioAihekokonaisuusMuokkausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioKurssiLuontiDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioOpetussuunnitelmaRakenneRevisionDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukiokoulutuksenYleisetTavoitteetDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukiokurssiListausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukiokurssiMuokkausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukiokurssiOppaineMuokkausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukiokurssiTarkasteleDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.OppaineKurssiTreeStructureDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.resource.util.CacheControl;
import fi.vm.sade.eperusteet.resource.util.CacheableResponse;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.yl.AihekokonaisuudetService;
import fi.vm.sade.eperusteet.service.yl.KurssiService;
import fi.vm.sade.eperusteet.service.yl.LukiokoulutuksenPerusteenSisaltoService;
import fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/perusteet/{perusteId}/lukiokoulutus")
@Hidden
public class LukiokoulutuksenPerusteenSisaltoController {
    private static final Logger logger = LoggerFactory.getLogger(LukiokoulutuksenPerusteenSisaltoController.class);

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
    private KayttajanTietoService kayttajanTietoService;

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

    @RequestMapping(value = "/rakenne/versiot", method = GET)
    public List<CombinedDto<Revision, HenkiloTietoDto>> listRakenneRevisions(
            @PathVariable("perusteId") final Long perusteId) {
        return withHenkilos(sisallot.listRakenneRevisions(perusteId));
    }

    @RequestMapping(value = "/rakenne/versiot/{revision}", method = GET)
    public LukioOpetussuunnitelmaRakenneRevisionDto<OppiaineSuppeaDto> getLukioRakenneByRevision(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("revision") final Integer revision) {
        return sisallot.getLukioRakenneByRevision(perusteId, revision, OppiaineSuppeaDto.class);
    }

    @RequestMapping(value = "/rakenne/versiot/{revision}/palauta", method = POST)
    public LukioOpetussuunnitelmaRakenneRevisionDto<OppiaineSuppeaDto> restorRakenneRevision(
            @PathVariable("perusteId") final Long perusteId, @PathVariable("revision") final Integer revision) {
        return sisallot.revertukioRakenneByRevision(perusteId, revision);
    }

    @RequestMapping(value = "/kurssit", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public RedirectView addKurssi(
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody LukioKurssiLuontiDto kurssi) {
        return new RedirectView("kurssit/" + kurssit.createLukiokurssi(perusteId, kurssi), true);
    }

    @RequestMapping(value = "/kurssit", method = GET)
    public ResponseEntity<List<LukiokurssiListausDto>> listKurssit(
            @PathVariable("perusteId") final Long perusteId) {
        return handleGet(perusteId, () -> kurssit.findLukiokurssitByPerusteId(perusteId));
    }

    @RequestMapping(value = "/kurssit/{kurssiId}/versiot", method = GET)
    public ResponseEntity<List<CombinedDto<Revision, HenkiloTietoDto>>> listKurssiVersiot(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("kurssiId") final Long kurssiId) {
        return handleGet(perusteId, () -> withHenkilos(kurssit.listKurssiVersions(perusteId, kurssiId)));
    }

    @RequestMapping(value = "/kurssit/{id}", method = GET)
    public ResponseEntity<LukiokurssiTarkasteleDto> getKurssi(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") Long id) {
        return handleGet(perusteId, () -> kurssit.getLukiokurssiTarkasteleDtoById(perusteId, id));
    }

    @RequestMapping(value = "/kurssit/{id}/versiot/{version}", method = GET)
    public ResponseEntity<LukiokurssiTarkasteleDto> getKurssiByRevision(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") Long id,
            @PathVariable("version") Integer version) {
        return handleGet(perusteId, () -> kurssit.getLukiokurssiTarkasteleDtoByIdAndVersion(perusteId, id, version));
    }

    @RequestMapping(value = "/kurssit/{id}/versiot/{version}/palauta", method = POST)
    public LukiokurssiTarkasteleDto revertLukioKurssi(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") Long id,
            @PathVariable("version") Integer version) {
        return kurssit.revertLukiokurssiTarkasteleDtoByIdAndVersion(perusteId, id, version);
    }

    @RequestMapping(value = "/kurssit/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteKurssi(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") Long id) {
        kurssit.deleteLukiokurssi(perusteId, id);
    }

    @RequestMapping(value = "/kurssit/{id}", method = POST)
    public RedirectView updateKurssi(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long kurssiId,
            @RequestBody LukiokurssiMuokkausDto kurssi) {
        assertKurssiId(kurssiId, kurssi);
        kurssit.updateLukiokurssi(perusteId, kurssi);
        return new RedirectView("" + kurssiId, true);
    }

    @RequestMapping(value = "/kurssit/{id}/oppiaineet", method = POST)
    public RedirectView updateKurssiOppiaineRelations(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long kurssiId,
            @RequestBody LukiokurssiOppaineMuokkausDto kurssi) {
        assertKurssiId(kurssiId, kurssi);
        kurssit.updateLukiokurssiOppiaineRelations(perusteId, kurssi);
        return new RedirectView("", true);
    }

    private void assertKurssiId(Long kurssiId, IdHolder kurssi) {
        if (kurssi.getId() == null) {
            kurssi.setId(kurssiId);
        } else if (!kurssi.getId().equals(kurssiId)) {
            throw new NotExistsException("Kurssia ei löytynyt");
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

    @RequestMapping(value = "/oppiaineet/{id}/versiot", method = GET)
    @CacheControl(age = CacheControl.ONE_YEAR)
    public List<CombinedDto<Revision, HenkiloTietoDto>> getOpppiaineVersions(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id) {
        return withHenkilos(oppiaineet.getOppiaineRevisions(perusteId, id, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS));
    }

    @RequestMapping(value = "/oppiaineet/{id}/versiot/{revision}", method = GET)
    @CacheControl(age = CacheControl.ONE_YEAR)
    public OppiaineDto getOppiaineByRevision(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id,
            @PathVariable("revision") final Integer revision) {
        return oppiaineet.getOppiaine(perusteId, id, revision, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS);
    }

    @RequestMapping(value = "/oppiaineet/{id}/versiot/{revisio}/palauta", method = POST)
    @CacheControl(age = CacheControl.ONE_YEAR)
    public OppiaineDto revertOppiaine(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id,
            @PathVariable("revisio") final Integer revisio) {
        return oppiaineet.revertOppiaine(perusteId, id, revisio, OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS);
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
            @RequestBody UpdateDto<LukioOppiaineUpdateDto> dto) {
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
    public void updateStructure(
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody OppaineKurssiTreeStructureDto structureDto) {
        kurssit.updateTreeStructure(perusteId, structureDto, null);
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
        return sisallot.addSisalto(perusteId, id, dto);
    }

    @RequestMapping(value = "/sisalto/{id}", method = POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSisaltoViite(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id,
            @RequestBody final fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Suppea pov) {
        viittet.reorderSubTree(perusteId, id, pov);
    }

    @RequestMapping(value = "/aihekokonaisuudet", method = GET)
    public ResponseEntity<List<AihekokonaisuusListausDto>> getAihekokonaisuudet(
            @PathVariable("perusteId") final Long perusteId) {
        return handleGet(perusteId, () -> aihekokonaisuudet.getAihekokonaisuudet(perusteId));
    }

    @RequestMapping(value = "/aihekokonaisuudet/yleiskuvaus", method = GET)
    public ResponseEntity<AihekokonaisuudetYleiskuvausDto> getAihekokonaisuudetYleiskuvaus(
            @PathVariable("perusteId") final Long perusteId) {
        return handleGet(perusteId, () -> aihekokonaisuudet.getAihekokonaisuudetYleiskuvaus(perusteId));
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
                -> new CombinedDto<>(r, kayttajanTiedot(r.getMuokkaajaOid())))
                .collect(toList());
    }

    private HenkiloTietoDto kayttajanTiedot(String muokkaajaOid) {
        try {
            return new HenkiloTietoDto(kayttajanTietoService.hae(muokkaajaOid));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Host is down")) {
                // Fail silently.
                logger.warn("Käyttäjätietojen haku versiotietoihin käyttäjälle {} epäonnistui. " +
                        "Palvelu alhaalla. Virhe: {}", muokkaajaOid, e.getMessage());
                return null;
            }
            throw e;
        }
    }

    @RequestMapping(value = "/aihekokonaisuudet/yleiskuvaus/versio/{revisio}", method = GET)
    public ResponseEntity<AihekokonaisuudetYleiskuvausDto> getAihekokonaisuudetYleiskuvausByVersio(
            @PathVariable("perusteId") final long perusteId,
            @PathVariable("revisio") final int revisio) {
        return handleGet(perusteId, () -> aihekokonaisuudet.getAihekokonaisuudetYleiskuvausByVersion(perusteId, revisio));
    }

    @RequestMapping(value = "/aihekokonaisuudet/yleiskuvaus/palauta/{revisio}", method = POST)
    public ResponseEntity<AihekokonaisuudetYleiskuvausDto> palautaAihekokonaisuudetYleiskuvaus(
            @PathVariable("perusteId") final long perusteId,
            @PathVariable("revisio") final int revisio) {
        AihekokonaisuudetYleiskuvausDto dto = aihekokonaisuudet.palautaAihekokonaisuudetYleiskuvaus(perusteId, revisio);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/aihekokonaisuudet/{id}", method = GET)
    public ResponseEntity<LukioAihekokonaisuusMuokkausDto> getAihekokonaisuus(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id) {
        return handleGet(perusteId, () -> aihekokonaisuudet.getLukioAihekokobaisuusMuokkausById(perusteId, id));
    }

    @RequestMapping(value = "/aihekokonaisuudet/{id}", method = POST)
    public RedirectView updateAihekokonaisuus(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long aihekokonaisuusId,
            @RequestBody LukioAihekokonaisuusMuokkausDto aihekokonaisuus) {
        if (!aihekokonaisuus.getId().equals(aihekokonaisuusId)) {
            throw new NotExistsException("Aihekokonaisuutta ei löytynyt");
        }
        aihekokonaisuudet.muokkaaAihekokonaisuutta(perusteId, aihekokonaisuus);
        return new RedirectView("" + aihekokonaisuusId, true);
    }

    @RequestMapping(value = "/aihekokonaisuudet/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAihekokonaisuus(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long aihekokonaisuusId) {
        aihekokonaisuudet.poistaAihekokonaisuus(perusteId, aihekokonaisuusId);
    }

    @RequestMapping(value = "/aihekokonaisuudet/aihekokonaisuus", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public RedirectView addAihekokonaisuus(
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody LukioAihekokonaisuusLuontiDto aihekokonaisuusLuontiDto) {
        return new RedirectView("" + aihekokonaisuudet.luoAihekokonaisuus(perusteId, aihekokonaisuusLuontiDto), true);
    }

    @RequestMapping(value = "/aihekokonaisuudet/{id}/versiot", method = GET)
    public List<CombinedDto<Revision, HenkiloTietoDto>> getAihekokonaisuusVersiot(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long aihekokonaisuusId) {
        return withHenkilos(aihekokonaisuudet.getAihekokonaisuusVersiot(perusteId, aihekokonaisuusId));
    }

    @RequestMapping(value = "/aihekokonaisuudet/{id}/versio/{revisio}", method = GET)
    public ResponseEntity<LukioAihekokonaisuusMuokkausDto> getAihekokonaisuusByVersion(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id,
            @PathVariable("revisio") final int revisio) {
        return handleGet(perusteId, () -> aihekokonaisuudet.getAihekokonaisuusByVersion(perusteId, id, revisio));
    }

    @RequestMapping(value = "/aihekokonaisuudet/{id}/palauta/{revisio}", method = POST)
    public ResponseEntity<LukioAihekokonaisuusMuokkausDto> palautaAihekokonaisuudetYleiskuvaus(
            @PathVariable("perusteId") final long perusteId,
            @PathVariable("id") final Long id,
            @PathVariable("revisio") final int revisio) {
        LukioAihekokonaisuusMuokkausDto dto = aihekokonaisuudet.palautaAihekokonaisuus(perusteId, id, revisio);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/yleisettavoitteet", method = GET)
    public ResponseEntity<LukiokoulutuksenYleisetTavoitteetDto> getYleisetTavoitteet(
            @PathVariable("perusteId") final Long perusteId) {
        return handleGet(perusteId, () -> perusteet.getYleisetTavoitteet(perusteId));
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
        return withHenkilos(perusteet.getYleisetTavoitteetVersiot(perusteId));
    }

    @RequestMapping(value = "/yleisettavoitteet/versio/{revisio}", method = GET)
    public ResponseEntity<LukiokoulutuksenYleisetTavoitteetDto> getYleisetTavoitteetByVersio(
            @PathVariable("perusteId") final long perusteId,
            @PathVariable("revisio") final int revisio) {
        return handleGet(perusteId, () -> perusteet.getYleisetTavoitteetByVersion(perusteId, revisio));
    }

    @RequestMapping(value = "/yleisettavoitteet/palauta/{revisio}", method = POST)
    public ResponseEntity<LukiokoulutuksenYleisetTavoitteetDto> palautaYleisetTavoitteet(
            @PathVariable("perusteId") final long perusteId,
            @PathVariable("revisio") final int revisio) {
        LukiokoulutuksenYleisetTavoitteetDto dto = perusteet.palautaYleisetTavoitteet(perusteId, revisio);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    private <T> ResponseEntity<T> handleGet(Long perusteId, Supplier<T> response) {
        return CacheableResponse.create(perusteet.getPerusteVersion(perusteId), 1, response);
    }
}
