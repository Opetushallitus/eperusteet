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

import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.*;
import fi.vm.sade.eperusteet.dto.yl.lukio.OppiaineJarjestysDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 *
 * @author jhyoty
 */
public interface OppiaineService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    OppiaineDto addOppiaine(@P("perusteId") Long perusteId, OppiaineDto dto, OppiaineOpetuksenSisaltoTyyppi tyyppi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<OppiaineSuppeaDto> getOppimaarat(@P("perusteId") Long perusteId, Long oppiaineId, OppiaineOpetuksenSisaltoTyyppi tyyppi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    OppiaineDto getOppiaine(@P("perusteId") long perusteId, long oppiaineId, OppiaineOpetuksenSisaltoTyyppi tyyppi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    OppiaineDto getOppiaine(@P("perusteId") long perusteId, long oppiaineId, int revisio, OppiaineOpetuksenSisaltoTyyppi tyyppi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    List<Revision> getOppiaineRevisions(@P("perusteId") long perusteId, long oppiaineId, OppiaineOpetuksenSisaltoTyyppi tyyppi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    OppiaineDto revertOppiaine(@P("perusteId") long perusteId, long oppiaineId, int revisio, OppiaineOpetuksenSisaltoTyyppi tyyppi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    <T extends OppiaineBaseUpdateDto> OppiaineDto updateOppiaine(@P("perusteId") Long perusteId, UpdateDto<T> dto, OppiaineOpetuksenSisaltoTyyppi tyyppi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void deleteOppiaine(@P("perusteId") Long perusteId, Long oppiaineId, OppiaineOpetuksenSisaltoTyyppi tyyppi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    OppiaineenVuosiluokkaKokonaisuusDto addOppiaineenVuosiluokkaKokonaisuus(@P("perusteId") Long perusteId, Long oppiaineId, OppiaineenVuosiluokkaKokonaisuusDto dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    OppiaineenVuosiluokkaKokonaisuusDto getOppiaineenVuosiluokkaKokonaisuus(@P("perusteId") Long perusteId, Long oppiaineId, Long vuosiluokkaKokonaisuusId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    OppiaineenVuosiluokkaKokonaisuusDto updateOppiaineenVuosiluokkaKokonaisuus(@P("perusteId") Long perusteId, Long oppiaineId, UpdateDto<OppiaineenVuosiluokkaKokonaisuusDto> dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void deleteOppiaineenVuosiluokkaKokonaisuus(@P("perusteId") Long perusteId, Long oppiaineId, Long vuosiluokkaKokonaisuusId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    OpetuksenKohdealueDto addKohdealue(@P("perusteId") Long perusteId, Long id, OpetuksenKohdealueDto kohdealue);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    void deleteKohdealue(@P("perusteId") Long perusteId, Long id, Long kohdealueId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void jarjestaLukioOppiaineet(long perusteId, List<OppiaineJarjestysDto> oppiaineet);
}
