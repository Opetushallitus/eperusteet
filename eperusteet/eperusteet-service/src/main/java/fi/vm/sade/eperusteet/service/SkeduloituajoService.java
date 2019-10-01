package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.SkeduloituAjo;
import org.springframework.security.access.prepost.PreAuthorize;

public interface SkeduloituajoService {

    @PreAuthorize("isAuthenticated()")
    SkeduloituAjo lisaaUusiAjo(String nimi);

}
