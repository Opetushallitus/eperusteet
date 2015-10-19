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

import fi.vm.sade.eperusteet.dto.yl.lukio.AihekokonaisuudetYleiskuvausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.AihekokonaisuusListausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioAihekokonaisuusLuontiDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioAihekokonaisuusMuokkausDto;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * User: jsikio
 *
 */
public interface AihekokonaisuudetService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<AihekokonaisuusListausDto> getAihekokonaisuudet(Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    AihekokonaisuudetYleiskuvausDto getAihekokonaisuudetYleiskuvaus(Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    LukioAihekokonaisuusMuokkausDto getLukioAihekokobaisuusMuokkausById(long perusteId, long aihekokonaisuusId) throws NotExistsException;

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    long luoAihekokonaisuus(long perusteId, LukioAihekokonaisuusLuontiDto aihekokonaisuusLuontiDto) throws BusinessRuleViolationException;

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void muokkaaAihekokonaisuutta(long perusteId, LukioAihekokonaisuusMuokkausDto lukioAihekokonaisuusMuokkausDto) throws NotExistsException;

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void tallennaYleiskuvaus(Long perusteId, AihekokonaisuudetYleiskuvausDto aihekokonaisuudetYleiskuvausDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void poistaAihekokonaisuus(long perusteId, long aihekokonaisuusId) throws NotExistsException;

}
