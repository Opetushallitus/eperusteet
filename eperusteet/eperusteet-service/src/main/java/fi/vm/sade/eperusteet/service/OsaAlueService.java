package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueLaajaDto;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

public interface OsaAlueService {

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'LUKU')")
    OsaAlueLaajaDto getOsaAlue(@P("viiteId") Long id, Long osaAlueId);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS') or hasPermission(#viiteId, 'tutkinnonosaviite', 'KORJAUS')")
    OsaAlueLaajaDto addOsaAlue(@P("viiteId") final Long viiteId, OsaAlueLaajaDto osaAlueDto);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS') or hasPermission(#viiteId, 'tutkinnonosaviite', 'KORJAUS')")
    OsaAlueLaajaDto addOsaAlue(Long perusteId, @P("viiteId") final Long viiteId, OsaAlueLaajaDto osaAlueDto);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS') or hasPermission(#viiteId, 'tutkinnonosaviite', 'KORJAUS')")
    OsaAlueLaajaDto updateOsaAlue(@P("viiteId") final Long viiteId, final Long osaAlueId, OsaAlueLaajaDto osaAlue);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS') or hasPermission(#viiteId, 'tutkinnonosaviite', 'KORJAUS')")
    OsaAlueLaajaDto updateOsaAlue(Long perusteId, @P("viiteId") final Long viiteId, final Long osaAlueId, OsaAlueLaajaDto osaAlue);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS') or hasPermission(#viiteId, 'tutkinnonosaviite', 'KORJAUS')")
    void removeOsaAlue(@P("viiteId") final Long viiteId, final Long osaAlueId);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS') or hasPermission(#viiteId, 'tutkinnonosaviite', 'KORJAUS')")
    void removeOsaAlue(Long perusteId, @P("viiteId") final Long viiteId, final Long osaAlueId);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'LUKU')")
    LukkoDto getOsaAlueLock(@P("viiteId") Long viiteId, Long osaAlueId);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS') or hasPermission(#viiteId, 'tutkinnonosaviite', 'KORJAUS')")
    LukkoDto lockOsaAlue(@P("viiteId") Long viiteId, Long osaAlueId);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS') or hasPermission(#viiteId, 'tutkinnonosaviite', 'KORJAUS')")
    void unlockOsaAlue(@P("viiteId") Long viiteId, Long osaAlueId);
}
