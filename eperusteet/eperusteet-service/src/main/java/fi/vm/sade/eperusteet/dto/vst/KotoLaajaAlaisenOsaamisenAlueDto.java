package fi.vm.sade.eperusteet.dto.vst;

import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KotoLaajaAlaisenOsaamisenAlueDto {

    private KoodiDto koodi;
    private LokalisoituTekstiDto kuvaus;
}
