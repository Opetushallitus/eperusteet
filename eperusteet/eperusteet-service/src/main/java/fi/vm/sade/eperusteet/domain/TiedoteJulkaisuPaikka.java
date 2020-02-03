package fi.vm.sade.eperusteet.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TiedoteJulkaisuPaikka {
    OPINTOPOLKU("opintopolku"),
    OPINTOPOLKU_ETUSIVU("opintopolku_etusivu"),
    OPS("ops"),
    AMOSAA("amosaa");

    private String paikka;

    @Override
    public String toString() {
        return paikka;
    }
}
