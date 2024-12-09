package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.resource.AbstractLockService;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkaKokonaisuusContext;
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
@Tag(name = "PerusopetusVuosiluokkaKokonaisuusLukko")
@RequestMapping(value = "/api/perusteet/{perusteId}/perusopetus/vuosiluokkakokonaisuudet/{kokonaisuusId}/lukko")
public class VuosiluokkaKokonaisuusLockController extends AbstractLockService<VuosiluokkaKokonaisuusContext> {
    @Autowired
    @LockCtx(VuosiluokkaKokonaisuusContext.class)
    private LockService<VuosiluokkaKokonaisuusContext> service;

    @Override
    protected LockService<VuosiluokkaKokonaisuusContext> service() {
        return service;
    }

    @RequestMapping(method = GET)
    public ResponseEntity<LukkoDto> checkLockPerusopetusVlk(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("kokonaisuusId") final Long kokonaisuusId) {
        return super.checkLock(VuosiluokkaKokonaisuusContext.of(perusteId, kokonaisuusId));
    }

    @RequestMapping(method = POST)
    public ResponseEntity<LukkoDto> lockPerusopetusVlk(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("kokonaisuusId") final Long kokonaisuusId,
            @RequestHeader(value = "If-Match", required = false) String eTag) {
        return super.lock(VuosiluokkaKokonaisuusContext.of(perusteId, kokonaisuusId), eTag);
    }

    @RequestMapping(method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlockPerusopetusVlk(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("kokonaisuusId") final Long kokonaisuusId) {
        super.unlock(VuosiluokkaKokonaisuusContext.of(perusteId, kokonaisuusId));
    }
}
