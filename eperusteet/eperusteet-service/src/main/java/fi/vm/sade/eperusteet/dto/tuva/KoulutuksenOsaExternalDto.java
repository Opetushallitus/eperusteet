package fi.vm.sade.eperusteet.dto.tuva;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("koulutuksenosa")
@JsonIgnoreProperties(ignoreUnknown = true)
public class KoulutuksenOsaExternalDto extends KoulutuksenOsaDto {
    private Long viiteId;
}
