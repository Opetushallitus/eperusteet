package fi.vm.sade.eperusteet.dto.vst;

import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenSisaltoDto;
import lombok.Data;

@Data
public class VapaasivistystyoSisaltoDto implements PerusteenSisaltoDto {
    private Long id;
    private PerusteenOsaViiteDto.Laaja sisalto;
    private Integer laajuus;
}
