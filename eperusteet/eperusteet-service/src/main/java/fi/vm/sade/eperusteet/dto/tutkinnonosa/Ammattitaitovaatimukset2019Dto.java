package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ammattitaitovaatimukset2019Dto {
    private Long id;
    private LokalisoituTekstiDto kohde;
    private List<Ammattitaitovaatimus2019Dto> vaatimukset = new ArrayList<>();
    private List<AmmattitaitovaatimustenKohdealue2019Dto> kohdealueet = new ArrayList<>();
}
