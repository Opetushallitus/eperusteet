package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ammattitaitovaatimukset2019Dto {
    private LokalisoituTekstiDto kohde;
    private Set<Ammattitaitovaatimus2019Dto> vaatimukset = new HashSet<>();
}
