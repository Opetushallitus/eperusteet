package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.KoulutusalaDto;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

public interface KoulutusalaService {

    @PreAuthorize("permitAll()")
    List<KoulutusalaDto> getAll();
}
