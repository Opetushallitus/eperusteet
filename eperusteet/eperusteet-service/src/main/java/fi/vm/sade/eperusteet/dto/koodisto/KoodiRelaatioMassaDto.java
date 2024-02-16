package fi.vm.sade.eperusteet.dto.koodisto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KoodiRelaatioMassaDto {
    private String codeElementUri;
    private String relationType;
    private Boolean isChild;
    private List<String> relations;
    private Boolean child;
}
