package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuus;
import fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
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

import static fi.vm.sade.eperusteet.service.audit.EperusteetMessageFields.PERUSTEENOSA;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.LISAYS;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.POISTO;
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
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody(required = false) PerusteenOsaViiteDto.Matala dto
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
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("id") final Long id
    ) {
        audit.withAudit(LogMessage.builder(perusteId, PERUSTEENOSA, POISTO), (Void) -> {
            service.removeSisalto(perusteId, id);
            return null;
        });
    }

    @RequestMapping(value = "/laajaalaiset", method = GET)
    public ResponseEntity<Lops2019LaajaAlainenOsaaminenKokonaisuusDto> getLaajaAlainenOsaaminenKokonaisuus(
            @PathVariable("perusteId") final Long perusteId
    ) {
        Lops2019LaajaAlainenOsaaminenKokonaisuusDto kokonaisuus = service.getLaajaAlainenOsaaminenKokonaisuus(perusteId);
        return ResponseEntity.ok(kokonaisuus);
    }

    @RequestMapping(value = "/laajaalaiset", method = PUT)
    public ResponseEntity<Lops2019LaajaAlainenOsaaminenKokonaisuusDto> updateLaajaAlainenOsaaminenKokonaisuus(
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody Lops2019LaajaAlainenOsaaminenKokonaisuusDto dto
    ) {
        return ResponseEntity.ok(service.updateLaajaAlainenOsaaminenKokonaisuus(perusteId, dto));
    }

    @RequestMapping(value = "/oppiaineet", method = GET)
    public List<Lops2019OppiaineDto> getOppiaineet(
            @PathVariable("perusteId") final Long perusteId
    ) {
        return service.getOppiaineet(perusteId);
    }

}
