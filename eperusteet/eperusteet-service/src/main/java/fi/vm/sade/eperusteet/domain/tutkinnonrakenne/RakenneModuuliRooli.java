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

/**
 * @author nkala
 */
public enum RakenneModuuliRooli {
    NORMAALI("määritelty"),
    VIRTUAALINEN("määrittelemätön"),
    OSAAMISALA("osaamisala"),
    VIERAS("vieras");

    private final String rooli;

    private RakenneModuuliRooli(String rooli) {
        this.rooli = rooli;
    }

    @Override
    public String toString() {
        return rooli;
    }

    @JsonCreator
    public static RakenneModuuliRooli of(String x) {
        for (RakenneModuuliRooli r : values()) {
            if (r.rooli.equalsIgnoreCase(x)) {
                return r;
            }
        }
        throw new IllegalArgumentException(x + " ei ole kelvollinen tyyppi");
    }
}
