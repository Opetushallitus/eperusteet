package fi.vm.sade.eperusteet.dto.kios;

import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenSisaltoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KieliJaKaantajaTutkintoSisaltoDto implements PerusteenSisaltoDto {
    private Long id;
    private PerusteenOsaViiteDto.Laaja sisalto;
}

