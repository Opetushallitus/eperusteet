package fi.vm.sade.eperusteet.service;

import org.springframework.security.access.prepost.PreAuthorize;

public interface MaintenanceService {
    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    void addMissingOsaamisalakuvaukset();
}
