package fi.vm.sade.eperusteet.dto.validointi;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidointiStatusInfoDto {
    private String viesti;
    private RakenneValidointiDto validointi;
    private List<TekstiPalanen> nimet = new ArrayList<>();
    private Suoritustapakoodi suoritustapa;
    private Set<Kieli> kieli;
}
