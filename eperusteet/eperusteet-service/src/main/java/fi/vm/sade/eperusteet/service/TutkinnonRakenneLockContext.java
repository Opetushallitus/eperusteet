package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutkinnonRakenneLockContext {

    private Long perusteId;
    private Suoritustapakoodi koodi;

    public TutkinnonRakenneLockContext() {
    }

    public TutkinnonRakenneLockContext(Long perusteId, Suoritustapakoodi koodi) {
        this.perusteId = perusteId;
        this.koodi = koodi;
    }

    public void setOsanId(Long perusteId) {
        this.perusteId = perusteId;
    }

    public void setSuoritustapa(Suoritustapakoodi koodi) {
        this.koodi = koodi;
    }

    public static TutkinnonRakenneLockContext of(Long perusteId, Suoritustapakoodi koodi) {
        return new TutkinnonRakenneLockContext(perusteId, koodi);
    }
}
