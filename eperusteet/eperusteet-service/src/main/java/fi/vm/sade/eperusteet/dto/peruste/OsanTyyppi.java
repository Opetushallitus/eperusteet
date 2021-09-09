package fi.vm.sade.eperusteet.dto.peruste;

import com.fasterxml.jackson.annotation.JsonCreator;
import fi.vm.sade.eperusteet.domain.DokumenttiVirhe;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;

public enum OsanTyyppi {
    LAAJAALAINENOSAAMINEN("laajaalainenosaaminen"),
    KOTO_KIELITAITOTASO("koto_kielitaitotaso"),
    TAVOITESISALTOALUE("tavoitesisaltoalue"),
    KOULUTUKSENOSA("koulutuksenosa"),
    OPINTOKOKONAISUUS("opintokokonaisuus"),
    KOTO_OPINTO("koto_opinto"),
    RAKENNE("rakenne"),
    TAITEENALA("taiteenala"),
    TUTKINNONOSA("tutkinnonosa"),
    OPETUKSENYLEISETTAVOITTEET("opetuksenyleisettavoitteet"),
    AIHEKOKONAISUUDET("aihekokonaisuudet"),
    TEKSTIKAPPALE("tekstikappale");

    private final String tyyppi;

    OsanTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    @Override
    public String toString() {
        return tyyppi;
    }

    @JsonCreator
    public static OsanTyyppi of(String tila) {
        for (OsanTyyppi s : values()) {
            if (s.tyyppi.equalsIgnoreCase(tila)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tila + " ei ole kelvollinen OsanTyyppi");
    }

}
