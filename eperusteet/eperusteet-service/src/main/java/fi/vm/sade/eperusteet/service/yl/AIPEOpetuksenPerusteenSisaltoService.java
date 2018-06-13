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

import fi.vm.sade.eperusteet.dto.yl.*;
import java.util.List;

import fi.vm.sade.eperusteet.repository.version.Revision;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author nkala
 */
public interface AIPEOpetuksenPerusteenSisaltoService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<AIPEKurssiSuppeaDto> getKurssit(@P("perusteId") Long perusteId, Long vaiheId, Long oppiaineId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<AIPEOppiaineSuppeaDto> getOppimaarat(@P("perusteId") Long perusteId, Long vaiheId, Long oppiaineId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    AIPEOppiaineDto addOppimaara(@P("perusteId") Long perusteId, Long vaiheId, Long oppiaineId, AIPEOppiaineDto oppiaineDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    AIPEKurssiDto getKurssi(@P("perusteId") Long perusteId, Long vaiheId, Long oppiaineId, Long kurssiId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    AIPEKurssiDto addKurssi(@P("perusteId") Long perusteId, Long vaiheId, Long oppiaineId, AIPEKurssiDto kurssiDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    AIPEKurssiDto updateKurssi(@P("perusteId") Long perusteId, Long vaiheId, Long oppiaineId, Long kurssiId, AIPEKurssiDto kurssiDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void removeKurssi(@P("perusteId") Long perusteId, Long vaiheId, Long oppiaineId, Long kurssiId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<OpetuksenKohdealueDto> getKohdealueet(@P("perusteId") Long perusteId, Long vaiheId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<AIPEOppiaineSuppeaDto> getOppiaineet(@P("perusteId") Long perusteId, Long vaiheId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    AIPEOppiaineDto getOppiaine(@P("perusteId") Long perusteId, Long vaiheId, Long oppiaineId, Integer rev);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<Revision> getOppiaineRevisions(Long perusteId, Long vaiheId, Long oppiaineId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    AIPEOppiaineDto updateOppiaine(@P("perusteId") Long perusteId, Long vaiheId, Long oppiaineId, AIPEOppiaineDto oppiaineDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    AIPEOppiaineDto addOppiaine(@P("perusteId") Long perusteId, Long vaiheId, AIPEOppiaineDto oppiaineDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void removeOppiaine(@P("perusteId") Long perusteId, Long vaiheId, Long oppiaineId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    AIPEVaiheDto getVaihe(@P("perusteId") Long perusteId, Long vaiheId, Integer rev);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    AIPEVaiheDto addVaihe(@P("perusteId") Long perusteId, AIPEVaiheDto vaiheDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    AIPEVaiheDto updateVaihe(@P("perusteId") Long perusteId, Long vaiheId, AIPEVaiheDto vaiheDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void removeVaihe(@P("perusteId") Long perusteId, Long vaiheId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<AIPEVaiheSuppeaDto> getVaiheet(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    LaajaalainenOsaaminenDto getLaajaalainen(@P("perusteId") Long perusteId, Long laajalainenId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    LaajaalainenOsaaminenDto addLaajaalainen(@P("perusteId") Long perusteId, LaajaalainenOsaaminenDto laajaalainenDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    LaajaalainenOsaaminenDto updateLaajaalainen(@P("perusteId") Long perusteId, Long laajalainenId, LaajaalainenOsaaminenDto laajaalainenDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void removeLaajaalainen(@P("perusteId") Long perusteId, Long laajalainenId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<LaajaalainenOsaaminenDto> getLaajaalaiset(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void updateLaajaalainenOsaaminenJarjestys(Long perusteId, List<LaajaalainenOsaaminenDto> laajaalaiset);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void updateVaiheetJarjestys(Long perusteId, List<AIPEVaiheBaseDto> jarjestys);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void updateOppiaineetJarjestys(Long perusteId, Long vaiheId, List<AIPEOppiaineBaseDto> jarjestys);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void updateKurssitJarjestys(Long perusteId, Long vaiheId, Long oppiaineId, List<AIPEKurssiBaseDto> jarjestys);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void updateOppimaaratJarjestys(Long perusteId, Long vaiheId, Long oppiaineId, List<AIPEOppiaineBaseDto> jarjestys);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<Revision> getVaiheRevisions(Long perusteId, Long vaiheId);
}
