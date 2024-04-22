package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DokumenttiVirhe {

    EI_VIRHETTA("ei_virhetta"),
    PERUSTETTA_EI_LOYTYNYT("perustetta_ei_loytynyt"),
    TUNTEMATON("tuntematon"),
    TUNTEMATON_LOKALISOINTI("lokalisointiavainta_ei_loytynyt");

    private final String virhe;

    private DokumenttiVirhe(String virhe) {
        this.virhe = virhe;
    }

    @Override
    public String toString() {
        return virhe;
    }

    @JsonCreator
    public static DokumenttiVirhe of(String virhe) {
        for (DokumenttiVirhe t : values()) {
            if (t.virhe.equalsIgnoreCase(virhe)) {
                return t;
            }
        }
        throw new IllegalArgumentException(virhe + " ei ole kelvollinen virhekoodi");
    }
}
