package fi.vm.sade.eperusteet.dto.kayttaja;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KayttajanTietoDto {
    public KayttajanTietoDto(String oidHenkilo) {
        this.oidHenkilo = oidHenkilo;
    }

    String kutsumanimi;
    String etunimet;
    String sukunimi;
    String oidHenkilo;
    String kieliKoodi;
    JsonNode yhteystiedot;
    Set<String> oikeudet = new HashSet<>();
}
