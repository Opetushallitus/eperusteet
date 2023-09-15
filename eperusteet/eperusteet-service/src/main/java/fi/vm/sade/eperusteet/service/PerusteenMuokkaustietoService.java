package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.HistoriaTapahtuma;
import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.domain.yl.PerusteenMuokkaustietoLisaparametrit;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenMuutostietoDto;
import fi.vm.sade.eperusteet.dto.MuokkaustietoKayttajallaDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface PerusteenMuokkaustietoService {

    List<MuokkaustietoKayttajallaDto> getPerusteenMuokkausTietos(Long perusteId, Date viimeisinLuontiaika, int lukumaara);

    @PreAuthorize("isAuthenticated()")
    void addMuokkaustieto(@P("perusteId") Long perusteId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma);

    @PreAuthorize("isAuthenticated()")
    void addMuokkaustieto(@P("perusteId") Long perusteId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, Set<PerusteenMuokkaustietoLisaparametrit> lisaparametrit);

    @PreAuthorize("isAuthenticated()")
    void addMuokkaustieto(@P("perusteId") Long perusteId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, String lisatieto);

    @PreAuthorize("isAuthenticated()")
    void addMuokkaustieto(@P("perusteId") Long perusteId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, NavigationType navigationType);

    @PreAuthorize("isAuthenticated()")
    void addMuokkaustieto(@P("perusteId") Long perusteId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, NavigationType navigationType, String lisatieto);

    @PreAuthorize("isAuthenticated()")
    void addMuokkaustieto(@P("perusteId") Long perusteId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, NavigationType navigationType, String lisatieto, Set<PerusteenMuokkaustietoLisaparametrit> lisaparametrit);

    List<PerusteenMuutostietoDto> getVersionMuutostiedot(Long perusteId, Integer revision);
}
