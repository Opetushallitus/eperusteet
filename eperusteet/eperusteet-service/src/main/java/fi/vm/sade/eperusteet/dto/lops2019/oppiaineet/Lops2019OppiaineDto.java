package fi.vm.sade.eperusteet.dto.lops2019.oppiaineet;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Lops2019OppiaineDto implements ReferenceableDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private KoodiDto koodi;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Lops2019ModuuliDto> moduulit;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Lops2019ArviointiDto arviointi;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Lops2019TehtavaDto tehtava;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Lops2019OppiaineLaajaAlainenOsaaminenDto> laajaAlaisetOsaamiset;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Lops2019OppiaineTavoitteetDto tavoitteet;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Lops2019OppiaineDto> oppimaarat;
}
