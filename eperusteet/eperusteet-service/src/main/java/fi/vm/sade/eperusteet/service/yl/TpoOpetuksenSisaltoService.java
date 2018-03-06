package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.dto.util.TaiteenalaViiteUpdateDto;
import fi.vm.sade.eperusteet.dto.yl.TaiteenalaDto;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Deprecated
public interface TpoOpetuksenSisaltoService {
    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<TaiteenalaViiteUpdateDto> getTaiteenalat(Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    TaiteenalaDto getTaiteenala(@P("perusteId") Long perusteId, Long taiteenalaId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    TaiteenalaDto updateTaiteenala(@P("perusteId") Long perusteId, Long taiteenalaId, TaiteenalaDto oppiaineDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    TaiteenalaDto addTaiteenala(@P("perusteId") Long perusteId, TaiteenalaDto oppiaineDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void removeTaiteenala(Long perusteId, Long taiteenalaId);
}
