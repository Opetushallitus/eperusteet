package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import java.util.List;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

public interface TutkinnonOsaViiteService {
    @PreAuthorize("hasPermission(#id, 'tutkinnonosaviite', 'LUKU')")
    public List<Revision> getVersiot(@P("id") Long id);

    @PreAuthorize("hasPermission(#id, 'tutkinnonosaviite', 'LUKU')")
    public TutkinnonOsaViiteDto getVersio(@P("id") Long id, Integer versioId);

    @PreAuthorize("hasPermission(#id, 'tutkinnonosaviite', 'LUKU')")
    public Integer getLatestRevision(@P("id") final Long id);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS') or hasPermission(#viiteId, 'tutkinnonosaviite', 'KORJAUS')")
    public LukkoDto lockPerusteenOsa(@P("viiteId") final Long viiteId);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS') or hasPermission(#viiteId, 'tutkinnonosaviite', 'KORJAUS')")
    public void unlockPerusteenOsa(@P("viiteId") final Long viiteId);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'LUKU')")
    public LukkoDto getPerusteenOsaLock(@P("viiteId") final Long viiteId);

    @PreAuthorize("hasPermission(#id, 'tutkinnonosaviite', 'MUOKKAUS')")
    TutkinnonOsaViiteDto revertToVersio(@P("id") Long id, Integer versioId);

    @PreAuthorize("hasPermission(#tov.id, 'tutkinnonosaviite', 'MUOKKAUS') or hasPermission(#tov.id, 'tutkinnonosaviite', 'KORJAUS')")
    TutkinnonOsaViiteDto update(@P("tov") TutkinnonOsaViiteDto viiteDto, boolean updateTutkinnonOsa);
}
