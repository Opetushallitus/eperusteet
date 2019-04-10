package fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen;

import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Lops2019PainopisteDto implements ReferenceableDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
}
