package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.YllapitoDto;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

public interface MaintenanceService {
    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    void addMissingOsaamisalakuvaukset();

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    void runValidointi();

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    void teeJulkaisut();

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    List<YllapitoDto> getSallitutYllapidot();
}
