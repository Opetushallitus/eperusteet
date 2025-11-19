package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PerusteTyyppi {

    NORMAALI("normaali"),
    OPAS("opas"),
    AMOSAA_YHTEINEN("amosaayhteinen"),
    POHJA("pohja"),
    DIGITAALINEN_OSAAMINEN("digitaalinen_osaaminen"),
    KIELI_KAANTAJA_TUTKINTO("kieli_kaantaja_tutkinto");

    private final String tila;

    private PerusteTyyppi(String tila) {
        this.tila = tila;
    }

    @Override
    public String toString() {
        return tila;
    }

    @JsonCreator
    public static PerusteTyyppi of(String tila) {
        for (PerusteTyyppi s : values()) {
            if (s.tila.equalsIgnoreCase(tila)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tila + " ei ole kelvollinen PerusteTyyppi");
    }

    public boolean isOneOf(PerusteTyyppi... tyypit) {
        for (PerusteTyyppi toinen : tyypit) {
            if (toinen.toString().equals(this.tila)) {
                return true;
            }
        }
        return false;
    }

}
