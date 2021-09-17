package fi.vm.sade.eperusteet.dto.peruste;

import java.util.List;
import lombok.Data;

@Data
public class OpasSisaltoDto implements PerusteenSisaltoDto {

    private Long id;
    private PerusteenOsaViiteDto.Laaja sisalto;
    private List<OppaanKiinnitettyKoodiDto> oppaanKiinnitetytKoodit;
}
