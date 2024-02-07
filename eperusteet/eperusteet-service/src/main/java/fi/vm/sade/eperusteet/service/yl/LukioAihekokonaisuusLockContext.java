package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.service.PerusteAware;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LukioAihekokonaisuusLockContext implements PerusteAware {
    private Long perusteId;
    private Long aihekokonaisuusId;

    public LukioAihekokonaisuusLockContext() {
    }

    public LukioAihekokonaisuusLockContext(Long perusteId, Long aihekokonaisuusId) {
        this.perusteId = perusteId;
        this.aihekokonaisuusId = aihekokonaisuusId;
    }

    public static LukioAihekokonaisuusLockContext of(Long perusteId, Long aihekokonaisuusId) {
        return new LukioAihekokonaisuusLockContext(perusteId, aihekokonaisuusId);
    }
}
