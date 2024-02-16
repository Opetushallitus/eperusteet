package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.peruste.TermiDto;
import java.util.List;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

public interface TermistoService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU') or isAuthenticated()")
    List<TermiDto> getTermit(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU') or isAuthenticated()")
    TermiDto getTermi(@P("perusteId") Long perusteId, String avain);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    TermiDto addTermi(@P("perusteId") Long perusteId, TermiDto dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    TermiDto updateTermi(@P("perusteId") Long perusteId, TermiDto dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    void deleteTermi(@P("perusteId") Long perusteId, Long id);
}
