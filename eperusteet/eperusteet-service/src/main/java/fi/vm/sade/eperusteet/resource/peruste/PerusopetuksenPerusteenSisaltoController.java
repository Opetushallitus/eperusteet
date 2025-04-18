package fi.vm.sade.eperusteet.resource.peruste;

import com.google.common.base.Supplier;
import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.dto.kayttaja.HenkiloTietoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.OpetuksenKohdealueDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineenVuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.resource.util.CacheControl;
import fi.vm.sade.eperusteet.resource.util.CacheableResponse;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.yl.LaajaalainenOsaaminenService;
import fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import fi.vm.sade.eperusteet.service.yl.PerusopetuksenPerusteenSisaltoService;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkaKokonaisuusService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/perusteet/{perusteId}/perusopetus")
@Tag(name = "PerusopetuksenPerusteenSisalto")
@InternalApi
public class PerusopetuksenPerusteenSisaltoController {

    @Autowired
    private PerusopetuksenPerusteenSisaltoService sisallot;

    @Autowired
    private OppiaineService oppiaineService;

    @Autowired
    private VuosiluokkaKokonaisuusService kokonaisuudet;

    @Autowired
    private PerusteenOsaViiteService viittet;

    @Autowired
    private LaajaalainenOsaaminenService osaamiset;

    @Autowired
    private KayttajanTietoService kayttajat;

    @Autowired
    private PerusteService perusteet;

