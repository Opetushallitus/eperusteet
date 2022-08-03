package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Poistettava;
import fi.vm.sade.eperusteet.dto.PoistettuSisaltoDto;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

public interface PoistoService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void restore(Long perusteId, Long poistettuId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    List<PoistettuSisaltoDto> getRemoved(Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    PoistettuSisaltoDto remove(Long perusteId, Poistettava poistettava);

}
