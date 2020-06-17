package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OsaAlueTyyppi {
    OSAALUE2014("osaalue2014"),
    OSAALUE2020("osaalue2020");

    private final String tyyppi;

    private OsaAlueTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    @Override
    public String toString() {
        return tyyppi;
    }

    @JsonCreator
    public static OsaAlueTyyppi of(String tyyppi) {
        for (OsaAlueTyyppi s : values()) {
            if (s.tyyppi.equalsIgnoreCase(tyyppi)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tyyppi + " ei ole kelvollinen tyyppi");
    }
}
