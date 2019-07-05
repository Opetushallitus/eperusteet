package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.peruste.JulkaisuDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface JulkaisutService {
    @PreAuthorize("permitAll()")
    JulkaisuDto getJulkaisu(long id);
}
