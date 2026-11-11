package fi.vm.sade.eperusteet.service.util;

import java.time.LocalDateTime;

public final class TiedoteUtil {
    private static final int VANHENEMISRAJA_KUUKAUSINA = 24;

    private TiedoteUtil() {
    }

    public static LocalDateTime vanhenemispaiva() {
        return LocalDateTime.now().minusMonths(VANHENEMISRAJA_KUUKAUSINA);
    }
}
