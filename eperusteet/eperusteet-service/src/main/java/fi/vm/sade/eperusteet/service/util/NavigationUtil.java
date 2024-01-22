package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class NavigationUtil {

    private static final Set<NavigationType> NUMEROITAVAT_TYYPIT = Set.of(
            NavigationType.linkkisivu,
            NavigationType.viite,
            NavigationType.opintokokonaisuus,
            NavigationType.koulutuksenosa,
            NavigationType.koto_laajaalainenosaaminen,
            NavigationType.koto_kielitaitotaso,
            NavigationType.koto_opinto);

    public static NavigationNodeDto asetaNumerointi(Peruste peruste, NavigationNodeDto node) {
        if (!peruste.getToteutus().equals(KoulutustyyppiToteutus.AMMATILLINEN) && peruste.getTyyppi().equals(PerusteTyyppi.NORMAALI)) {
            asetaNumerointi(node.getChildren(), "");
        }
        return node;
    }

    public static void asetaNumerointi(List<NavigationNodeDto> nodes, String taso) {
        AtomicInteger nro = new AtomicInteger(0);
        nodes.stream()
                .filter(node -> NUMEROITAVAT_TYYPIT.contains(node.getType()))
                .forEach(node -> {
                    node.meta("numerointi", taso + nro.incrementAndGet());
                    asetaNumerointi(node.getChildren(), taso + nro.get() + ".");
                });
    }
}
