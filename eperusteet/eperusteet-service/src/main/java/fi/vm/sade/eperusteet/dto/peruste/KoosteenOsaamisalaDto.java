package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaKoosteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class KoosteenOsaamisalaDto {
    KoodiDto koodi;
    List<KoodiDto> tutkinnonOsat = new ArrayList<>();
}
