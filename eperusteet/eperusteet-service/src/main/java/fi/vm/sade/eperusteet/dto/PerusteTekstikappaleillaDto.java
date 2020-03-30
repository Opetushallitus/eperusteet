package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PerusteTekstikappaleillaDto {

    private PerusteDto perusteDto;
    private List<TekstiKappaleDto> tekstikappeet;

}
