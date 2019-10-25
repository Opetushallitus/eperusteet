package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.dto.lops2019.Lops2019OppiaineKaikkiDto;
import fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.yl.Lops2019Service;
import java.util.List;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Slf4j
@InternalApi
@RestController
@RequestMapping("/perusteet/{perusteId}/lops2019")
@Api("Lops2019")
public class Lops2019Controller {

    @Autowired
    private Lops2019Service service;

    @RequestMapping(value = "/sisalto", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PerusteenOsaViiteDto.Matala> addSisalto(
            @PathVariable final Long perusteId,
            @RequestBody(required = false) final PerusteenOsaViiteDto.Matala dto
    ) {
        if (dto == null || (dto.getPerusteenOsa() == null && dto.getPerusteenOsaRef() == null)) {
            return ResponseEntity.ok(service.addSisalto(perusteId, null, null));
        } else {
            return ResponseEntity.ok(service.addSisalto(perusteId, null, dto));
        }
    }

    @RequestMapping(value = "/sisalto/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSisalto(
            @PathVariable final Long perusteId,
            @PathVariable final Long id
    ) {
        service.removeSisalto(perusteId, id);
    }

    @RequestMapping(value = "/laajaalaiset", method = GET)
    public ResponseEntity<Lops2019LaajaAlainenOsaaminenKokonaisuusDto> getLaajaAlainenOsaaminenKokonaisuus(
            @PathVariable final Long perusteId
    ) {
        Lops2019LaajaAlainenOsaaminenKokonaisuusDto kokonaisuus = service.getLaajaAlainenOsaaminenKokonaisuus(perusteId);
        return ResponseEntity.ok(kokonaisuus);
    }

    @RequestMapping(value = "/laajaalaiset", method = PUT)
    public ResponseEntity<Lops2019LaajaAlainenOsaaminenKokonaisuusDto> updateLaajaAlainenOsaaminenKokonaisuus(
            @PathVariable final Long perusteId,
            @RequestBody final Lops2019LaajaAlainenOsaaminenKokonaisuusDto dto
    ) {
        return ResponseEntity.ok(service.updateLaajaAlainenOsaaminenKokonaisuus(perusteId, dto));
    }

    @RequestMapping(value = "/oppiaineet", method = GET)
    public List<Lops2019OppiaineDto> getOppiaineet(
            @PathVariable final Long perusteId
    ) {
        return service.getOppiaineet(perusteId);
    }

    @RequestMapping(value = "/oppiaineet", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<List<Lops2019OppiaineDto>> sortOppiaineet(
            @PathVariable final Long perusteId,
            @RequestBody final List<Lops2019OppiaineDto> oppiaineet
    ) {
        return ResponseEntity.ok(service.sortOppiaineet(perusteId, oppiaineet));
    }

    @RequestMapping(value = "/oppiaineet/{oppiaineId}/versiot/{rev}/palauta", method = GET)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void palautaOppiaineenSisalto(
            @PathVariable final Long perusteId,
            @PathVariable final Long oppiaineId,
            @PathVariable final int rev
    ) {
        service.restoreOppiaineRevisionInplace(perusteId, oppiaineId, rev);
    }

    @RequestMapping(value = "/oppiaineet/{oppiaineId}/versiot", method = GET)
    public List<Revision> getOppiaineenVersiot(
            @PathVariable final Long perusteId,
            @PathVariable final Long oppiaineId
    ) {
        return service.getOppiaineRevisions(perusteId, oppiaineId);
    }

    @RequestMapping(value = "/oppiaineet/{oppiaineId}/versiot/{rev}", method = GET)
    public Lops2019OppiaineKaikkiDto getOppiaineenVersioData(
            @PathVariable final Long perusteId,
            @PathVariable final Long oppiaineId,
            @PathVariable final int rev
    ) {
        return service.getOppiaineRevisionData(perusteId, oppiaineId, rev);
    }

    @RequestMapping(value = "/oppiaineet/uusi", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Lops2019OppiaineDto> addOppiaine(
            @PathVariable final Long perusteId,
            @RequestBody final Lops2019OppiaineDto dto
    ) {
        return ResponseEntity.ok(service.addOppiaine(perusteId, dto));
    }

    @RequestMapping(value = "/oppiaineet/{id}", method = GET)
    public ResponseEntity<Lops2019OppiaineDto> getOppiaine(
            @PathVariable final Long perusteId,
            @PathVariable final Long id
    ) {
        return ResponseEntity.ok(service.getOppiaine(perusteId, id));
    }

    @RequestMapping(value = "/oppiaineet/{id}/palautamoduulit", method = GET)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void palautaOppiaineenModuulit(
            @PathVariable final Long perusteId,
            @PathVariable final Long id
    ) {
        service.palautaOppiaineenModuulit(perusteId, id);
    }

    @RequestMapping(value = "/oppiaineet/{id}", method = PUT)
    public ResponseEntity<Lops2019OppiaineDto> updateOppiaine(
            @PathVariable final Long perusteId,
            @PathVariable final Long id,
            @RequestBody final Lops2019OppiaineDto dto
    ) {
        dto.setId(id);
        return ResponseEntity.ok(service.updateOppiaine(perusteId, dto));
    }

    @RequestMapping(value = "/oppiaineet/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOppiaine(
            @PathVariable final Long perusteId,
            @PathVariable final Long id
    ) {
        service.removeOppiaine(perusteId, id);
    }

    @RequestMapping(value = "/oppiaineet/{oppiaineId}/moduulit/{moduuliId}", method = GET)
    public ResponseEntity<Lops2019ModuuliDto> getModuuli(
            @PathVariable final Long perusteId,
            @PathVariable final Long oppiaineId,
            @PathVariable final Long moduuliId
    ) {
        return ResponseEntity.ok(service.getModuuli(perusteId, oppiaineId, moduuliId));
    }

    @RequestMapping(value = "/oppiaineet/{oppiaineId}/moduulit/{moduuliId}", method = PUT)
    public ResponseEntity<Lops2019ModuuliDto> updateModuuli(
            @PathVariable final Long perusteId,
            @PathVariable final Long oppiaineId,
            @PathVariable final Long moduuliId,
            @RequestBody Lops2019ModuuliDto dto
    ) {
        dto.setId(moduuliId);
        return ResponseEntity.ok(service.updateModuuli(perusteId, dto));
    }

    @RequestMapping(value = "/oppiaineet/{oppiaineId}/moduulit/{moduuliId}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModuuli(
            @PathVariable final Long perusteId,
            @PathVariable final Long oppiaineId,
            @PathVariable final Long moduuliId
    ) {
        service.removeModuuli(perusteId, oppiaineId, moduuliId);
    }

}
