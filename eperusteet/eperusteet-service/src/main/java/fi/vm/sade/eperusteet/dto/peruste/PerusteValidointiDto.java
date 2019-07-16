package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerusteValidointiDto {
    private Long id;
    private PerusteTyyppi tyyppi;
}
