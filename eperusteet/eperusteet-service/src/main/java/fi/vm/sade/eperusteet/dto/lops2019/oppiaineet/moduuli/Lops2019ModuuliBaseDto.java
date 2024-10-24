package fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli;

import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lops2019ModuuliBaseDto implements ReferenceableDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private Boolean pakollinen;
    private KoodiDto koodi;
    private BigDecimal laajuus;

    public LokalisoituTekstiDto getNimi() {
        if (koodi != null ) {
            return koodi.getNimi();
        }

        return nimi;
    }
}
