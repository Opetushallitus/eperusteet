package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PerusteTila {
    LUONNOS("luonnos"),
    VALMIS("valmis"),
    POISTETTU("poistettu");

    private final String tila;

    private PerusteTila(String tila) {
        this.tila = tila;
    }

    @Override
    public String toString() {
        return tila;
    }

    @JsonCreator
    public static PerusteTila of(String tila) {
        for (PerusteTila s : values()) {
            if (s.tila.equalsIgnoreCase(tila)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tila + " ei ole kelvollinen PerusteTila");
    }
}
