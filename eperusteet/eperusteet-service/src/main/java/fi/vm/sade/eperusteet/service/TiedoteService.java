package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.peruste.TiedoteQuery;
import org.springframework.data.domain.Page;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * @author mikkom
 */
public interface TiedoteService {

    @PreAuthorize("permitAll()")
    Page<TiedoteDto> findBy(TiedoteQuery tiedoteQuery);

    @PreAuthorize("permitAll()")
    @PostFilter("filterObject.julkinen or filterObject.perusteprojekti == null or hasPermission(filterObject.perusteprojekti.id, 'perusteprojekti', 'LUKU')")
    List<TiedoteDto> getAll(boolean vainJulkiset, Long alkaen, Long perusteprojektiId);

    @PreAuthorize("permitAll()")
    @PostFilter("filterObject.julkinen or filterObject.perusteprojekti == null or hasPermission(filterObject.perusteprojekti.id, 'perusteprojekti', 'LUKU')")
    List<TiedoteDto> getAll(boolean vainJulkiset, Long alkaen);

    @PostAuthorize("returnObject == null or returnObject.julkinen or isAuthenticated() or returnObject.julkaisupaikat.size() > 0")
    TiedoteDto getTiedote(@P("tiedoteId") Long tiedoteId);

    @PreAuthorize("hasPermission(null, 'tiedote', 'LUONTI') or (#tiedoteDto.perusteet != null and #tiedoteDto.perusteet.size() == 1 and hasPermission(#tiedoteDto.perusteet[0].id, 'peruste', 'MUOKKAUS'))")
    TiedoteDto addTiedote(TiedoteDto tiedoteDto);

    @PreAuthorize("hasPermission(null, 'tiedote', 'MUOKKAUS') or (#tiedoteDto.perusteet != null and #tiedoteDto.perusteet.size() == 1 and hasPermission(#tiedoteDto.perusteet[0].id, 'peruste', 'MUOKKAUS'))")
    TiedoteDto updateTiedote(TiedoteDto tiedoteDto);

    @PreAuthorize("hasPermission(null, 'tiedote', 'POISTO')")
    void removeTiedote(Long tiedoteId);
}
