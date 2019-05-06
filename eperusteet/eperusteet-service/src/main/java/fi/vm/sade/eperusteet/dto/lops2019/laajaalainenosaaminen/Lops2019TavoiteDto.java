package fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Lops2019TavoiteDto {
    private Long id;
    private LokalisoituTekstiDto kohde;
    private List<Lops2019TavoiteTavoiteDto> tavoitteet;
}
