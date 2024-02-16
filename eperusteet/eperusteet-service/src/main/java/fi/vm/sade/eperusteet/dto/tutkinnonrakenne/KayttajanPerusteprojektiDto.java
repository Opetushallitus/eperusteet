package fi.vm.sade.eperusteet.dto.tutkinnonrakenne;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KayttajanPerusteprojektiDto {
    Boolean passivoitu;
    String tehtavanimike;
    String organisaatioOid;
    Long id;
}
