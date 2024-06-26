package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.*;
import fi.vm.sade.eperusteet.dto.yl.lukio.OppiaineJarjestysDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

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

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    void updateOppiaineJarjestys(@P("perusteId") Long perusteId, List<OppiaineSuppeaDto> oppiaineet);

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
    List<OpetuksenKohdealueDto> updateKohdealueet(@P("perusteId") Long perusteId, Long oppiaineId, List<OpetuksenKohdealueDto> kohdealueet);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    void deleteKohdealue(@P("perusteId") Long perusteId, Long id, Long kohdealueId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void reArrangeLukioOppiaineet(long perusteId, List<OppiaineJarjestysDto> oppiaineet, Integer tryRestoreFromRevision);
}
