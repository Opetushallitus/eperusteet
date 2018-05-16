package fi.vm.sade.eperusteet.dto.validointi;

import fi.vm.sade.eperusteet.domain.validation.RakenneOngelma;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RakenneValidointiDto {
    public List<RakenneOngelmaDto> ongelmat = new ArrayList<>();
}
