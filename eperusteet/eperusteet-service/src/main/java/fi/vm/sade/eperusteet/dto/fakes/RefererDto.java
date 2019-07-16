package fi.vm.sade.eperusteet.dto.fakes;

import fi.vm.sade.eperusteet.dto.Reference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefererDto {
    private Reference ref;
    private java.util.Optional<Reference> javaOptional;
}