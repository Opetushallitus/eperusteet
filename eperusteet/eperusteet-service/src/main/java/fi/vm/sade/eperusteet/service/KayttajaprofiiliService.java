/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kayttajaprofiili;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajaProfiiliDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajaprofiiliPreferenssiDto;
import fi.vm.sade.eperusteet.dto.kayttaja.SuosikkiDto;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author harrik
 */
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
