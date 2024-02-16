package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.dto.yl.lukio.*;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface KurssiService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<LukiokurssiListausDto> findLukiokurssitByPerusteId(long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<LukiokurssiListausDto> findLukiokurssitByRakenneRevision(long perusteId, long rakenneId, int revision);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<LukiokurssiListausDto> findLukiokurssitByOppiaineId(long perusteId, long oppiaineId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    LukiokurssiTarkasteleDto getLukiokurssiTarkasteleDtoById(long perusteId, long kurssiId) throws NotExistsException;

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    LukiokurssiTarkasteleDto revertLukiokurssiTarkasteleDtoByIdAndVersion(long perusteId, long kurssiId, int version);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    LukiokurssiTarkasteleDto getLukiokurssiTarkasteleDtoByIdAndVersion(long perusteId, long id, int version)
            throws NotExistsException;

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    long createLukiokurssi(long perusteId, LukioKurssiLuontiDto kurssiDto) throws BusinessRuleViolationException;

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void updateLukiokurssi(long perusteId, LukiokurssiMuokkausDto muokkausDto) throws NotExistsException;

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void updateLukiokurssiOppiaineRelations(long perusteId, LukiokurssiOppaineMuokkausDto muokkausDto)
            throws NotExistsException;

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void deleteLukiokurssi(long perusteId, long kurssiId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void updateTreeStructure(long perusteId, OppaineKurssiTreeStructureDto structure, Integer tryRestoreFromRevision);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<Revision> listKurssiVersions(long perusteId, long kurssiId);

}
