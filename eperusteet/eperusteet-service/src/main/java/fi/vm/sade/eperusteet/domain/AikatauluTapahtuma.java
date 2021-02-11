package fi.vm.sade.eperusteet.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString(of = "tapahtuma")
@Getter
@AllArgsConstructor
public enum AikatauluTapahtuma {

    LUOMINEN("luominen"),
    LAUSUNTOKIERROS("lausuntokierros"),
    JOHTOKUNNANKASITTELY("johtokunnankasittely"),
    ARVIOITUJULKAISUPAIVA("arvioitujulkaisupaiva"),
    TAVOITE("tavoite"),
    JULKAISU("julkaisu");

    private String tapahtuma;

    @Override
    public String toString() {
        return tapahtuma;
    }
}
