package fi.vm.sade.eperusteet.dto.lops2019.oppiaineet;

import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Lops2019OppiaineBaseDto implements ReferenceableDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private KoodiDto koodi;
    private Reference oppiaine;

    private Lops2019ArviointiDto arviointi;
    private Lops2019TehtavaDto tehtava;
    private Lops2019OppiaineLaajaAlainenOsaaminenDto laajaAlaisetOsaamiset;
    private Lops2019OppiaineTavoitteetDto tavoitteet;
}
