package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum KoulutusOsanTyyppi {
    YHTEINEN("yhteinen"),
    VALINNAINEN("valinnainen");

    private final String tyyppi;

    KoulutusOsanTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    @Override
    public String toString() {
        return tyyppi;
    }

    @JsonCreator
    public static KoulutusOsanTyyppi of(String tyyppi) {
        for (KoulutusOsanTyyppi s : values()) {
            if (s.tyyppi.equalsIgnoreCase(tyyppi)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tyyppi + " ei ole kelvollinen KoulutusOsanTyyppi");
    }
}
