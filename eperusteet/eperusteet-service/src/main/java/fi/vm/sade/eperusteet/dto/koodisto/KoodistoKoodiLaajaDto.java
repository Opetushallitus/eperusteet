package fi.vm.sade.eperusteet.dto.koodisto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KoodistoKoodiLaajaDto extends KoodistoKoodiDto {
    private KoodiElementti[] includesCodeElements;
}
