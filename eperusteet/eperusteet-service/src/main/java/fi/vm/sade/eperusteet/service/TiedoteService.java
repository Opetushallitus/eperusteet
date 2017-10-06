package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.TiedoteDto;

import java.util.List;

import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * @author mikkom
 */
public interface TiedoteService {
    @PreAuthorize("permitAll()")
    @PostFilter("filterObject.julkinen or filterObject.perusteprojekti == null or hasPermission(filterObject.perusteprojekti.id, 'perusteprojekti', 'LUKU')")
    List<TiedoteDto> getAll(boolean vainJulkiset, Long alkaen, Long perusteprojektiId);

    @PreAuthorize("permitAll()")
    @PostFilter("filterObject.julkinen or filterObject.perusteprojekti == null or hasPermission(filterObject.perusteprojekti.id, 'perusteprojekti', 'LUKU')")
    List<TiedoteDto> getAll(boolean vainJulkiset, Long alkaen);

    @PostAuthorize("returnObject == null or returnObject.julkinen or isAuthenticated()")
    TiedoteDto getTiedote(@P("tiedoteId") Long tiedoteId);

    @PreAuthorize("hasPermission(#id, 'tiedote', 'LUONTI')")
    TiedoteDto addTiedote(TiedoteDto tiedoteDto);

    @PreAuthorize("hasPermission(#id, 'tiedote', 'MUOKKAUS')")
    TiedoteDto updateTiedote(TiedoteDto tiedoteDto);

    @PreAuthorize("hasPermission(#id, 'tiedote', 'POISTO')")
    void removeTiedote(Long tiedoteId);
}
