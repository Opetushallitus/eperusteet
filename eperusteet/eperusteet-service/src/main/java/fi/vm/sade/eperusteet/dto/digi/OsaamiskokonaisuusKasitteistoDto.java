package fi.vm.sade.eperusteet.dto.digi;

import fi.vm.sade.eperusteet.domain.digi.DigitaalinenOsaaminenTaso;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OsaamiskokonaisuusKasitteistoDto {
    private Long id;
    private DigitaalinenOsaaminenTaso taso;
    private LokalisoituTekstiDto kuvaus;
    private LokalisoituTekstiDto keskeinenKasitteisto;
}
