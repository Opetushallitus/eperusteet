package fi.vm.sade.eperusteet.dto.peruste;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpasSisaltoKevytDto {

    private Long id;
    private List<OppaanKiinnitettyKoodiDto> oppaanKiinnitetytKoodit;

}
