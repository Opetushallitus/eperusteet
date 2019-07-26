package fi.vm.sade.eperusteet.dto.lops2019.oppiaineet;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliBaseDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Lops2019OppiaineDto extends Lops2019OppiaineBaseDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Lops2019ModuuliBaseDto> moduulit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Lops2019OppiaineDto> oppimaarat;
}
