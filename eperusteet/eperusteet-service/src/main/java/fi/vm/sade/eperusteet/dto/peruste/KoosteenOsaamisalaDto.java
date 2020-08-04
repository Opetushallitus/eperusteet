package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
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
