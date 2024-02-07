package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OsaamistasoDto {
    private Long id;
    private LokalisoituTekstiDto otsikko;
    private KoodiDto koodi;
}
