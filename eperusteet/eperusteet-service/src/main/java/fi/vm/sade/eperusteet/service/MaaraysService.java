package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysLiiteDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MaaraysService {

    @PreAuthorize("permitAll()")
    Page<MaaraysDto> getMaaraykset(MaaraysQueryDto query);

    @PreAuthorize("permitAll()")
    <T> List<T> getMaaraykset(Class<T> clazz);

    @PreAuthorize("permitAll()")
    MaaraysDto getMaarays(Long id);

    @PreAuthorize("permitAll()")
    List<MaaraysDto> getPerusteenJulkaistutMuutosmaaraykset(@P("perusteId") Long perusteId);

    @PreAuthorize("permitAll()")
    Map<Kieli, List<String>> getAsiasanat();

    @PreAuthorize("hasPermission(null, 'maarays', 'LUONTI') or (#maarays.peruste != null && hasPermission(#maarays.peruste.id, 'peruste', 'MUOKKAUS'))")
    MaaraysDto addMaarays(@P("maarays") MaaraysDto muuMaaraysDto);

    @PreAuthorize("hasPermission(null, 'maarays', 'MUOKKAUS') or (#maarays.peruste != null && hasPermission(#maarays.peruste.id, 'peruste', 'MUOKKAUS'))")
    MaaraysDto updateMaarays(@P("maarays") MaaraysDto muuMaaraysDto);

    @PreAuthorize("hasPermission(null, 'maarays', 'POISTO')")
    void deleteMaarays(Long id);

    @PreAuthorize("hasPermission(null, 'maarays', 'LUONTI')")
    UUID uploadFile(MaaraysLiiteDto maaraysLiiteUploadDto);

    @PreAuthorize("permitAll()")
    MaaraysLiiteDto getLiite(UUID uuid);

    @PreAuthorize("permitAll()")
    void exportLiite(UUID id, OutputStream os) throws SQLException, IOException;

    @PreAuthorize("permitAll()")
    List<String> getMaarayksienKoulutustyypit();

    @PreAuthorize("hasPermission(null, 'maarays', 'LUKU') or hasPermission(#perusteId, 'peruste', 'LUKU')")
    MaaraysDto getPerusteenMaarays(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(null, 'maarays', 'LUKU') or hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<MaaraysDto> getPerusteenMuutosmaaraykset(@P("perusteId") Long perusteId);

}
