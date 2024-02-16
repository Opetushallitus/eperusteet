package fi.vm.sade.eperusteet.dto.kayttaja;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KayttajaprofiiliPreferenssiDto {
    Long id;
    String avain;
    String arvo;

    public KayttajaprofiiliPreferenssiDto(String avain, String arvo) {
        this.avain = avain;
        this.arvo = arvo;
    }

}
