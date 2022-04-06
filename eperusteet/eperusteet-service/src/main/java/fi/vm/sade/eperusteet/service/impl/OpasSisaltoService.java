package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.dto.peruste.OpasSisaltoKevytDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface OpasSisaltoService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    OpasSisaltoKevytDto update(Long perusteId, OpasSisaltoKevytDto opasSisaltoDto);
}
