package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.HistoriaTapahtuma;
import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.domain.PerusteenMuokkaustieto;
import fi.vm.sade.eperusteet.dto.MuokkaustietoKayttajallaDto;
import fi.vm.sade.eperusteet.dto.PerusteenMuokkaustietoDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Date;
import java.util.List;

public interface PerusteenMuokkaustietoService {

    List<MuokkaustietoKayttajallaDto> getPerusteenMuokkausTietos(Long perusteId, Date viimeisinLuontiaika, int lukumaara);

    @PreAuthorize("isAuthenticated()")
    void addMuokkaustieto(@P("perusteId") Long perusteId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma);

    @PreAuthorize("isAuthenticated()")
    void addMuokkaustieto(@P("perusteId") Long perusteId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, String lisatieto);

    @PreAuthorize("isAuthenticated()")
    void addMuokkaustieto(@P("perusteId") Long perusteId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, NavigationType navigationType);

    @PreAuthorize("isAuthenticated()")
    void addMuokkaustieto(@P("perusteId") Long perusteId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, NavigationType navigationType, String lisatieto);
}
