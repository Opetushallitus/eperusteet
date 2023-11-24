package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.maarays.Maarays;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysAsiasanaDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysLiiteDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysLiiteUploadDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;

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

    @PreAuthorize("hasPermission(null, 'maarays', 'LUKU')")
    Map<Kieli, List<String>> getAsiasanat();

    @PreAuthorize("hasPermission(null, 'maarays', 'LUONTI')")
    MaaraysDto addMaarays(MaaraysDto muuMaaraysDto);

    @PreAuthorize("hasPermission(null, 'maarays', 'MUOKKAUS')")
    MaaraysDto updateMaarays(MaaraysDto muuMaaraysDto);

    @PreAuthorize("hasPermission(null, 'maarays', 'POISTO')")
    void deleteMaarays(long id);

    @PreAuthorize("hasPermission(null, 'maarays', 'LUONTI')")
    UUID uploadFile(MaaraysLiiteDto maaraysLiiteUploadDto);

    @PreAuthorize("permitAll()")
    MaaraysLiiteDto getLiite(UUID uuid);

    @PreAuthorize("permitAll()")
    void exportLiite(UUID id, OutputStream os) throws SQLException, IOException;

    @PreAuthorize("permitAll()")
    List<String> getMaarayksienKoulutustyypit();

    @PreAuthorize("hasPermission(null, 'maarays', 'LUKU')")
    MaaraysDto getPerusteenMaarays(Long perusteId);

}
