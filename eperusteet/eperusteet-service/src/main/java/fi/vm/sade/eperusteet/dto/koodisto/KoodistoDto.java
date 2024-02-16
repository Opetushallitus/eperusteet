package fi.vm.sade.eperusteet.dto.koodisto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KoodistoDto {
    private String koodistoUri;

    private KoodistoVersioDto latestKoodistoVersio;

    public static KoodistoDto of(String uri) {
        KoodistoDto result = new KoodistoDto();
        result.setKoodistoUri(uri);
        return result;
    }
}
