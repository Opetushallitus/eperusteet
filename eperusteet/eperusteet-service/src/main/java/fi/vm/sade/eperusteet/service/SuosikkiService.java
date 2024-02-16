package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.kayttaja.SuosikkiDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface SuosikkiService {

    @PreAuthorize("isAuthenticated()")
    public SuosikkiDto get(final Long suosikkiId);
}
