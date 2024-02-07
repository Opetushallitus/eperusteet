package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kayttajaprofiili;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajaProfiiliDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajaprofiiliPreferenssiDto;
import fi.vm.sade.eperusteet.dto.kayttaja.SuosikkiDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface KayttajaprofiiliService {
    @PreAuthorize("isAuthenticated()")
    Kayttajaprofiili createOrGet();

    @PreAuthorize("isAuthenticated()")
    KayttajaProfiiliDto get();

    @PreAuthorize("isAuthenticated()")
    KayttajaProfiiliDto setPreference(KayttajaprofiiliPreferenssiDto uusi);

    @PreAuthorize("isAuthenticated()")
    KayttajaProfiiliDto addSuosikki(final SuosikkiDto suosikkiDto);

    @PreAuthorize("isAuthenticated()")
    KayttajaProfiiliDto deleteSuosikki(final Long suosikkiId) throws IllegalArgumentException;

    @PreAuthorize("isAuthenticated()")
    KayttajaProfiiliDto updateSuosikki(Long suosikkiId, SuosikkiDto suosikkiDto) throws IllegalArgumentException;
}
