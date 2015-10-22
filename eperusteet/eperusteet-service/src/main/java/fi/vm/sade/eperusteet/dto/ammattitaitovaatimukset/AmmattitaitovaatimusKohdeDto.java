package fi.vm.sade.eperusteet.dto.ammattitaitovaatimukset;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by autio on 19.10.2015.
 */
@Getter
@Setter
public class AmmattitaitovaatimusKohdeDto {
    private Long id;
    private LokalisoituTekstiDto otsikko;
    private LokalisoituTekstiDto selite;
    private List<AmmattitaitovaatimusDto> vaatimukset;
}
