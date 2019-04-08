package fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Lops2019LaajaAlainenOsaaminenDto implements ReferenceableDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LokalisoituTekstiDto kuvaus;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LokalisoituTekstiDto opinnot;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Lops2019TavoiteDto> tavoitteet;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Lops2019PainopisteDto> painopisteet;
}
