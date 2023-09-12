package fi.vm.sade.eperusteet.service;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface AmosaaClient {

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    List<Object> getTilastot();

    @PreAuthorize("hasPermission(null, 'arviointiasteikko', 'MUOKKAUS')")
    void updateArvioinnit();
}
