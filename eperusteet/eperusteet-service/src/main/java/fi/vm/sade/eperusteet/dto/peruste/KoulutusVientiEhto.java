package fi.vm.sade.eperusteet.dto.peruste;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KoulutusVientiEhto {
    TRUE("true"),
    FALSE("false"),
    KAIKKI("kaikki");

    private String value;

    public static KoulutusVientiEhto of(String tila) {
        for (KoulutusVientiEhto t : values()) {
            if (t.value.equalsIgnoreCase(tila)) {
                return t;
            }
        }
        throw new IllegalArgumentException(tila + " ei ole kelvollinen ehto");
    }
}
