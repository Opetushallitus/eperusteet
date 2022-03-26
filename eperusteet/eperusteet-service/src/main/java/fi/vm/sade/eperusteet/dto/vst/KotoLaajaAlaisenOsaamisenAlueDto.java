package fi.vm.sade.eperusteet.dto.vst;

import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KotoLaajaAlaisenOsaamisenAlueDto {

    private KoodiDto koodi;
    private LokalisoituTekstiDto kuvaus;
}
