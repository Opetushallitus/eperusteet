package fi.vm.sade.eperusteet.domain.tuva;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum KoulutusOsanKoulutustyyppi {

    TUTKINTOKOULUTUKSEENVALMENTAVA("tutkintokoulutukseenvalmentava"),
    PERUSOPETUS("perusopetus"),
    LUKIOKOULUTUS("lukiokoulutus"),
    AMMATILLINENKOULUTUS("ammatillinenkoulutus");

    private final String tyyppi;

    KoulutusOsanKoulutustyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    @Override
    public String toString() {
        return tyyppi;
    }

    @JsonCreator
    public static KoulutusOsanKoulutustyyppi of(String tyyppi) {
        for (KoulutusOsanKoulutustyyppi s : values()) {
            if (s.tyyppi.equalsIgnoreCase(tyyppi)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tyyppi + " ei ole kelvollinen KoulutusOsanKoulutustyyppi");
    }
}