    @RequestMapping(value = "/oppiaineet", method = GET)
    public ResponseEntity<List<OppiaineSuppeaDto>> getPerusopetusOppiaineet(
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
    public OppiaineDto addPerusopetusOppiaine(
        @PathVariable("perusteId") final Long perusteId,
        @RequestBody OppiaineDto dto) {
        return oppiaineService.addOppiaine(perusteId, dto, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
    }

    @RequestMapping(value = "/oppiaineet/{id}", method = GET)
    public ResponseEntity<OppiaineDto> getPerusopetusOppiaine(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {

        return handleGet(perusteId, new Supplier<OppiaineDto>() {
            @Override
            public OppiaineDto get() {
                return oppiaineService.getOppiaine(perusteId, id, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
            }
        });
    }

    @RequestMapping(value = "/oppiaineet/jarjestys", method = POST)
    public void updateOppiaineJarjestys(
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody List<OppiaineSuppeaDto> oppiaineet) {
        oppiaineService.updateOppiaineJarjestys(perusteId, oppiaineet);
    }

    @RequestMapping(value = "/oppiaineet/{id}/versiot/{revisio}", method = GET)
    @CacheControl(age = CacheControl.ONE_YEAR)
    public OppiaineDto getOppiaineRevisiolla(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id,
        @PathVariable("revisio") final Integer revisio) {
        return oppiaineService.getOppiaine(perusteId, id, revisio, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
    }

    @RequestMapping(value = "/oppiaineet/{id}/oppimaarat", method = GET)
    public ResponseEntity<List<OppiaineSuppeaDto>> getPerusopetusOppimaarat(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {

        return handleGet(perusteId, new Supplier<List<OppiaineSuppeaDto>>() {
            @Override
            public List<OppiaineSuppeaDto> get() {
                return oppiaineService.getOppimaarat(perusteId, id, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
            }
        });

    }

    @RequestMapping(value = "/oppiaineet/{id}", method = POST)
    public OppiaineDto updatePerusopetusOppiaine(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id,
        @RequestBody UpdateDto<OppiaineDto> dto) {
        dto.getDto().setId(id);
        return oppiaineService.updateOppiaine(perusteId, dto, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
    }

    @RequestMapping(value = "/oppiaineet/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePerusopetusOppiaine(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {
        oppiaineService.deleteOppiaine(perusteId, id, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
    }

    @RequestMapping(value = "/oppiaineet/{id}/kohdealueet", method = GET)
    public Set<OpetuksenKohdealueDto> getKohdealueet(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {
        return oppiaineService.getOppiaine(perusteId, id, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS).getKohdealueet();
    }

    @RequestMapping(value = "/oppiaineet/{id}/kohdealueet", method = POST)
    public OpetuksenKohdealueDto addOppiaineenKohdealue(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id,
        @RequestBody OpetuksenKohdealueDto kohdealue) {
        return oppiaineService.addKohdealue(perusteId, id, kohdealue);
    }

    @RequestMapping(value = "/oppiaineet/{id}/kohdealueet/update", method = POST)
    public List<OpetuksenKohdealueDto> updateKohdealueet(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id,
            @RequestBody List<OpetuksenKohdealueDto> kohdealueet) {
        return oppiaineService.updateKohdealueet(perusteId, id, kohdealueet);
    }

    @RequestMapping(value = "/oppiaineet/{id}/kohdealueet/{kohdealueId}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOppiaineenKohdealue(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id,
        @PathVariable("kohdealueId") final Long kohdealueId) {
        oppiaineService.deleteKohdealue(perusteId, id, kohdealueId);
    }

    @RequestMapping(value = "/oppiaineet/{id}/vuosiluokkakokonaisuudet", method = GET)
    public ResponseEntity<Collection<OppiaineenVuosiluokkaKokonaisuusDto>> getOppiaineenVuosiluokkaKokonaisuudet(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long oppiaineId) {
        return handleGet(perusteId, new Supplier<Collection<OppiaineenVuosiluokkaKokonaisuusDto>>() {
            @Override
            public Collection<OppiaineenVuosiluokkaKokonaisuusDto> get() {
                return oppiaineService.getOppiaine(perusteId, oppiaineId, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS).getVuosiluokkakokonaisuudet();
            }
        });
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
    public ResponseEntity<OppiaineenVuosiluokkaKokonaisuusDto> getOppiaineenVuosiluokkakokonaisuus(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("oppiaineId") final Long oppiaineId,
        @PathVariable("id") final Long id) {
        return handleGet(perusteId, new Supplier<OppiaineenVuosiluokkaKokonaisuusDto>() {

            @Override
            public OppiaineenVuosiluokkaKokonaisuusDto get() {
                return oppiaineService.getOppiaineenVuosiluokkaKokonaisuus(perusteId, oppiaineId, id);
            }
        });
    }

    @RequestMapping(value = "/oppiaineet/{oppiaineId}/vuosiluokkakokonaisuudet/{id}", method = POST)
    public OppiaineenVuosiluokkaKokonaisuusDto updateOppiaineenVuosiluokkakokonaisuus(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("oppiaineId") final Long oppiaineId,
        @PathVariable("id") final Long id,
        @RequestBody UpdateDto<OppiaineenVuosiluokkaKokonaisuusDto> dto) {
        dto.getDto().setId(id);
        return oppiaineService.updateOppiaineenVuosiluokkaKokonaisuus(perusteId, oppiaineId, dto);
    }

    @RequestMapping(value = "/oppiaineet/{oppiaineId}/vuosiluokkakokonaisuudet/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOppiaineenVuosiluokkakokonaisuus(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("oppiaineId") final Long oppiaineId,
        @PathVariable("id") final Long id) {
        oppiaineService.deleteOppiaineenVuosiluokkaKokonaisuus(perusteId, oppiaineId, id);
    }

    @RequestMapping(value = "/vuosiluokkakokonaisuudet", method = GET)
    public ResponseEntity<List<VuosiluokkaKokonaisuusDto>> getVuosiluokkaKokonaisuudet(
        @PathVariable("perusteId") final Long perusteId) {
        return handleGet(perusteId, new Supplier<List<VuosiluokkaKokonaisuusDto>>() {

            @Override
            public List<VuosiluokkaKokonaisuusDto> get() {
                return sisallot.getVuosiluokkaKokonaisuudet(perusteId);
            }
        });

    }

    @RequestMapping(value = "/vuosiluokkakokonaisuudet", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public VuosiluokkaKokonaisuusDto addVuosiluokkaKokonaisuus(
        @PathVariable("perusteId") final Long perusteId,
        @RequestBody VuosiluokkaKokonaisuusDto dto) {
        return kokonaisuudet.addVuosiluokkaKokonaisuus(perusteId, dto);
    }

    @RequestMapping(value = "/vuosiluokkakokonaisuudet/{id}", method = GET)
    public ResponseEntity<VuosiluokkaKokonaisuusDto> getVuosiluokkaKokonaisuus(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {
        return handleGet(perusteId, new Supplier<VuosiluokkaKokonaisuusDto>() {

            @Override
            public VuosiluokkaKokonaisuusDto get() {
                return kokonaisuudet.getVuosiluokkaKokonaisuus(perusteId, id);
            }
        });
    }

    @RequestMapping(value = "/vuosiluokkakokonaisuudet/{id}/oppiaineet", method = GET)
    public ResponseEntity<List<OppiaineSuppeaDto>> getVuosiluokkaKokonaisuudenOppiaineet(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {
        return handleGet(perusteId, new Supplier<List<OppiaineSuppeaDto>>() {
            @Override
            public List<OppiaineSuppeaDto> get() {
                return kokonaisuudet.getOppiaineet(perusteId, id);
            }
        });
    }

    @RequestMapping(value = "/vuosiluokkakokonaisuudet/{id}", method = POST)
    public VuosiluokkaKokonaisuusDto updateVuosiluokkaKokonaisuus(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id,
        @RequestBody UpdateDto<VuosiluokkaKokonaisuusDto> dto) {
        dto.getDto().setId(id);
        return kokonaisuudet.updateVuosiluokkaKokonaisuus(perusteId, dto);
    }

    @RequestMapping(value = "/vuosiluokkakokonaisuudet/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVuosiluokkaKokonaisuus(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {
        kokonaisuudet.deleteVuosiluokkaKokonaisuus(perusteId, id);
    }

    @RequestMapping(value = "/laajaalaisetosaamiset", method = GET)
    public ResponseEntity<List<LaajaalainenOsaaminenDto>> getOsaamiset(
        @PathVariable("perusteId") final Long perusteId) {
        return handleGet(perusteId, new Supplier<List<LaajaalainenOsaaminenDto>>() {
            @Override
            public List<LaajaalainenOsaaminenDto> get() {
                return sisallot.getLaajaalaisetOsaamiset(perusteId);
            }
        });
    }

    @RequestMapping(value = "/laajaalaisetosaamiset", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public LaajaalainenOsaaminenDto addOsaaminen(
        @PathVariable("perusteId") final Long perusteId,
        @RequestBody LaajaalainenOsaaminenDto dto) {
        dto.setId(null);
        return osaamiset.addLaajaalainenOsaaminen(perusteId, dto);
    }

    @RequestMapping(value = "/laajaalaisetosaamiset/{id}", method = POST)
    public LaajaalainenOsaaminenDto updateOsaaminen(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id,
        @RequestBody LaajaalainenOsaaminenDto dto) {
        dto.setId(id);
        return osaamiset.updateLaajaalainenOsaaminen(perusteId, dto);
    }

    @RequestMapping(value = "/laajaalaisetosaamiset/{id}", method = GET)
    public ResponseEntity<LaajaalainenOsaaminenDto> getOsaaminen(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {
        return handleGet(perusteId, new Supplier<LaajaalainenOsaaminenDto>() {
            @Override
            public LaajaalainenOsaaminenDto get() {
                return osaamiset.getLaajaalainenOsaaminen(perusteId, id);
            }
        });
    }

    @RequestMapping(value = "/laajaalaisetosaamiset/{id}/versiot/{versioId}", method = GET)
    @CacheControl(age = CacheControl.ONE_YEAR)
    public LaajaalainenOsaaminenDto getOsaaminenVersiolla(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id,
        @PathVariable("versioId") final int versioId) {
        return osaamiset.getLaajaalainenOsaaminen(perusteId, id, versioId);
    }

    @RequestMapping(value = "/laajaalaisetosaamiset/{id}/versiot/", method = GET)
    public List<CombinedDto<Revision, HenkiloTietoDto>> getOsaaminenVersiot(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {
        List<Revision> versiot = osaamiset.getLaajaalainenOsaaminenVersiot(perusteId, id);
        List<CombinedDto<Revision, HenkiloTietoDto>> laajennetut = new ArrayList<>();
        for (Revision r : versiot) {
            laajennetut.add(new CombinedDto<>(r, new HenkiloTietoDto(kayttajat.hae(r.getMuokkaajaOid()))));
        }
        return laajennetut;
    }

    @RequestMapping(value = "/laajaalaisetosaamiset/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePerusopetusOsaaminen(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {
        osaamiset.deleteLaajaalainenOsaaminen(perusteId, id);
    }

    @RequestMapping(value = "/sisalto/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePerusopetusSisalto(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {
        sisallot.removeSisalto(perusteId, id);
    }

    @RequestMapping(value = "/sisalto", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public PerusteenOsaViiteDto.Matala addPerusopetusSisalto(
        @PathVariable("perusteId") final Long perusteId,
        @RequestBody(required = false) PerusteenOsaViiteDto.Matala dto) {
        if (dto == null || (dto.getPerusteenOsa() == null && dto.getPerusteenOsaRef() == null)) {
            return sisallot.addSisalto(perusteId, null, null);
        } else {
            return sisallot.addSisalto(perusteId, null, dto);
        }
    }

    @RequestMapping(value = "/sisalto/{id}/lapset", method = POST)
    public PerusteenOsaViiteDto.Matala addPerusopetuksenSisaltoLapsi(
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
    
    private <T> ResponseEntity<T> handleGet(Long perusteId, Supplier<T> response) {
        return CacheableResponse.create(perusteet.getPerusteVersion(perusteId), 1, response);
    }

}
