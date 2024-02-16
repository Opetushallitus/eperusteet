package fi.vm.sade.eperusteet.dto.arviointi;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArviointiDto {

    private Long id;
    private LokalisoituTekstiDto lisatiedot;
    private List<ArvioinninKohdealueDto> arvioinninKohdealueet;
}
