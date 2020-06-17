package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Osaamistavoite2020Dto extends OsaamistavoiteDto {
    private Ammattitaitovaatimukset2019Dto tavoitteet;
}
