package fi.vm.sade.eperusteet.dto.koodisto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KoodistoSuhteillaDto extends KoodistoDto {

    private List<KoodistoSuhdeDto> withinCodes;
    private List<KoodistoSuhdeDto> includesCodes;
    private List<KoodistoSuhdeDto> levelsWithCodes;

}
