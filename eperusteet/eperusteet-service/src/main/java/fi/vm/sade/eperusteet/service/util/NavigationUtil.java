package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class NavigationUtil {

    public static NavigationNodeDto asetaNumerointi(NavigationNodeDto node) {
        asetaNumerointi(node.getChildren(), "");
        return node;
    }

    public static void asetaNumerointi(List<NavigationNodeDto> nodes, String taso) {
        AtomicInteger nro = new AtomicInteger(0);
        nodes.stream()
                .filter(node -> node.getType().equals(NavigationType.viite))
                .forEach(node -> {
                    node.meta("numerointi", taso + nro.incrementAndGet());
                    asetaNumerointi(node.getChildren(), taso + nro.get() + ".");
                });
    }
}
