package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum JarjestysTapa {
    NIMI("nimi"),
    TILA("tila"),
    LUOTU("luotu"),
    MUOKATTU("muokattu"),
    PERUSTE_VOIMASSAOLO_ALKAA("peruste.voimassaoloAlkaa"),
    PERUSTE_VOIMASSAOLO_LOPPUU("peruste.voimassaoloLoppuu"),
    PERUSTE_PAATOSPVM("peruste.paatospvm"),
    KOULUTUSTYYPPI("koulutustyyppi");

    private final String tapa;

    JarjestysTapa(String tapa) {
        this.tapa = tapa;
    }

    @Override
    public String toString() {
        return tapa;
    }

    @JsonCreator
    public static JarjestysTapa of(String tapa) {
        for (JarjestysTapa s : values()) {
            if (s.tapa.equalsIgnoreCase(tapa)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tapa + " ei ole kelvollinen järjestystapa");
    }
}
