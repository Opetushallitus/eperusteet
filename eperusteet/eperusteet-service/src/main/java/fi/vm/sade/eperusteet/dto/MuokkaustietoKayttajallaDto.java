package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MuokkaustietoKayttajallaDto extends PerusteenMuokkaustietoDto {
    private KayttajanTietoDto kayttajanTieto;
}
