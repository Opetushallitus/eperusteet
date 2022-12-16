package fi.vm.sade.eperusteet.dto.validointi;

import fi.vm.sade.eperusteet.domain.validation.RakenneOngelma;
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
public class RakenneValidointiDto {
    public List<RakenneOngelmaDto> ongelmat = new ArrayList<>();
}
