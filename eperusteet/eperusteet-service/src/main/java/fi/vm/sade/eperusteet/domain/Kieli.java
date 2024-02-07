package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;

public enum Kieli {
    // käytetään ISO-639-1 kielikoodistoa

    FI("fi"), // suomi
    SV("sv"), // svenska
    SE("se"), // davvisámegiella (sámi), pohjoissaame (saame)
    RU("ru"), // русский язык, venäjä
    EN("en"); // english

    private final String koodi;

    Kieli(String koodi) {
        this.koodi = koodi;
    }

    @Override
    public String toString() {
        return koodi;
    }

    @JsonCreator
    public static Kieli of(String koodi) {
        for (Kieli k : values()) {
            if (k.koodi.equalsIgnoreCase(koodi)) {
                return k;
            }
        }
        throw new IllegalArgumentException(koodi + " ei ole kelvollinen kielikoodi");
    }

    public static List<String> vaihtoehdot() {
        List<String> kielet = new ArrayList<>();
        for (Kieli value : Kieli.values()) {
            kielet.add(value.koodi);
        }
        return kielet;
    }

    public static List<Kieli> kvliiteKielet() {
        // Mahdolliset kvliite kielet
        List<Kieli> kielet = new ArrayList<>();
        kielet.add(Kieli.FI);
        kielet.add(Kieli.SV);
        kielet.add(Kieli.EN);
        return kielet;
    }
}
