package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Osaamistavoite2020Dto extends OsaamistavoiteDto {
    private Ammattitaitovaatimukset2019Dto tavoitteet;
}
