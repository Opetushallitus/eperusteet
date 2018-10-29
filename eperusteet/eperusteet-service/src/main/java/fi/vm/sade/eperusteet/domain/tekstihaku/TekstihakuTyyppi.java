package fi.vm.sade.eperusteet.domain.tekstihaku;


import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum TekstihakuTyyppi {
    PERUSTEPROJEKTI("PERUSTEPROJEKTI"),
    PERUSTE("PERUSTE"),
    TEKSTIKAPPALE("TEKSTIKAPPALE"),
    OSAALUE("TUTKINNONOSA_OSAALUE"),
    ARVIOINTI("TUTKINNONOSA_ARVIOINTI"),
    TUTKINNONOSA("TUTKINNONOSA");

    private final String tila;

    TekstihakuTyyppi(String tila) {
        this.tila = tila;
    }

    static public Set<TekstihakuTyyppi> kaikki() {
        return new HashSet<>(Arrays.asList(TekstihakuTyyppi.values()));
    }

    @Override
    public String toString() {
        return tila;
    }

    @JsonCreator
    public static TekstihakuTyyppi of(String tila) {
        for (TekstihakuTyyppi s : values()) {
            if (s.tila.equalsIgnoreCase(tila)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tila + " ei ole kelvollinen tekstihaun tyyppi");
    }
}
