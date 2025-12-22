package fi.vm.sade.eperusteet.dto.kios;

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
public class KaantajaKielitaitoTaitotasoDto {

    private Long id;
    private LokalisoituTekstiDto kuvaus;
    private KoodiDto taitotaso;
}

