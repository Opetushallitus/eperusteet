package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.service.PerusteAware;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OppiaineLockContext implements PerusteAware {

    Long perusteId;
    OppiaineOpetuksenSisaltoTyyppi tyyppi;
    Long oppiaineId;
    Long kokonaisuusId;

    public OppiaineLockContext() {
    }

    public OppiaineLockContext(OppiaineOpetuksenSisaltoTyyppi tyyppi, Long perusteId, Long oppiaineId, Long kokonaisuusId) {
        assert tyyppi != null;

        this.tyyppi = tyyppi;
        this.perusteId = perusteId;
        this.oppiaineId = oppiaineId;
        this.kokonaisuusId = kokonaisuusId;
    }


    public static OppiaineLockContext of(OppiaineOpetuksenSisaltoTyyppi tyyppi, Long perusteId, Long oppiaineId, Long kokonaisuusId) {
        return new OppiaineLockContext(tyyppi, perusteId, oppiaineId, kokonaisuusId);
    }

    public void setVuosiluokkaId(Long id) {
        this.kokonaisuusId = id;
    }

    public void setOsanId(Long id) {
        oppiaineId = id;
    }
}
