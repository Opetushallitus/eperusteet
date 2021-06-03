package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.domain.KiinnitettyKoodiTyyppi;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OppaanKiinnitettyKoodiDto {

    private Long id;
    private KiinnitettyKoodiTyyppi kiinnitettyKoodiTyyppi;
    private KoodiDto koodi;
}
