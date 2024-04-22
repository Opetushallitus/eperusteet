package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.domain.Kieli;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsitutAmmattitaitovaatimukset {
    Long projektiId;
    Long perusteId;
    Long tutkinnonOsa;
    Long tutkinnonOsaViite;
    Map<Kieli, String> kohde = new HashMap<>();
    Map<Kieli, List<String>> vaatimukset = new HashMap<>();
}
