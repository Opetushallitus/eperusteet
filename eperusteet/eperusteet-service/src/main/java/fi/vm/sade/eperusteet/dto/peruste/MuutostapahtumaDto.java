package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.dto.PerusteenMuokkaustietoDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MuutostapahtumaDto {
    private MuokkausTapahtuma tapahtuma;
    private List<PerusteenMuokkaustietoDto> muokkaustiedot;
}
