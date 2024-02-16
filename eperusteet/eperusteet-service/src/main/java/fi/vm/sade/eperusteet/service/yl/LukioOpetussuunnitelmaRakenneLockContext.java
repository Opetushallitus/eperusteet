package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.service.PerusteAware;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LukioOpetussuunnitelmaRakenneLockContext implements PerusteAware {
    private Long perusteId;

    public LukioOpetussuunnitelmaRakenneLockContext() {
    }

    public LukioOpetussuunnitelmaRakenneLockContext(long perusteId) {
        this.perusteId = perusteId;
    }
}
