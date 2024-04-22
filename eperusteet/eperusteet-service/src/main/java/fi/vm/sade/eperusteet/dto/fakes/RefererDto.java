package fi.vm.sade.eperusteet.dto.fakes;

import fi.vm.sade.eperusteet.dto.Reference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefererDto {
    private Reference ref;
    private java.util.Optional<Reference> javaOptional;
}
