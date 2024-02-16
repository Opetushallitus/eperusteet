package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KoosteenOsaamisalaDto {
    KoodiDto koodi;
    List<KoodiDto> tutkinnonOsat = new ArrayList<>();
}
