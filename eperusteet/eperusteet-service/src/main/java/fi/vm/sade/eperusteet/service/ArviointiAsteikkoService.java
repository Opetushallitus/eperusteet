package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.arviointi.ArviointiAsteikkoDto;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ArviointiAsteikkoService {

    @PreAuthorize("permitAll()")
    List<ArviointiAsteikkoDto> getAll();

    @PreAuthorize("permitAll()")
    ArviointiAsteikkoDto get(Long id);

    @PreAuthorize("hasPermission(null, 'arviointiasteikko', 'MUOKKAUS')")
    ArviointiAsteikkoDto update(ArviointiAsteikkoDto arviointiAsteikkoDto);

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    ArviointiAsteikkoDto insert(ArviointiAsteikkoDto arviointiAsteikkoDto);

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    void delete(ArviointiAsteikkoDto arviointiAsteikkoDto);

    @PreAuthorize("hasPermission(null, 'arviointiasteikko', 'MUOKKAUS')")
    List<ArviointiAsteikkoDto> update(List<ArviointiAsteikkoDto> arviointiAsteikkoDtos);

    @PreAuthorize("hasPermission(null, 'arviointiasteikko', 'POISTO')")
    void remove(Long id);
}
