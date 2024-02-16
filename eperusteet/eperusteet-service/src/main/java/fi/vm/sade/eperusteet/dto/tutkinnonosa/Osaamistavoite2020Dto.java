package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Osaamistavoite2020Dto extends OsaamistavoiteDto {
    private Ammattitaitovaatimukset2019Dto tavoitteet;
}
