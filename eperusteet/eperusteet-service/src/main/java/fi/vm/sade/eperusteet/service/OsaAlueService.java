package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.OsaAlueLockCtx;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueKokonaanDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueLaajaDto;
import fi.vm.sade.eperusteet.service.impl.AbstractLockService;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

public interface OsaAlueService {

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'LUKU')")
    OsaAlueLaajaDto getOsaAlue(@P("viiteId") Long id, Long osaAlueId);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS')")
    OsaAlueLaajaDto addOsaAlue(@P("viiteId") final Long viiteId, OsaAlueLaajaDto osaAlueDto);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS')")
    OsaAlueLaajaDto updateOsaAlue(@P("viiteId") final Long viiteId, final Long osaAlueId, OsaAlueLaajaDto osaAlue);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS')")
    void removeOsaAlue(@P("viiteId") final Long viiteId, final Long osaAlueId);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS')")
    LukkoDto getOsaAlueLock(@P("viiteId")Long viiteId, Long osaAlueId);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS')")
    LukkoDto lockOsaAlue(@P("viiteId") Long viiteId, Long osaAlueId);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS')")
    void unlockOsaAlue(@P("viiteId") Long viiteId, Long osaAlueId);
}
