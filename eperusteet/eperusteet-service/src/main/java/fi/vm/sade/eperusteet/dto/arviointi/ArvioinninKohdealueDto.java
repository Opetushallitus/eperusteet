package fi.vm.sade.eperusteet.dto.arviointi;

import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArvioinninKohdealueDto {
    private LokalisoituTekstiDto otsikko;
    private List<ArvioinninKohdeDto> arvioinninKohteet;
    private KoodiDto koodi;
}
