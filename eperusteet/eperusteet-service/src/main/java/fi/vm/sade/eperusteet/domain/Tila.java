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
import java.util.EnumSet;
import java.util.Set;

/**
 *
 * @author harrik
 */
public enum Tila {

    POISTETTU("poistettu") {
        @Override
        public Set<Tila> mahdollisetTilat() {
            return EnumSet.of(LUONNOS, LAADINTA);
        }
    },
    LUONNOS("luonnos") {
        @Override
        public Set<Tila> mahdollisetTilat() {
            return EnumSet.of(POISTETTU);
        }
    },
    LAADINTA("laadinta") {
        @Override
        public Set<Tila> mahdollisetTilat() {
            return EnumSet.of(KOMMENTOINTI, VIIMEISTELY, POISTETTU);
        }
    },
    KOMMENTOINTI("kommentointi"){
        @Override
        public Set<Tila> mahdollisetTilat() {
            return EnumSet.of(LAADINTA, POISTETTU);
        }
    },
    VIIMEISTELY("viimeistely"){
        @Override
        public Set<Tila> mahdollisetTilat() {
            return EnumSet.of(LAADINTA, VALMIS, POISTETTU);
        }
    },
    VALMIS("valmis"){
        @Override
        public Set<Tila> mahdollisetTilat() {
            return EnumSet.of(VIIMEISTELY, JULKAISTU, POISTETTU);
        }
    },
    JULKAISTU("julkaistu");


    private final String tila;

    private Tila(String tila) {
        this.tila = tila;
    }

    @Override
    public String toString() {
        return tila;
    }

    @JsonCreator
    public static Tila of(String tila) {
        for (Tila s : values()) {
            if (s.tila.equalsIgnoreCase(tila)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tila + " ei ole kelvollinen tila");
    }

    public Set<Tila> mahdollisetTilat() {
        return EnumSet.noneOf(Tila.class);
    }
}
