package fi.vm.sade.eperusteet.service.dokumentti;

import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DokumenttiStateService {

    @PreAuthorize("isAuthenticated()")
    DokumenttiDto save(DokumenttiDto dto);

}
