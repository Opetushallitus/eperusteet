package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

public interface Lops2019Service {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    PerusteenOsaViiteDto.Matala addSisalto(@P("perusteId") Long perusteId, Long viiteId, PerusteenOsaViiteDto.Matala dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void removeSisalto(@P("perusteId") Long perusteId, Long viiteId);
}
