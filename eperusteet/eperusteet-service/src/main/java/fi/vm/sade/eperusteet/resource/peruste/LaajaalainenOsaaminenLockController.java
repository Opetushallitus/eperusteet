package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.resource.AbstractLockService;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.yl.LaajaalainenOsaaminenContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@InternalApi
@Tag(name = "PerusopetusLaajaAlainenOsaaminenLukko")
@RequestMapping("/api/perusteet/{perusteId}/perusopetus/laajaalaisetosaamiset/{osaaminenId}/lukko")
public class LaajaalainenOsaaminenLockController extends AbstractLockService<LaajaalainenOsaaminenContext> {
    @Autowired
    @LockCtx(LaajaalainenOsaaminenContext.class)
    private LockService<LaajaalainenOsaaminenContext> service;

    @Override
    protected LockService<LaajaalainenOsaaminenContext> service() {
        return service;
    }

    @RequestMapping(method = GET)
    public ResponseEntity<LukkoDto> checkLockPerusopetusLao(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("osaaminenId") final Long osaaminenId) {
        return super.checkLock(LaajaalainenOsaaminenContext.of(perusteId, osaaminenId));
    }

    @RequestMapping(method = POST)
    public ResponseEntity<LukkoDto> lockPerusopetusLao(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("osaaminenId") final Long osaaminenId,
            @RequestHeader(value = "If-Match", required = false) String eTag) {
       return super.lock(LaajaalainenOsaaminenContext.of(perusteId, osaaminenId), eTag);
    }

    @RequestMapping(method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlockPerusopetusLao(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("osaaminenId") final Long osaaminenId) {
        super.unlock(LaajaalainenOsaaminenContext.of(perusteId, osaaminenId));
    }
}
