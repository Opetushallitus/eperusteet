package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import fi.vm.sade.eperusteet.dto.AbstractQueryDto;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AmmattitaitovaatimusQueryDto extends AbstractQueryDto {
    String nimi;
    String koodi;
}
