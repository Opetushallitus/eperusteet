package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.OpintoalaDto;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

public interface OpintoalaService {
    @PreAuthorize("permitAll()")
    public List<OpintoalaDto> getAll();
}
