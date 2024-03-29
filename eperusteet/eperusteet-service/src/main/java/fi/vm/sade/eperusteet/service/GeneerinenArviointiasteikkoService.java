package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.GeneerinenArviointiasteikkoDto;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface GeneerinenArviointiasteikkoService {

    @PreAuthorize("isAuthenticated()")
    List<GeneerinenArviointiasteikkoDto> getAll();

    @PreAuthorize("permitAll()")
    List<GeneerinenArviointiasteikkoDto> getJulkaistut();

    @PostAuthorize("returnObject == null or returnObject.julkaistu or hasPermission(null, 'perusteprojekti', 'LUONTI')")
    GeneerinenArviointiasteikkoDto getOne(Long id);

    @PostAuthorize("returnObject == null or returnObject.julkaistu or hasPermission(null, 'perusteprojekti', 'LUONTI')")
    <T> T getOne(Long id, Class<T> type);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    GeneerinenArviointiasteikkoDto add(GeneerinenArviointiasteikkoDto asteikko);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    GeneerinenArviointiasteikkoDto update(Long id, GeneerinenArviointiasteikkoDto asteikkoDto);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    void remove(Long id);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    GeneerinenArviointiasteikkoDto kopioi(Long id);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    boolean geneerinenArviointiJulkaistu(Long id);

}
