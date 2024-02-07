package fi.vm.sade.eperusteet.dto.kayttaja;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HenkiloTietoDto {
    String kutsumanimi;
    String sukunimi;

    public HenkiloTietoDto(KayttajanTietoDto ktd) {
        if (ktd != null) {
            this.kutsumanimi = ktd.getKutsumanimi();
            this.sukunimi = ktd.getSukunimi();
        }
    }
}
