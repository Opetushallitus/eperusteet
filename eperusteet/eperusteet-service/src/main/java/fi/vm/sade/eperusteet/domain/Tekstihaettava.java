package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.tekstihaku.TekstihakuCollection;
import fi.vm.sade.eperusteet.domain.tekstihaku.TekstihakuCtx;


public interface Tekstihaettava {
    default void traverse(TekstihakuCollection haku) {
        TekstihakuCtx ctx = this.partialContext();
        haku.push(ctx);
        getTekstihaku(haku);
        haku.pop();
    }

    void getTekstihaku(TekstihakuCollection haku);

    default TekstihakuCtx partialContext() {
        return null;
    }
}
