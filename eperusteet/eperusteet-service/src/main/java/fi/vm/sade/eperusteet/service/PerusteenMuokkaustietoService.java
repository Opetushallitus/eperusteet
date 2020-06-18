package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.HistoriaTapahtuma;
import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.dto.MuokkaustietoKayttajallaDto;
import fi.vm.sade.eperusteet.dto.PerusteenMuokkaustietoDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import java.util.Date;
import java.util.List;

public interface PerusteenMuokkaustietoService {

    List<MuokkaustietoKayttajallaDto> getPerusteenMuokkausTietos(Long opsId, Date viimeisinLuontiaika, int lukumaara);

    void addOpsMuokkausTieto(Long opsId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma);

    void addOpsMuokkausTieto(Long opsId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, String lisatieto);

    void addOpsMuokkausTieto(Long opsId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, NavigationType navigationType);

    void addOpsMuokkausTieto(Long opsId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, NavigationType navigationType, String lisatieto);
}
