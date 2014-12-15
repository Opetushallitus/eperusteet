package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.TiedoteDto;
import java.util.Date;
import java.util.List;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * @author mikkom
 */
public interface TiedoteService {
    @PreAuthorize("permitAll()")
    List<TiedoteDto> getAll(boolean vainJulkiset, Long alkaen);

    @PreAuthorize("permitAll()")
    TiedoteDto getTiedote(@P("tiedoteId") Long tiedoteId);

    @PreAuthorize("hasPermission(#id, 'tiedote', 'LUONTI')")
    TiedoteDto addTiedote(TiedoteDto tiedoteDto);

    @PreAuthorize("hasPermission(#id, 'tiedote', 'MUOKKAUS')")
    TiedoteDto updateTiedote(TiedoteDto tiedoteDto);

    @PreAuthorize("hasPermission(#id, 'tiedote', 'POISTO')")
    void removeTiedote(Long tiedoteId);
}
