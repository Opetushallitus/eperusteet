package fi.vm.sade.eperusteet.dto.yl;

import fi.vm.sade.eperusteet.domain.yl.Vuosiluokka;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VuosiluokkakokonaisuusVainVlkDto {
    private Long id;
    private UUID tunniste;
    private Set<Vuosiluokka> vuosiluokat;
}
