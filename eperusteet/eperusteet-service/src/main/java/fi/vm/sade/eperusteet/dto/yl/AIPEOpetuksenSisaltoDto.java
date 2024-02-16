package fi.vm.sade.eperusteet.dto.yl;

import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenSisaltoDto;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIPEOpetuksenSisaltoDto implements PerusteenSisaltoDto {
    private PerusteenOsaViiteDto.Laaja sisalto;
    private List<LaajaalainenOsaaminenDto> laajaalaisetosaamiset = new ArrayList<>();
    private List<AIPEVaiheDto> vaiheet = new ArrayList<>();
}
