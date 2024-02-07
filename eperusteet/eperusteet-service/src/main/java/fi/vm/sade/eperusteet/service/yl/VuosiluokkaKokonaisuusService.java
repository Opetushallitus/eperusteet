package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import java.util.List;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
