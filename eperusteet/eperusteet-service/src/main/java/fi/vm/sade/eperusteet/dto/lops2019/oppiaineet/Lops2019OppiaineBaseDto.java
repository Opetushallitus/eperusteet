package fi.vm.sade.eperusteet.dto.lops2019.oppiaineet;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Lops2019OppiaineBaseDto implements ReferenceableDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private KoodiDto koodi;
    private Reference oppiaine;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Lops2019ArviointiDto arviointi;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Lops2019TehtavaDto tehtava;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Lops2019OppiaineLaajaAlainenOsaaminenDto> laajaAlaisetOsaamiset;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Lops2019OppiaineTavoitteetDto tavoitteet;
}
