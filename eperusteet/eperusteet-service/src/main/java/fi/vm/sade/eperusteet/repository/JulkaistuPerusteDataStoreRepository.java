package fi.vm.sade.eperusteet.repository;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface JulkaistuPerusteDataStoreRepository {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'TILANVAIHTO')")
    void syncPeruste(Long perusteId);

    List<Long> findPerusteIdsByKoulutustyypit(List<String> koulutustyypit);

}
