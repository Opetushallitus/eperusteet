package fi.vm.sade.eperusteet.dto.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldComparisonFailureDto {
    private String field;
}