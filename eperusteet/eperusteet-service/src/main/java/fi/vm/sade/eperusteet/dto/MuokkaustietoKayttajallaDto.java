package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MuokkaustietoKayttajallaDto extends PerusteenMuokkaustietoDto {
    private KayttajanTietoDto kayttajanTieto;
}
