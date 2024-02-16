package fi.vm.sade.eperusteet.dto.yl;

import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIPEKurssiBaseDto implements AIPEHasId {
    private Long id;
    private UUID tunniste;
    private Optional<LokalisoituTekstiDto> nimi;
    private Reference oppiaine;
    private KoodiDto koodi;

    public Optional<LokalisoituTekstiDto> getNimi() {
        if (getKoodi() != null) {
            return Optional.of(getKoodi().getNimi());
        }

        return nimi;
    }
}
