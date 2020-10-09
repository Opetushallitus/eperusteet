package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.vst.VapaasivistystyoSisaltoKevytDto;
import fi.vm.sade.eperusteet.dto.vst.VapaasivistystyoSisaltoDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface VapaasivistystyoSisaltoService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    VapaasivistystyoSisaltoDto update(Long perusteId, VapaasivistystyoSisaltoKevytDto vapaasivistystyoSisaltoDto);
}
