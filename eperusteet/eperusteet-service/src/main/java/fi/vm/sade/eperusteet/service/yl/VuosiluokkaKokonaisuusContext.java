package fi.vm.sade.eperusteet.service.yl;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VuosiluokkaKokonaisuusContext {

    Long perusteId;
    Long kokonaisuusId;

    public VuosiluokkaKokonaisuusContext() {
    }

    public VuosiluokkaKokonaisuusContext(Long perusteId, Long kokonaisuusId) {
        this.perusteId = perusteId;
        this.kokonaisuusId = kokonaisuusId;
    }

    public void setOsanId(Long id) {
        this.kokonaisuusId = id;
    }

    public static VuosiluokkaKokonaisuusContext of(Long perusteId, Long kokonaisuusId) {
        return new VuosiluokkaKokonaisuusContext(perusteId, kokonaisuusId);
    }
}
