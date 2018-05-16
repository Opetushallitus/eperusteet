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
package fi.vm.sade.eperusteet.domain.tutkinnonrakenne;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;

/**
 *
 * @author nkala
 */
public enum RakenneModuuliErikoisuus {
    VIERAS("vieras");

    private final String erikoisuus;

    private RakenneModuuliErikoisuus(String rooli) {
        this.erikoisuus = rooli;
    }

    @Override
    public String toString() {
        return erikoisuus;
    }

    @JsonCreator
    public static RakenneModuuliErikoisuus of(String erikoisuus) {
        for (RakenneModuuliErikoisuus r : values()) {
            if (r.erikoisuus.equalsIgnoreCase(erikoisuus)) {
                return r;
            }
        }
        throw new fi.vm.sade.eperusteet.service.exception.IllegalArgumentException("erikoisuus-ei-ole-kelvollinen-erikoisuus",
                new HashMap<String, Object>(){{ put("erikoisuus", erikoisuus); }});
    }
}
