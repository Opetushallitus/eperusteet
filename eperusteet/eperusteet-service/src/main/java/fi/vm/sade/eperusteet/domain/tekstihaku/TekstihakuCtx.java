package fi.vm.sade.eperusteet.domain.tekstihaku;

import com.google.common.base.Function;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class TekstihakuCtx {
    private Perusteprojekti perusteprojekti;
    private Peruste peruste;
    private String koulutustyyppi;
    private Boolean esikatseltavissa;
    private ProjektiTila tila;
    private Suoritustapa suoritustapa;
    private PerusteenOsaViite pov;
    private TutkinnonOsaViite tov;
    private TekstiKappale tekstiKappale;
    private TutkinnonOsa tutkinnonOsa;
    private OsaAlue osaalue;

    static private <T> T choose(TekstihakuCtx a, TekstihakuCtx b, Function<TekstihakuCtx, T> accessor) {
        T res = accessor.apply(b);
        return res != null ? res : accessor.apply(a);
    }

    public static TekstihakuCtx combine(TekstihakuCtx parent, TekstihakuCtx ctx) {

        if (parent == null) {
            return ctx;
        }
        else if (ctx == null) {
            return parent;
        }
        else {
            TekstihakuCtx result = new TekstihakuCtx();
            result.setPerusteprojekti(choose(parent, ctx, TekstihakuCtx::getPerusteprojekti));
            result.setPeruste(choose(parent, ctx, TekstihakuCtx::getPeruste));
            result.setKoulutustyyppi(choose(parent, ctx, TekstihakuCtx::getKoulutustyyppi));
            result.setEsikatseltavissa(choose(parent, ctx, TekstihakuCtx::getEsikatseltavissa));
            result.setTila(choose(parent, ctx, TekstihakuCtx::getTila));

            result.setSuoritustapa(choose(parent, ctx, TekstihakuCtx::getSuoritustapa));
            result.setPov(choose(parent, ctx, TekstihakuCtx::getPov));
            result.setTov(choose(parent, ctx, TekstihakuCtx::getTov));

            result.setTekstiKappale(choose(parent, ctx, TekstihakuCtx::getTekstiKappale));
            result.setTutkinnonOsa(choose(parent, ctx, TekstihakuCtx::getTutkinnonOsa));

            return result;
        }
    }
}
