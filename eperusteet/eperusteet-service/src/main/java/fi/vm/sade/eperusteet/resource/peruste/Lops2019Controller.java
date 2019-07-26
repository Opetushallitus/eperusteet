package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.audit.EperusteetAudit;
import fi.vm.sade.eperusteet.service.audit.LogMessage;
import fi.vm.sade.eperusteet.service.yl.Lops2019Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static fi.vm.sade.eperusteet.service.audit.EperusteetMessageFields.*;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@InternalApi
@RestController
@RequestMapping("/perusteet/{perusteId}/lops2019")
public class Lops2019Controller {

    @Autowired
    private EperusteetAudit audit;

    @Autowired
    private Lops2019Service service;

    @RequestMapping(value = "/sisalto", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PerusteenOsaViiteDto.Matala> addSisalto(
            @PathVariable final Long perusteId,
            @RequestBody(required = false) final PerusteenOsaViiteDto.Matala dto
    ) {
        return audit.withAudit(LogMessage.builder(perusteId, PERUSTEENOSA, LISAYS), (Void) -> {
            if (dto == null || (dto.getPerusteenOsa() == null && dto.getPerusteenOsaRef() == null)) {
                return ResponseEntity.ok(service.addSisalto(perusteId, null, null));
            } else {
                return ResponseEntity.ok(service.addSisalto(perusteId, null, dto));
            }
        });
    }

    @RequestMapping(value = "/sisalto/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSisalto(
            @PathVariable final Long perusteId,
            @PathVariable final Long id
    ) {
        audit.withAudit(LogMessage.builder(perusteId, PERUSTEENOSA, POISTO), (Void) -> {
            service.removeSisalto(perusteId, id);
            return null;
        });
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
        return audit.withAudit(LogMessage.builder(perusteId, LAAJAALAINENOSAAMINEN, MUOKKAUS),
                (Void) -> ResponseEntity.ok(service.updateLaajaAlainenOsaaminenKokonaisuus(perusteId, dto)));
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
        return audit.withAudit(LogMessage.builder(perusteId, OPPIAINE, MUOKKAUS),
                (Void) -> ResponseEntity.ok(service.sortOppiaineet(perusteId, oppiaineet)));
    }

    @RequestMapping(value = "/oppiaineet/uusi", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Lops2019OppiaineDto> addOppiaine(
            @PathVariable final Long perusteId,
            @RequestBody final Lops2019OppiaineDto dto
    ) {
        return audit.withAudit(LogMessage.builder(perusteId, OPPIAINE, LISAYS),
                (Void) -> ResponseEntity.ok(service.addOppiaine(perusteId, dto)));
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
        return audit.withAudit(LogMessage.builder(perusteId, OPPIAINE, MUOKKAUS), (Void) -> {
            dto.setId(id);
            return ResponseEntity.ok(service.updateOppiaine(perusteId, dto));
        });
    }

    @RequestMapping(value = "/oppiaineet/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOppiaine(
            @PathVariable final Long perusteId,
            @PathVariable final Long id
    ) {
        audit.withAudit(LogMessage.builder(perusteId, OPPIAINE, POISTO), (Void) -> {
            service.removeOppiaine(perusteId, id);
            return null;
        });
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
        return audit.withAudit(LogMessage.builder(perusteId, OPPIAINE, MUOKKAUS), (Void) -> {
            dto.setId(moduuliId);
            return ResponseEntity.ok(service.updateModuuli(perusteId, dto));
        });
    }

    @RequestMapping(value = "/oppiaineet/{oppiaineId}/moduulit/{moduuliId}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModuuli(
            @PathVariable final Long perusteId,
            @PathVariable final Long oppiaineId,
            @PathVariable final Long moduuliId
    ) {
        audit.withAudit(LogMessage.builder(perusteId, OPPIAINE, POISTO), (Void) -> {
            service.removeModuuli(perusteId, oppiaineId, moduuliId);
            return null;
        });
    }

}
