package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.GeneerinenArviointiasteikkoDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface GeneerinenArviointiasteikkoService {

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    List<GeneerinenArviointiasteikkoDto> getAll();

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    GeneerinenArviointiasteikkoDto getOne(Long id);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    GeneerinenArviointiasteikkoDto add(GeneerinenArviointiasteikkoDto asteikko);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    GeneerinenArviointiasteikkoDto update(Long id);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    void remove(Long id);
}
