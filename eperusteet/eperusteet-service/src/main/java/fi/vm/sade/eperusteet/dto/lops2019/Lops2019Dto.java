package fi.vm.sade.eperusteet.dto.lops2019;

import fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.dto.lops2019.sisalto.Lops2019SisaltoDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Lops2019Dto {
    private Lops2019LaajaAlainenOsaaminenKokonaisuusDto laajaAlainenOsaaminen;
    private List<Lops2019OppiaineDto> oppiaineet;
    private Lops2019SisaltoDto sisalto;
}

