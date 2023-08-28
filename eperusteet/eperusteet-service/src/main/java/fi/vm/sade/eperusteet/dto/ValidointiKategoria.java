package fi.vm.sade.eperusteet.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ValidointiKategoria {
    MAARITTELEMATON("maarittelematon"),
    TEKSTI("teksti"),
    KIELISISALTO("kielisisalto"),
    PERUSTE("peruste"),
    RAKENNE("rakenne"),
    KOODISTO("koodisto");

    private final String tyyppi;

    @Override
    public String toString() {
        return tyyppi;
    }

    @JsonCreator
    public static ValidointiKategoria of(String tila) {
        for (ValidointiKategoria s : values()) {
            if (s.tyyppi.equalsIgnoreCase(tila)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tila + " ei ole kelvollinen validointikategoria");
    }
}
