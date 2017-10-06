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
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.version.Revision;

import java.util.List;

import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jhyoty
 */
@Service
@Transactional(readOnly = true)
public interface VuosiluokkaKokonaisuusService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    VuosiluokkaKokonaisuusDto addVuosiluokkaKokonaisuus(@P("perusteId") Long perusteId, VuosiluokkaKokonaisuusDto dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    VuosiluokkaKokonaisuusDto getVuosiluokkaKokonaisuus(@P("perusteId") Long perusteId, Long kokonaisuusId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    VuosiluokkaKokonaisuusDto getVuosiluokkaKokonaisuus(@P("perusteId") Long perusteId, Long kokonaisuusId, int revisio);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    List<Revision> getVuosiluokkaKokonaisuusRevisions(@P("perusteId") long perusteId, long kokonaisuusId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    VuosiluokkaKokonaisuusDto updateVuosiluokkaKokonaisuus(@P("perusteId") Long perusteId, UpdateDto<VuosiluokkaKokonaisuusDto> dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<OppiaineSuppeaDto> getOppiaineet(Long perusteId, Long kokonaisuusId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void deleteVuosiluokkaKokonaisuus(@P("perusteId") Long perusteId, Long kokonaisuusId);

}
