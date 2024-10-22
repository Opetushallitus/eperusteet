package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.YllapitoDto;
import java.util.List;

import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import org.springframework.security.access.prepost.PreAuthorize;

public interface MaintenanceService {
    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    void addMissingOsaamisalakuvaukset();

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    void teeJulkaisut(boolean julkaiseKaikki, String tyyppi, String koulutustyyppi, String tiedote);

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    void teeJulkaisu(long perusteId, String tiedote);

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    List<YllapitoDto> getYllapidot();

    @PreAuthorize("permitAll()")
    String getYllapitoValue(String key);

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    void updateYllapito(List<YllapitoDto> yllapitoList);

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    void clearCache(String cache);

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    void teeMaarayksetPerusteille();

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    void cacheJulkisetPerusteNavigoinnit();

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    void cacheJulkaistutPerusteet();

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    void clearPerusteCaches(Long perusteId);
}
