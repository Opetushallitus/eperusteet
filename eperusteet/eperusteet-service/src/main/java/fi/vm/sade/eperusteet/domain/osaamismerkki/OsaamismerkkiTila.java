package fi.vm.sade.eperusteet.domain.osaamismerkki;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OsaamismerkkiTila {
    LAADINTA("laadinta"),
    JULKAISTU("julkaistu");

    private final String tila;

    OsaamismerkkiTila(String tila) {
        this.tila = tila;
    }

    @Override
    public String toString() {
        return tila;
    }

    @JsonCreator
    public static OsaamismerkkiTila of(String tila) {
        for (OsaamismerkkiTila s : values()) {
            if (s.tila.equalsIgnoreCase(tila)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tila + " ei ole kelvollinen OsaamismerkkiTila");
    }
}
