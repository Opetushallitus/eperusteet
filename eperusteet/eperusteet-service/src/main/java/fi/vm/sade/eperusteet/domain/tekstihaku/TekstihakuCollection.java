package fi.vm.sade.eperusteet.domain.tekstihaku;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TekstihakuCollection {
    private Stack<TekstihakuCtx> tekstihakuStack = new Stack<>();

    @Getter
    private List<TekstiHaku> result = new ArrayList<>();

    public TekstihakuCtx pop() {
        return tekstihakuStack.pop();
    }

    public void push(TekstihakuCtx ctx) {
        if (tekstihakuStack.isEmpty()) {
            tekstihakuStack.push(ctx);
        }
        else {
            TekstihakuCtx newCtx = TekstihakuCtx.combine(tekstihakuStack.lastElement(), ctx);
            tekstihakuStack.push(newCtx);
        }
    }

    public void add(String kuvaus, TekstiPalanen palanen) {
        if (palanen != null) {
            palanen.getTeksti().forEach((kieli, s) -> {
                this.add(kuvaus, kieli, s);
            });
        }
    }


    public void add(String kuvaus, String str) {
        this.add(kuvaus, null, str);
    }

    public void add(String kuvaus, Kieli kieli, String str) {
        if (str != null && !str.isEmpty()) {
            result.add(TekstiHaku.of(tekstihakuStack.lastElement(), kuvaus, kieli, str));
        }
    }

}
