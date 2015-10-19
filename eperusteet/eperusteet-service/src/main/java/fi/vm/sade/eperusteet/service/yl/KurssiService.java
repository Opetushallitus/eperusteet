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

import fi.vm.sade.eperusteet.dto.yl.lukio.*;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * User: tommiratamaa
 * Date: 29.9.15
 * Time: 14.54
 */
public interface KurssiService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<LukiokurssiListausDto> findLukiokurssitByPerusteId(long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<LukiokurssiListausDto> findLukiokurssitByOppiaineId(long perusteId, long oppiaineId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    LukiokurssiTarkasteleDto getLukiokurssiTarkasteleDtoById(long perusteId, long kurssiId) throws NotExistsException;

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    long luoLukiokurssi(long perusteId, LukioKurssiLuontiDto kurssiDto) throws BusinessRuleViolationException;

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void muokkaaLukiokurssia(long perusteId, LukiokurssiMuokkausDto muokkausDto) throws NotExistsException;

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void muokkaaLukiokurssinOppiaineliitoksia(long perusteId, LukiokurssiOppaineMuokkausDto muokkausDto)
            throws NotExistsException;

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void poistaLukiokurssi(long perusteId, long kurssiId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void updateTreeStructure(long perusteId, OppaineKurssiTreeStructureDto structure);
}
