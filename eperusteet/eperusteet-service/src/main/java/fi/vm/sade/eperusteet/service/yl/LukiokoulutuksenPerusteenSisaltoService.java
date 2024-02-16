package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.dto.yl.OppiaineBaseDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioOpetussuunnitelmaRakenneRevisionDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukioOppiainePuuDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface LukiokoulutuksenPerusteenSisaltoService extends OppiainePerusteenSisaltoService {
    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    LukioOppiainePuuDto getOppiaineTreeStructure(long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<Revision> listRakenneRevisions(long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    <OppiaineType extends OppiaineBaseDto> LukioOpetussuunnitelmaRakenneRevisionDto<OppiaineType>
            getLukioRakenneByRevision(long perusteId, int revision, Class<OppiaineType> oppiaineClz);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    <OppiaineType extends OppiaineBaseDto> LukioOpetussuunnitelmaRakenneRevisionDto<OppiaineType>
            getLukioRakenne(long perusteId, Class<OppiaineType> oppiaineClz);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    <T extends OppiaineBaseDto> List<T> getOppiaineetByRakenneRevision(long perusteId, int revision, Class<T> view);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    LukioOpetussuunnitelmaRakenneRevisionDto<OppiaineSuppeaDto> revertukioRakenneByRevision(
            long perusteId, int revision);
}
