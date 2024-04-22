package fi.vm.sade.eperusteet.domain.tutkinnonrakenne;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum RakenneModuuliErikoisuus {
    VIERAS("vieras");

    private final String erikoisuus;

    private RakenneModuuliErikoisuus(String rooli) {
        this.erikoisuus = rooli;
    }

    @Override
    public String toString() {
        return erikoisuus;
    }

    @JsonCreator
    public static RakenneModuuliErikoisuus of(String x) {
        for (RakenneModuuliErikoisuus r : values()) {
            if (r.erikoisuus.equalsIgnoreCase(x)) {
                return r;
            }
        }
        throw new IllegalArgumentException(x + " ei ole kelvollinen tyyppi");
    }
}
