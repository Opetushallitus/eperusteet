package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TiedoteJulkaisuPaikka {
    OPINTOPOLKU("opintopolku"),
    OPINTOPOLKU_ETUSIVU("opintopolku_etusivu"),
    OPS("ops"),
    LOPS("lops"),
    AMOSAA("amosaa");

    private String paikka;

    @Override
    public String toString() {
        return paikka;
    }

    @JsonCreator
    public static TiedoteJulkaisuPaikka of(String paikka) {
        for (TiedoteJulkaisuPaikka s : values()) {
            if (s.paikka.equalsIgnoreCase(paikka)) {
                return s;
            }
        }
        throw new IllegalArgumentException(paikka + " ei ole kelvollinen TiedoteJulkaisuPaikka");
    }
}
