package fi.vm.sade.eperusteet.domain.views;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@EqualsAndHashCode(of = { "perusteprojekti", "peruste", "tekstipalanen", "kieli" })
public class TekstiHakuId implements Serializable {
    private Perusteprojekti perusteprojekti;
    private Peruste peruste;
    private TekstiPalanen tekstipalanen;
    private Kieli kieli;

    public TekstiHakuId() {
    }

    public TekstiHakuId(Perusteprojekti perusteprojekti, Peruste peruste, TekstiPalanen tekstipalanen, Kieli kieli) {
        this.perusteprojekti = perusteprojekti;
        this.peruste = peruste;
        this.tekstipalanen = tekstipalanen;
        this.kieli = kieli;
    }
}
