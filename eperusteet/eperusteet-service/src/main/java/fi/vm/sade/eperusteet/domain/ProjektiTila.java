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

import java.util.*;
import java.util.stream.Stream;

/**
 * @author harrik
 */
public enum ProjektiTila {
    POISTETTU("poistettu") {
        @Override
        public List<ProjektiTila> mahdollisetTilat(PerusteTyyppi tyyppi) {
            return Collections.singletonList(LAADINTA);
        }
    },
    LAADINTA("laadinta") {
        @Override
        public List<ProjektiTila> mahdollisetTilat(PerusteTyyppi tyyppi) {
            if (tyyppi.equals(PerusteTyyppi.POHJA)) {
                return Arrays.asList(VALMIS, POISTETTU);
            } else {
                return Arrays.asList(VIIMEISTELY, POISTETTU);
            }
        }
    },
    VIIMEISTELY("viimeistely") {
        @Override
        public List<ProjektiTila> mahdollisetTilat(PerusteTyyppi tyyppi) {
            return Arrays.asList(KAANNOS, LAADINTA);
        }
    },
    KAANNOS("kaannos") {
        @Override
        public List<ProjektiTila> mahdollisetTilat(PerusteTyyppi tyyppi) {
            return Arrays.asList(VALMIS, VIIMEISTELY, LAADINTA);
        }
    },
    VALMIS("valmis") {
        @Override
        public List<ProjektiTila> mahdollisetTilat(PerusteTyyppi tyyppi) {
            if (tyyppi.equals(PerusteTyyppi.POHJA)) {
                return Collections.singletonList(LAADINTA);
            } else {
                return Arrays.asList(JULKAISTU, KAANNOS, VIIMEISTELY, LAADINTA);
            }
        }
    },
    JULKAISTU("julkaistu") {
        @Override
        public List<ProjektiTila> mahdollisetTilat(PerusteTyyppi tyyppi) {
            // EP-1387
            if (tyyppi.equals(PerusteTyyppi.OPAS)) {
                return Collections.singletonList(LAADINTA);
            } else {
                return Collections.emptyList();
            }
        }
    };

    public static ProjektiTila[] jalkeen(ProjektiTila tila) {
        return Stream.of(ProjektiTila.values()).filter(t -> t.ordinal() > tila.ordinal()).toArray(ProjektiTila[]::new);
    }

    private final String tila;

    ProjektiTila(String tila) {
        this.tila = tila;
    }

    @Override
    public String toString() {
        return tila;
    }

    @JsonCreator
    public static ProjektiTila of(String tila) {
        for (ProjektiTila s : values()) {
            if (s.tila.equalsIgnoreCase(tila)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tila + " ei ole kelvollinen ProjektiTila");
    }

    public List<ProjektiTila> mahdollisetTilat(PerusteTyyppi tyyppi) {
        return new ArrayList<>();
    }

    public boolean isOneOf(ProjektiTila... tilat) {
        for (ProjektiTila toinen : tilat) {
            if (toinen.toString().equals(this.tila)) {
                return true;
            }
        }
        return false;
    }
}
