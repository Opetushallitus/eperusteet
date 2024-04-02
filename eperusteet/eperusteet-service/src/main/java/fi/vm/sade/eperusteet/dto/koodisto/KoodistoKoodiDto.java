package fi.vm.sade.eperusteet.dto.koodisto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class KoodistoKoodiDto {
    private String koodiUri;
    private String koodiArvo;
    private String versio;
    private String version;
    private KoodistoMetadataDto[] metadata;
    private KoodistoDto koodisto;
    private Date voimassaAlkuPvm;
    private Date voimassaLoppuPvm;

    public KoodistoMetadataDto getMetadataName(String kieli) {
        if(metadata == null) {
            return null;
        }

        return Arrays.asList(metadata).stream()
                .filter(metadata -> metadata.getKieli().equalsIgnoreCase(kieli))
                .findFirst()
                .orElse(
                        Arrays.asList(metadata).stream()
                                .findFirst()
                                .orElse(null)
                );
    }
}
