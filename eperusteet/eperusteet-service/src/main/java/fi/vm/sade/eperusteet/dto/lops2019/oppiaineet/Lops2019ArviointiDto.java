package fi.vm.sade.eperusteet.dto.lops2019.oppiaineet;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lops2019ArviointiDto {
    private LokalisoituTekstiDto kuvaus;
}
