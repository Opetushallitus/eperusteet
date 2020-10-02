package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.StructurallyComparable;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Iterator;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@UtilityClass
public class PerusteUtils {
    static public KoulutustyyppiToteutus getToteutus(KoulutustyyppiToteutus toteutus, String koulutustyyppi, PerusteTyyppi tyyppi) {
        KoulutusTyyppi kt = koulutustyyppi == null ? null : KoulutusTyyppi.of(koulutustyyppi);
        return getToteutus(toteutus, kt, tyyppi);
    }

    static public <T extends StructurallyComparable<T>> boolean nestedStructureEquals(Collection<T> a, Collection<T> b) {
        return nestedStructureEquals(a, b, false);
    }

    static public <T extends StructurallyComparable<T>> boolean nestedStructureEquals(Collection<T> a, Collection<T> b, boolean isOrderFree) {
        boolean result = refXnor(a, b);
        if (a != null && b != null) {
            if (a.size() != b.size()) {
                return false;
            }
            else {
                if (isOrderFree) {
                    for (T bv : b) {
                        result &= a.stream().anyMatch(ab -> ab.structureEquals(bv));
                    }
                }
                else {
                    Iterator<T> aiter = a.iterator();
                    Iterator<T> biter = b.iterator();
                    while (aiter.hasNext()) {
                        T av = aiter.next();
                        T bv = biter.next();
                        if (!av.structureEquals(bv)) {
                            return false;
                        }
                    }
                }
            }
        }
        return result;
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
                } else if (kt == KoulutusTyyppi.PERUSOPETUS) {
                    return KoulutustyyppiToteutus.PERUSOPETUS;
                } else if (kt == KoulutusTyyppi.LUKIOKOULUTUS || kt == KoulutusTyyppi.AIKUISTENLUKIOKOULUTUS) {
                    return KoulutustyyppiToteutus.LOPS;
                } else if (kt == KoulutusTyyppi.TPO) {
                    return KoulutustyyppiToteutus.TPO;
                } else if (kt == KoulutusTyyppi.VAPAASIVISTYSTYO) {
                    return KoulutustyyppiToteutus.VAPAASIVISTYSTYO;
                }
            }
            return KoulutustyyppiToteutus.YKSINKERTAINEN;
        }
    }
}
