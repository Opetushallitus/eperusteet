package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PoistetunTyyppi {
    TEKSTIKAPPALE("tekstikappale");

    private final String tyyppi;

    PoistetunTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    @Override
    public String toString() {
        return tyyppi;
    }

    @JsonCreator
    public static PoistetunTyyppi of(String tyyppi) {
        for (PoistetunTyyppi s : values()) {
            if (s.tyyppi.equalsIgnoreCase(tyyppi)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tyyppi + " ei ole kelvollinen tyyppi");
    }
}
