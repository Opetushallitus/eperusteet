package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PerusteUtils {
    static public KoulutustyyppiToteutus getToteutus(KoulutustyyppiToteutus toteutus, String koulutustyyppi, PerusteTyyppi tyyppi) {
        KoulutusTyyppi kt = koulutustyyppi == null ? null : KoulutusTyyppi.of(koulutustyyppi);
        return getToteutus(toteutus, kt, tyyppi);
    }

    static public KoulutustyyppiToteutus getToteutus(KoulutustyyppiToteutus toteutus, KoulutusTyyppi kt, PerusteTyyppi tyyppi) {
        if (toteutus != null) {
            return toteutus;
        }
        else if (tyyppi == PerusteTyyppi.OPAS) {
            return KoulutustyyppiToteutus.YKSINKERTAINEN;
        }
        else {
            if (kt != null) {
                if (kt.isAmmatillinen()) {
                    return KoulutustyyppiToteutus.AMMATILLINEN;
                }
                else if (kt == KoulutusTyyppi.PERUSOPETUS) {
                    return KoulutustyyppiToteutus.PERUSOPETUS;
                }
                else if (kt == KoulutusTyyppi.LUKIOKOULUTUS) {
                    return KoulutustyyppiToteutus.LOPS;
                }
            }
            return KoulutustyyppiToteutus.YKSINKERTAINEN;
        }
    }
}
