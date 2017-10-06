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
 * @author jussi
 */
public enum DokumenttiVirhe {

    EI_VIRHETTA("ei_virhetta"),
    PERUSTETTA_EI_LOYTYNYT("perustetta_ei_loytynyt"),
    TUNTEMATON("tuntematon"),
    TUNTEMATON_LOKALISOINTI("lokalisointiavainta_ei_loytynyt");

    private final String virhe;

    private DokumenttiVirhe(String virhe) {
        this.virhe = virhe;
    }

    @Override
    public String toString() {
        return virhe;
    }

    @JsonCreator
    public static DokumenttiVirhe of(String virhe) {
        for (DokumenttiVirhe t : values()) {
            if (t.virhe.equalsIgnoreCase(virhe)) {
                return t;
            }
        }
        throw new IllegalArgumentException(virhe + " ei ole kelvollinen virhekoodi");
    }
}
