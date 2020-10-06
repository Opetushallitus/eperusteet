package fi.vm.sade.eperusteet.dto.vst;

import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import lombok.Data;

@Data
public class VapaasivistystyoSisaltoDto {
    private Long id;
    private PerusteenOsaViiteDto.Laaja sisalto;
    private Integer laajuus;
}
