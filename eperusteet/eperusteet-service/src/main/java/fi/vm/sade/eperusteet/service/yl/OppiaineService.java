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
package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.dto.yl.OpetuksenKohdealueDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineenVuosiluokkaKokonaisuusDto;
import java.util.List;
import java.util.Set;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author jhyoty
 */
public interface OppiaineService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    OppiaineDto addOppiaine(@P("perusteId") Long perusteId, OppiaineDto dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<OppiaineSuppeaDto> getOppimaarat(@P("perusteId") Long perusteId, Long oppiaineId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    OppiaineDto getOppiaine(@P("perusteId") Long perusteId, Long oppiaineId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    OppiaineDto updateOppiaine(@P("perusteId") Long perusteId, OppiaineDto dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void deleteOppiaine(@P("perusteId") Long perusteId, Long oppiaineId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    OppiaineenVuosiluokkaKokonaisuusDto addOppiaineenVuosiluokkaKokonaisuus(@P("perusteId") Long perusteId, Long oppiaineId, OppiaineenVuosiluokkaKokonaisuusDto dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    OppiaineenVuosiluokkaKokonaisuusDto getOppiaineenVuosiluokkaKokonaisuus(@P("perusteId") Long perusteId, Long oppiaineId, Long vuosiluokkaKokonaisuusId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    OppiaineenVuosiluokkaKokonaisuusDto updateOppiaineenVuosiluokkaKokonaisuus(@P("perusteId") Long perusteId, Long oppiaineId, OppiaineenVuosiluokkaKokonaisuusDto dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void deleteOppiaineenVuosiluokkaKokonaisuus(@P("perusteId") Long perusteId, Long oppiaineId, Long vuosiluokkaKokonaisuusId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    Set<OpetuksenKohdealueDto> updateKohdealueet(@P("perusteId") Long perusteId, Long oppiaineId, Set<OpetuksenKohdealueDto> kohdealueet);

}
