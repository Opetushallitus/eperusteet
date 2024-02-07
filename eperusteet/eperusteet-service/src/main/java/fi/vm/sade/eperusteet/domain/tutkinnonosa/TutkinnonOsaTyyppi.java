package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TutkinnonOsaTyyppi {
    NORMAALI("normaali"),
    TUTKE2("tutke2"),
    REFORMI_TUTKE2("reformi_tutke2");

    private final String tyyppi;

    private TutkinnonOsaTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    @Override
    public String toString() {
        return tyyppi;
    }

    public static boolean isTutke(TutkinnonOsaTyyppi tyyppi) {
        return tyyppi.equals(TutkinnonOsaTyyppi.TUTKE2)
                || tyyppi.equals(TutkinnonOsaTyyppi.REFORMI_TUTKE2);
    }

    @JsonCreator
    public static TutkinnonOsaTyyppi of(String tyyppi) {
        for (TutkinnonOsaTyyppi s : values()) {
            if (s.tyyppi.equalsIgnoreCase(tyyppi)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tyyppi + " ei ole kelvollinen tyyppi");
    }
}
