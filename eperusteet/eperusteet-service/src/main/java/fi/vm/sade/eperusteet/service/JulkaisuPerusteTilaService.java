package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.JulkaisuPerusteTila;
import org.springframework.security.access.prepost.PreAuthorize;

public interface JulkaisuPerusteTilaService {

    @PreAuthorize("isAuthenticated()")
    void saveJulkaisuPerusteTila(JulkaisuPerusteTila julkaisuPerusteTila);
}
