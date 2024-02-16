package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpintoalaDto {
    private String koodi;
    private LokalisoituTekstiDto nimi;
}
