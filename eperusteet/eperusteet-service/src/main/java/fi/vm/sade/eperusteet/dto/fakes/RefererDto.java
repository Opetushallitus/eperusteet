package fi.vm.sade.eperusteet.dto.fakes;

import fi.vm.sade.eperusteet.dto.util.EntityReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefererDto {
    private EntityReference ref;
//    private java.util.Optional<EntityReference> javaOptional;
    private com.google.common.base.Optional<EntityReference> googleOptional;
}