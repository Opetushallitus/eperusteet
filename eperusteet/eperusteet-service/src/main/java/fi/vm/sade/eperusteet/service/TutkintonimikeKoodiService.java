package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface TutkintonimikeKoodiService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<TutkintonimikeKoodiDto> getTutkintonimikekoodit(@P("perusteId") Long perusteId);
}
