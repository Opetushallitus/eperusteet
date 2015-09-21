/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineBaseDto;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * User: tommiratamaa
 * Date: 21.9.15
 * Time: 15.31
 */
public interface OppiainePerusteenSisaltoService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    <T extends OppiaineBaseDto> List<T> getOppiaineet(Long perusteId, Class<T> view);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    <T extends PerusteenOsaViiteDto<?>> T getSisalto(@P("perusteId") Long perusteId, Long sisaltoId, Class<T> view);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    PerusteenOsaViiteDto.Matala addSisalto(@P("perusteId") Long perusteId, Long viiteId, PerusteenOsaViiteDto.Matala dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void removeSisalto(@P("perusteId") Long perusteId, Long viiteId);

}
