package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.SkeduloituAjo;
import org.springframework.security.access.prepost.PreAuthorize;

public interface SkeduloituajoService {

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    SkeduloituAjo lisaaUusiAjo(String nimi);

}
