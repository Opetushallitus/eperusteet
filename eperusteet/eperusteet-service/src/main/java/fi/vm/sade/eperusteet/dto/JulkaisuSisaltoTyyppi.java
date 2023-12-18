package fi.vm.sade.eperusteet.dto;

public enum JulkaisuSisaltoTyyppi {
    PERUSTE,
    TUTKINNONOSA,
    KAIKKI;

    public static JulkaisuSisaltoTyyppi of(boolean perusteet, boolean tutkinnonosat) {
        if (perusteet && !tutkinnonosat) {
            return PERUSTE;
        }

        if (!perusteet && tutkinnonosat) {
            return TUTKINNONOSA;
        }

        return KAIKKI;
    }
}
