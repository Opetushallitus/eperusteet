package fi.vm.sade.eperusteet.dto.lukutaitokoulutus;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LukutaitokoulutusTavoitealueDto {

    private Long id;
    private LokalisoituTekstiDto nimi;

    @Builder.Default
    private List<LokalisoituTekstiDto> tavoitteet = new ArrayList<>();
}
