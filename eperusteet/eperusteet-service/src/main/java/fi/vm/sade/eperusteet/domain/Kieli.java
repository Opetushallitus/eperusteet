/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author jhyoty
 */
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
        throw new fi.vm.sade.eperusteet.service.exception.IllegalArgumentException("koodi-ei-ole-kelvollinen-kielikoodi",
                new HashMap<String, Object>(){{ put("koodi", koodi); }});
    }

    public static List<String> vaihtoehdot() {
        List<String> kielet = new ArrayList<>();
        for (Kieli value : Kieli.values()) {
            kielet.add(value.koodi);
        }
        return kielet;
    }
}
