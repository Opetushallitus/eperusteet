package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TiedoteKevytDto {
    private Long id;
    private LokalisoituTekstiDto otsikko;
    private boolean julkinen;
    private boolean yleinen;
    private Date luotu;
}
