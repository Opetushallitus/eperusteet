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

import java.util.HashMap;

public enum JarjestysTapa {
    NIMI("nimi"),
    TILA("tila"),
    LUOTU("luotu"),
    MUOKATTU("muokattu");

    private final String tapa;

    JarjestysTapa(String tapa) {
        this.tapa = tapa;
    }

    @Override
    public String toString() {
        return tapa;
    }

    @JsonCreator
    public static JarjestysTapa of(String tapa) {
        for (JarjestysTapa s : values()) {
            if (s.tapa.equalsIgnoreCase(tapa)) {
                return s;
            }
        }
        throw new fi.vm.sade.eperusteet.service.exception.IllegalArgumentException("tapa-ei-ole-kelvollinen-jarjestystapa",
                new HashMap<String, Object>(){{ put("tapa", tapa); }});
    }
}
