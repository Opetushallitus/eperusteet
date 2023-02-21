package fi.vm.sade.eperusteet.dto.kayttaja;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KayttooikeusKyselyDto {

    private Map<String, List<String>> kayttooikeudet;
    private List<String> organisaatioOids;
    private boolean passivoitu;
    private boolean duplikaatti;

}