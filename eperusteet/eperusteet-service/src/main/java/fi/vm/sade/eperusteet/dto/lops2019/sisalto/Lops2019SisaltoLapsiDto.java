package fi.vm.sade.eperusteet.dto.lops2019.sisalto;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Lops2019SisaltoLapsiDto {
    private Long id;
    private TekstiKappaleDto perusteenOsa;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Lops2019SisaltoLapsiDto> lapset;
}
