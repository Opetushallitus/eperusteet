package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.service.PerusteAware;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KurssiLockContext implements PerusteAware {
    private Long perusteId;
    private Long kurssiId;

    public KurssiLockContext() {
    }

    public KurssiLockContext(Long perusteId, Long kurssiId) {
        this.perusteId = perusteId;
        this.kurssiId = kurssiId;
    }
}
