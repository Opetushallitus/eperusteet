package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.service.PerusteAware;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LaajaalainenOsaaminenContext implements PerusteAware {

    Long perusteId;
    Long osaaminenId;

    public LaajaalainenOsaaminenContext() {
    }

    public LaajaalainenOsaaminenContext(Long perusteId, Long osaaminenId) {
        this.perusteId = perusteId;
        this.osaaminenId = osaaminenId;
    }

    public void setOsanId(Long id) {
        this.osaaminenId = id;
    }

    public static LaajaalainenOsaaminenContext of(Long perusteId, Long osaaminenId) {
        return new LaajaalainenOsaaminenContext(perusteId, osaaminenId);
    }


}
