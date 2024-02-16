package fi.vm.sade.eperusteet.dto.fakes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Referer {
    private Referable ref;
    private Referable javaOptional;
}
