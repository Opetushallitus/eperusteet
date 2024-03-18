package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OpasTyyppi {
    NORMAALI("normaali"),
    TIETOAPALVELUSTA("tietoapalvelusta");

    private final String tyyppi;

    @Override
    public String toString() {
        return tyyppi;
    }

    @JsonCreator
    public static OpasTyyppi of(String tyyppi) {
        for (OpasTyyppi s : values()) {
            if (s.tyyppi.equalsIgnoreCase(tyyppi)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tyyppi + " ei ole kelvollinen OpasTyyppi");
    }
}
