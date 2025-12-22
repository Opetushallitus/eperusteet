package fi.vm.sade.eperusteet.dto.kios;

import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KaantajaTaitotasoTutkintotasoOsaDto {

    private Long id;
    private KoodiDto suorituksenOsa;
    @Builder.Default
    private List<KaantajaTaitotasoTutkintotasoOsaTaitotasoDto> taitotasot = new ArrayList<>();
}

