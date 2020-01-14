package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.domain.Kieli;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ParsitutAmmattitaitovaatimukset {
    Long perusteId;
    Long tutkinnonOsa;
    Long tutkinnonOsaViite;
    Map<Kieli, String> kohde = new HashMap<>();
    Map<Kieli, List<String>> vaatimukset = new HashMap<>();
}
