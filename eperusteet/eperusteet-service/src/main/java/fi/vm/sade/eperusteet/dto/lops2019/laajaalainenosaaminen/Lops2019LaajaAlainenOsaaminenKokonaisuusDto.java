package fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lops2019LaajaAlainenOsaaminenKokonaisuusDto {
    private List<Lops2019LaajaAlainenOsaaminenDto> laajaAlaisetOsaamiset = new ArrayList<>();
}
