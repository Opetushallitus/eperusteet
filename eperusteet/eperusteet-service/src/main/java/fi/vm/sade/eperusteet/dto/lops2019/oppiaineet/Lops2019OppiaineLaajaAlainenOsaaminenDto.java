package fi.vm.sade.eperusteet.dto.lops2019.oppiaineet;

import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class Lops2019OppiaineLaajaAlainenOsaaminenDto {
    private LokalisoituTekstiDto kuvaus;
    private Set<KoodiDto> koodit;
}
