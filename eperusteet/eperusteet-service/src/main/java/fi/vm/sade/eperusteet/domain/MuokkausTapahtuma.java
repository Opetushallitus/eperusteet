package fi.vm.sade.eperusteet.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MuokkausTapahtuma {
    LUONTI("luonti"),
    PAIVITYS("paivitys"),
    PALAUTUS("palautus"),
    JARJESTETTY("jarjestetty"),
    KOPIOINTI("kopiointi"),
    POISTO("poisto");

    private String tapahtuma;

    @Override
    public String toString() {
        return tapahtuma;
    }
}
