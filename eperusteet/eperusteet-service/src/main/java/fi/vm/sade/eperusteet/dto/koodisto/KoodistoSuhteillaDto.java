package fi.vm.sade.eperusteet.dto.koodisto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KoodistoSuhteillaDto extends KoodistoDto {

    private List<KoodistoSuhdeDto> withinCodes;
    private List<KoodistoSuhdeDto> includesCodes;
    private List<KoodistoSuhdeDto> levelsWithCodes;

}
