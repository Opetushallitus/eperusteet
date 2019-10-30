package fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Lops2019LaajaAlainenOsaaminenKokonaisuusDto {
    private List<Lops2019LaajaAlainenOsaaminenDto> laajaAlaisetOsaamiset = new ArrayList<>();
}
