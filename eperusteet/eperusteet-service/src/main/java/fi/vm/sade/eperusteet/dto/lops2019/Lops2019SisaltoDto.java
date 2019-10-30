package fi.vm.sade.eperusteet.dto.lops2019;

import fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Lops2019SisaltoDto {
    private Lops2019LaajaAlainenOsaaminenKokonaisuusDto laajaAlainenOsaaminen;
    private List<Lops2019OppiaineKaikkiDto> oppiaineet = new ArrayList<>();
    private PerusteenOsaViiteDto.Laaja sisalto;
}

