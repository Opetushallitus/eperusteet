package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KoulutusalaDto {
    private String koodi;
    private LokalisoituTekstiDto nimi;
    private List<OpintoalaDto> opintoalat;
}
