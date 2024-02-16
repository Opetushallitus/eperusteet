package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineBaseDto;
import java.util.List;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

public interface OppiainePerusteenSisaltoService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    <T extends OppiaineBaseDto> List<T> getOppiaineet(Long perusteId, Class<T> view);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    <T extends PerusteenOsaViiteDto<?>> T getSisalto(@P("perusteId") Long perusteId, Long sisaltoId, Class<T> view);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    PerusteenOsaViiteDto.Matala addSisalto(@P("perusteId") Long perusteId, Long viiteId, PerusteenOsaViiteDto.Matala dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void removeSisalto(@P("perusteId") Long perusteId, Long viiteId);
}
