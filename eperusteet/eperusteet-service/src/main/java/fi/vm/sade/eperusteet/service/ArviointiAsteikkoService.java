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

import fi.vm.sade.eperusteet.dto.arviointi.ArviointiAsteikkoDto;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author jhyoty
 */
public interface ArviointiAsteikkoService {

    @PreAuthorize("permitAll()")
    List<ArviointiAsteikkoDto> getAll();

    @PreAuthorize("permitAll()")
    ArviointiAsteikkoDto get(Long id);

    @PreAuthorize("hasPermission(null, 'arviointiasteikko', 'MUOKKAUS')")
    ArviointiAsteikkoDto update(ArviointiAsteikkoDto arviointiAsteikkoDto);

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    ArviointiAsteikkoDto insert(ArviointiAsteikkoDto arviointiAsteikkoDto);

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    void delete(ArviointiAsteikkoDto arviointiAsteikkoDto);

    @PreAuthorize("hasPermission(null, 'arviointiasteikko', 'MUOKKAUS')")
    List<ArviointiAsteikkoDto> update(List<ArviointiAsteikkoDto> arviointiAsteikkoDtos);

    @PreAuthorize("hasPermission(null, 'arviointiasteikko', 'POISTO')")
    void remove(Long id);
}
