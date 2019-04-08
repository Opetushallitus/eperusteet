package fi.vm.sade.eperusteet.dto.lops2019.sisalto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Lops2019SisaltoDto {
    private Long id;
    private List<Lops2019SisaltoLapsiDto> lapset;
}
