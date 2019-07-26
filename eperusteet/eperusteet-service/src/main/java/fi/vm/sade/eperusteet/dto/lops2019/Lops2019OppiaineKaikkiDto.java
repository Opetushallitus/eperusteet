package fi.vm.sade.eperusteet.dto.lops2019;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineBaseDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Lops2019OppiaineKaikkiDto extends Lops2019OppiaineBaseDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Lops2019ModuuliDto> moduulit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Lops2019OppiaineKaikkiDto> oppimaarat;
}
