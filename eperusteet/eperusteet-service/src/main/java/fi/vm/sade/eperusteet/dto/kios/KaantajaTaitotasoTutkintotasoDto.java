package fi.vm.sade.eperusteet.dto.kios;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
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
public class KaantajaTaitotasoTutkintotasoDto {

    private Long id;
    private LokalisoituTekstiDto nimi;
    @Builder.Default
    private List<KaantajaTaitotasoTutkintotasoOsaDto> osat = new ArrayList<>();
}

