package fi.vm.sade.eperusteet.domain.tekstihaku;

import fi.vm.sade.eperusteet.domain.*;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;


//@Immutable
//@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Getter
@Setter
//@Table(name = "tekstihaku")
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@EqualsAndHashCode(exclude = { "id" })
public class TekstiHaku implements Serializable {
    private TekstihakuCtx ctx;
    private String kuvaus;
    private Kieli kieli;
    private String teksti;

    public static TekstiHaku of(TekstihakuCtx ctx, String kuvaus, Kieli kieli, String teksti) {
        TekstiHaku result = new TekstiHaku();
        result.ctx = ctx;
        result.kuvaus = kuvaus;
        result.kieli = kieli;
        result.teksti = teksti;
        return result;
    }

}
