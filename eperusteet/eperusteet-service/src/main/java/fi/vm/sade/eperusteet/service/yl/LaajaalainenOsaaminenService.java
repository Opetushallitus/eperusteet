package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import java.util.List;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

public interface LaajaalainenOsaaminenService {

    @PreAuthorize(value = "hasPermission(#perusteId, 'peruste', 'LUKU')")
    public List<Revision> getLaajaalainenOsaaminenVersiot(Long perusteId, Long id);

    @PreAuthorize(value = "hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    LaajaalainenOsaaminenDto addLaajaalainenOsaaminen(@P(value = "perusteId") Long perusteId, LaajaalainenOsaaminenDto dto);

    @PreAuthorize(value = "hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void deleteLaajaalainenOsaaminen(@P(value = "perusteId") Long perusteId, Long id);

    @PreAuthorize(value = "hasPermission(#perusteId, 'peruste', 'LUKU')")
    LaajaalainenOsaaminenDto getLaajaalainenOsaaminen(@P(value = "perusteId") Long perusteId, Long id);

    @PreAuthorize(value = "hasPermission(#perusteId, 'peruste', 'LUKU')")
    LaajaalainenOsaaminenDto getLaajaalainenOsaaminen(@P(value = "perusteId") Long perusteId, Long id, int revisio);

    @PreAuthorize(value = "hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    LaajaalainenOsaaminenDto updateLaajaalainenOsaaminen(@P(value = "perusteId") Long perusteId, LaajaalainenOsaaminenDto dto);

}
