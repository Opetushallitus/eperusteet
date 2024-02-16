package fi.vm.sade.eperusteet.dto.yl;

import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenSisaltoDto;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerusopetuksenPerusteenSisaltoDto implements PerusteenSisaltoDto {
    private PerusteenOsaViiteDto.Laaja sisalto;
    private Set<LaajaalainenOsaaminenDto> laajaalaisetosaamiset;
    private List<OppiaineLaajaDto> oppiaineet;
    private Set<VuosiluokkaKokonaisuusDto> vuosiluokkakokonaisuudet;
}
