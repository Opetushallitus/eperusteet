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

import fi.vm.sade.eperusteet.dto.lukiokoulutus.AihekokonaisuudetDto;
import fi.vm.sade.eperusteet.dto.lukiokoulutus.AihekokonaisuusDto;
import fi.vm.sade.eperusteet.dto.lukiokoulutus.YleisetTavoitteetDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * User: tommiratamaa
 * Date: 21.9.15
 * Time: 15.33
 */
public interface LukioOpetuksenPerusteenSisaltoService extends OppiainePerusteenSisaltoService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    AihekokonaisuudetDto getAihekokonaisuudet(Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    YleisetTavoitteetDto getYleisetTavoitteet(Long perusteId);

}
