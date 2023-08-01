package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.liite.LiiteTyyppi;
import fi.vm.sade.eperusteet.dto.liite.LiiteDto;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface LiiteService {

    LiiteDto get(UUID id);

    UUID addJulkaisuLiite(Long julkaisuId, LiiteTyyppi tyyppi, String mime, String nimi, long length, InputStream is);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    UUID add(@P("perusteId") final Long perusteId, LiiteTyyppi tyyppi, String mime, String nimi, long length, InputStream is);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    UUID add(@P("perusteId") final Long perusteId, LiiteTyyppi tyyppi, String mime, String nimi, byte[] bytearray);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU') or isAuthenticated()")
    LiiteDto get(@P("perusteId") Long perusteId, UUID id);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU') or isAuthenticated()")
    List<LiiteDto> getAll(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU') or isAuthenticated()")
    List<LiiteDto> getAllByTyyppi(@P("perusteId") Long perusteId, Set<String> tyypit);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void delete(@P("perusteId") Long perusteId, UUID id);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')  or isAuthenticated()")
    void export(@P("perusteId") final Long perusteId, UUID id, OutputStream os);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    void paivitaLisatieto(@P("perusteId") final Long perusteId, UUID id, String lisatieto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void copyLiitteetForPeruste(Long perusteId, Long pohjaPerusteId);
}
