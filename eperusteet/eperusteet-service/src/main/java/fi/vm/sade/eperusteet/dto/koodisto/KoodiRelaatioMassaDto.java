package fi.vm.sade.eperusteet.dto.koodisto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KoodiRelaatioMassaDto {
    private String codeElementUri;
    private String relationType;
    private Boolean isChild;
    private List<String> relations;
    private Boolean child;
}
