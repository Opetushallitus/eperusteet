package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.SkeduloituAjo;
import fi.vm.sade.eperusteet.domain.SkeduloituAjoStatus;
import org.springframework.security.access.prepost.PreAuthorize;

public interface SkeduloituajoService {

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    SkeduloituAjo haeTaiLisaaAjo(String nimi);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    SkeduloituAjo lisaaUusiAjo(String nimi);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    SkeduloituAjo paivitaAjoStatus(SkeduloituAjo skeduloituAjo, SkeduloituAjoStatus status);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    SkeduloituAjo pysaytaAjo(SkeduloituAjo skeduloituAjo);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    SkeduloituAjo kaynnistaAjo(SkeduloituAjo skeduloituAjo);

}
