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

import fi.vm.sade.eperusteet.dto.liite.LiiteDto;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author jhyoty
 */
public interface LiiteService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    public UUID add(@P("perusteId") final Long opsId, String tyyppi, String nimi, long length, InputStream is);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    public LiiteDto get(Long perusteId, UUID id);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    public List<LiiteDto> getAll(Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    public void delete(Long perusteId, UUID id);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    public void export(@P("perusteId") final Long perusteId, UUID id, OutputStream os);

}
