package fi.vm.sade.eperusteet.service.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public final class TiedoteUtil {
    private static final int VANHENEMISRAJA_KUUKAUSINA = 24;

    public static LocalDateTime vanhenemispaiva() {
        return LocalDateTime.now().minusMonths(VANHENEMISRAJA_KUUKAUSINA);
    }
}
