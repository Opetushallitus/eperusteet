package fi.vm.sade.eperusteet.domain.tutkinnonrakenne;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum RakenneModuuliRooli {
    NORMAALI("määritelty"),
    VIRTUAALINEN("määrittelemätön"),
    OSAAMISALA("osaamisala"),
    TUTKINTONIMIKE("tutkintonimike"),
    VIERAS("vieras");

    private final String rooli;

    private RakenneModuuliRooli(String rooli) {
        this.rooli = rooli;
    }

    @Override
    public String toString() {
        return rooli;
    }

    @JsonCreator
    public static RakenneModuuliRooli of(String x) {
        for (RakenneModuuliRooli r : values()) {
            if (r.rooli.equalsIgnoreCase(x)) {
                return r;
            }
        }
        throw new IllegalArgumentException(x + " ei ole kelvollinen tyyppi");
    }
}
