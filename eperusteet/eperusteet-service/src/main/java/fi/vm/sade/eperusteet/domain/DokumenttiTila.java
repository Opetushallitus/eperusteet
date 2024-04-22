package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DokumenttiTila {

    EI_OLE("ei_ole"),
    JONOSSA("jonossa"),
    LUODAAN("luodaan"),
    EPAONNISTUI("epaonnistui"),
    VALMIS("valmis");

    private final String tila;

    private DokumenttiTila(String tila) {
        this.tila = tila;
    }


    @Override
    public String toString() {
        return tila;
    }

    @JsonCreator
    public static DokumenttiTila of(String tila) {
        for (DokumenttiTila t : values()) {
            if (t.tila.equalsIgnoreCase(tila)) {
                return t;
            }
        }
        throw new IllegalArgumentException(tila + " ei ole kelvollinen dokumenttitila");
    }
}
