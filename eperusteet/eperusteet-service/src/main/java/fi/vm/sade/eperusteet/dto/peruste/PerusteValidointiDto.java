package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerusteValidointiDto {
    private Long id;
    private PerusteTyyppi tyyppi;
}
