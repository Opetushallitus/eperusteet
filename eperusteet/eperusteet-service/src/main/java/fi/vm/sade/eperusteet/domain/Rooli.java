package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Rooli {
    OMISTAJA("omistaja"),
    SIHTEERI("sihteeri"),
    KOMMENTOIJA("kommentoija"),
    JASEN("jasen");

    private final String rooli;

    private Rooli(String rooli) {
        this.rooli = rooli;
    }


    @Override
    public String toString() {
        return rooli;
    }

    @JsonCreator
    public static Rooli of(String rooli) {
        for (Rooli r : values()) {
            if (r.rooli.equalsIgnoreCase(rooli)) {
                return r;
            }
        }
        throw new IllegalArgumentException(rooli + " ei ole kelvollinen rooli");
    }
}
