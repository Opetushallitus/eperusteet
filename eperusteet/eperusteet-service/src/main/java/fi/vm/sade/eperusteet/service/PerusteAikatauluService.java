package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.peruste.PerusteAikatauluDto;
import java.util.List;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;


public interface PerusteAikatauluService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    List<PerusteAikatauluDto> save(@P("perusteId") final Long perusteId, List<PerusteAikatauluDto> perusteAikataulut);
}
