package fi.vm.sade.eperusteet.dto.tuva;

import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenSisaltoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutkintoonvalmentavaSisaltoDto implements PerusteenSisaltoDto {
    private Long id;
    private PerusteenOsaViiteDto.Laaja sisalto;
}
