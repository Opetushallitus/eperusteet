package fi.vm.sade.eperusteet.dto.perusteprojekti;

import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerusteValidationDto  {
    TilaUpdateStatus validation;
    PerusteprojektiInfoDto perusteprojekti;
}
