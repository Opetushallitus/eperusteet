package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.service.PerusteAware;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LukioAihekokonaisuudetLockContext implements PerusteAware {
    private Long perusteId;
}
