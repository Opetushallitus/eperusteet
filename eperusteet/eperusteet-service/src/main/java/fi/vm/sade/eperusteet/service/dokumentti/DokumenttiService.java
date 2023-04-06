package fi.vm.sade.eperusteet.service.dokumentti;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.service.exception.DokumenttiException;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DokumenttiService {

    @PreAuthorize("hasPermission(#dto.perusteId, 'peruste', 'LUKU')")
    void setStarted(@P("dto") DokumenttiDto dto);

    @PreAuthorize("hasPermission(#dto.perusteId, 'peruste', 'LUKU')")
    void generateWithDto(@P("dto") DokumenttiDto dto) throws DokumenttiException;

    @PreAuthorize("hasPermission(#dto.perusteId, 'peruste', 'LUKU')")
    void generateWithDtoSynchronous(@P("dto") DokumenttiDto dto) throws DokumenttiException;

    @PreAuthorize("hasPermission(#id, 'peruste', 'LUKU')")
    DokumenttiDto createDtoFor(
            @P("id") final long id,
            Kieli kieli,
            Suoritustapakoodi suoritustapakoodi,
            GeneratorVersion version
    );

    @PreAuthorize("permitAll()")
    byte[] get(Long id);

    @PreAuthorize("permitAll()")
    Long getDokumenttiId(Long perusteId, Kieli kieli, Suoritustapakoodi suoritustapakoodi, GeneratorVersion generatorVersion);

    @PreAuthorize("permitAll()")
    DokumenttiDto query(Long id);

    @PreAuthorize("isAuthenticated()")
    DokumenttiDto findLatest(Long id, Kieli kieli, Suoritustapakoodi suoritustapakoodi);

    @PreAuthorize("isAuthenticated()")
    DokumenttiDto findLatest(Long id, Kieli kieli, Suoritustapakoodi suoritustapakoodi, GeneratorVersion version);

    @Deprecated
    void paivitaDokumentit();

//    @PreAuthorize("isAuthenticated()")
    void updateDokumenttiPdfData(byte[] pdfData, Long dokumenttiId);

//    @PreAuthorize("isAuthenticated()")
    void updateDokumenttiTila(DokumenttiTila tila, Long dokumenttiId);
}
