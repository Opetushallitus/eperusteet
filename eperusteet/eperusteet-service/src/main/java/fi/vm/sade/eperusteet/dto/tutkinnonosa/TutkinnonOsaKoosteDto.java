package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutkinnonOsaKoosteDto {
    private LokalisoituTekstiDto nimi;
    private String koodiArvo;
}
