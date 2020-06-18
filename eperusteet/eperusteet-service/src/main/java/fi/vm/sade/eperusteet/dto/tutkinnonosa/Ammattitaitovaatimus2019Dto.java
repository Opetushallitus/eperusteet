package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ammattitaitovaatimus2019Dto {
    private KoodiDto koodi;
    private LokalisoituTekstiDto vaatimus;

    public LokalisoituTekstiDto getVaatimus() {
        if (this.koodi != null) {
            return new LokalisoituTekstiDto(this.koodi.getNimi());
        }
        return vaatimus;
    }
}
