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

import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.repository.version.Revision;

import java.util.List;

import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * @author harrik
 */
public interface TutkinnonOsaViiteService {
    @PreAuthorize("hasPermission(#id, 'tutkinnonosaviite', 'LUKU')")
    public List<Revision> getVersiot(Long id);

    @PreAuthorize("hasPermission(#id, 'tutkinnonosaviite', 'LUKU')")
    public TutkinnonOsaViiteDto getVersio(Long id, Integer versioId);

    @PreAuthorize("hasPermission(#id, 'tutkinnonosaviite', 'LUKU')")
    public Integer getLatestRevision(@P("id") final Long id);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS') or hasPermission(#viiteId, 'tutkinnonosaviite', 'KORJAUS')")
    public LukkoDto lockPerusteenOsa(@P("viiteId") final Long viiteId);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS') or hasPermission(#viiteId, 'tutkinnonosaviite', 'KORJAUS')")
    public void unlockPerusteenOsa(@P("viiteId") final Long viiteId);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'LUKU')")
    public LukkoDto getPerusteenOsaLock(@P("viiteId") final Long viiteId);

    @PreAuthorize("hasPermission(#id, 'tutkinnonosaviite', 'MUOKKAUS')")
    TutkinnonOsaViiteDto revertToVersio(@P("id") Long id, Integer versioId);

    @PreAuthorize("hasPermission(#tov.id, 'tutkinnonosaviite', 'MUOKKAUS') or hasPermission(#tov.id, 'tutkinnonosaviite', 'KORJAUS')")
    TutkinnonOsaViiteDto update(@P("tov") TutkinnonOsaViiteDto viiteDto);
}
