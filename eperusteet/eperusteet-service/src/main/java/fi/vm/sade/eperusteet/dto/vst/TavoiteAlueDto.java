package fi.vm.sade.eperusteet.dto.vst;

import fi.vm.sade.eperusteet.domain.vst.TavoiteAlueTyyppi;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TavoiteAlueDto {

    private Long id;
    private TavoiteAlueTyyppi tavoiteAlueTyyppi;
    private KoodiDto otsikko;
    private List<KoodiDto> tavoitteet = new ArrayList<>();
    private List<LokalisoituTekstiDto> keskeisetSisaltoalueet = new ArrayList<>();
}
