package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.experimental.UtilityClass;

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
            NavigationType.koto_opinto,
            NavigationType.kaantajataito,
            NavigationType.kaantajataitotasoasteikko,
            NavigationType.kaantajakielitaito,
            NavigationType.kaantajataitotasokuvaus,
            NavigationType.kaantajaaihealue,
            NavigationType.kaantajatodistusmalli);

    private static final Set<PerusteTyyppi> NUMEROITAVAT_PERUSTE_TYYPIT = Set.of(
            PerusteTyyppi.NORMAALI,
            PerusteTyyppi.KIELI_KAANTAJA_TUTKINTO);

    public static NavigationNodeDto asetaNumerointi(Peruste peruste, NavigationNodeDto node) {
        if (!peruste.getToteutus().equals(KoulutustyyppiToteutus.AMMATILLINEN) && NUMEROITAVAT_PERUSTE_TYYPIT.contains(peruste.getTyyppi())) {
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
