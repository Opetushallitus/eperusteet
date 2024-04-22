package fi.vm.sade.eperusteet.dto.ammattitaitovaatimukset;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmmattitaitovaatimusKohdealueetDto {
    private Long id;
    private LokalisoituTekstiDto otsikko;
    private List<AmmattitaitovaatimusKohdeDto> vaatimuksenKohteet;
}
