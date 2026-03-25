package fi.vm.sade.eperusteet.dto.koodisto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import fi.vm.sade.eperusteet.dto.LokalisointiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.stream.Collectors;

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

    public LokalisoituTekstiDto getNimi() {
      if (latestKoodistoVersio == null || latestKoodistoVersio.getMetadata() == null) {
        return null;
      }
      return new LokalisoituTekstiDto(
        Arrays.stream(latestKoodistoVersio.getMetadata())
        .collect(Collectors.toMap(KoodistoMetadataDto::getKieli, KoodistoMetadataDto::getNimi)));
    }
}
