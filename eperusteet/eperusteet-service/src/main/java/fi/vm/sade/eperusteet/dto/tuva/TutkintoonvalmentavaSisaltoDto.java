package fi.vm.sade.eperusteet.dto.tuva;

import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import lombok.Data;

@Data
public class TutkintoonvalmentavaSisaltoDto {
    private Long id;
    private PerusteenOsaViiteDto.Laaja sisalto;
}
