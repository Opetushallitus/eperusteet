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
/**
 *
 * @author harrik
 */
public enum Suoritustapakoodi {

    OPS("ops"),
    NAYTTO("naytto"),
    PERUSOPETUS("perusopetus"),
    LISAOPETUS("lisaopetus"),
    VARHAISKASVATUS("varhaiskasvatus"),
    ESIOPETUS("esiopetus"),
    LUKIO("lukio");

    private final String koodi;

    Suoritustapakoodi(String koodi) {
        this.koodi = koodi;
    }

    @Override
    public String toString() {
        return koodi;
    }

    @JsonCreator
    public static Suoritustapakoodi of(String koodi) {
        for (Suoritustapakoodi s : values()) {
            if (s.koodi.equalsIgnoreCase(koodi)) {
                return s;
            }
        }
        throw new IllegalArgumentException(koodi + " ei ole kelvollinen suoritustapakoodi");
    }
}
