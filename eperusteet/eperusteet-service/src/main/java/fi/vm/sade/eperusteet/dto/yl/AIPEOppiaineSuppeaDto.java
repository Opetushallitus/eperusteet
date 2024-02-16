package fi.vm.sade.eperusteet.dto.yl;

import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AIPEOppiaineSuppeaDto extends AIPEOppiaineBaseDto {
    private Optional<Boolean> koosteinen;
    private Optional<Boolean> abstrakti;
    private Optional<Date> muokattu;
    private Optional<KoodiDto> koodi;

    public Optional<LokalisoituTekstiDto> getNimi() {
        if (koodi != null && koodi.isPresent()) {
            return Optional.of(koodi.get().getNimi());
        }

        return super.getNimi();
    }
}
