package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.KommenttiDto;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface KommenttiService {

    @PreAuthorize("isAuthenticated()")
    List<KommenttiDto> getAllByPerusteenOsa(Long id, Long perusteeonOsaId);

    @PreAuthorize("isAuthenticated()")
    List<KommenttiDto> getAllByPerusteenOsa(Long perusteenOsaId);

    @PreAuthorize("isAuthenticated()")
    List<KommenttiDto> getAllBySuoritustapa(Long id, String suoritustapa);

    @PreAuthorize("isAuthenticated()")
    List<KommenttiDto> getAllByPerusteprojekti(Long id);

    @PreAuthorize("isAuthenticated()")
    List<KommenttiDto> getAllByParent(Long id);

    @PreAuthorize("isAuthenticated()")
    List<KommenttiDto> getAllByYlin(Long id);

    @PreAuthorize("isAuthenticated()")
    KommenttiDto get(Long kommenttiId);

    @PreAuthorize("hasPermission(#k.perusteprojektiId, 'perusteProjekti', 'LUKU')")
    KommenttiDto add(@P("k") final KommenttiDto kommenttidto);

    @PreAuthorize("isAuthenticated()")
    KommenttiDto update(Long kommenttiId, final KommenttiDto kommenttidto);

    @PreAuthorize("isAuthenticated()")
    void delete(Long kommenttiId);

    @PreAuthorize("isAuthenticated()")
    void deleteReally(Long kommenttiId);
}
