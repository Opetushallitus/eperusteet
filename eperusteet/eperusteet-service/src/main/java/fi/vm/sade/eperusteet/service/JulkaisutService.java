package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface JulkaisutService {
    @PreAuthorize("permitAll()")
    List<JulkaisuBaseDto> getJulkaisut(long id);

    @PreAuthorize("hasPermission(#projektiId, 'perusteprojekti', 'TILANVAIHTO')")
    JulkaisuBaseDto teeJulkaisu(@P("projektiId") long projektiId, JulkaisuBaseDto julkaisuBaseDto);

    @PreAuthorize("hasPermission(#projektiId, 'perusteprojekti', 'TILANVAIHTO')")
    JulkaisuBaseDto aktivoiJulkaisu(@P("projektiId") long projektiId, int revision);
}
