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

package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 *
 * @author harrik
 */
public enum TutkinnonOsaTyyppi {
    NORMAALI("normaali"),
    TUTKE2("tutke2"),
    REFORMI_TUTKE2("reformi_tutke2");
            
    private final String tyyppi;

    private TutkinnonOsaTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    @Override
    public String toString() {
        return tyyppi;
    }

    @JsonCreator
    public static TutkinnonOsaTyyppi of(String tyyppi) {
        for (TutkinnonOsaTyyppi s : values()) {
            if (s.tyyppi.equalsIgnoreCase(tyyppi)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tyyppi + " ei ole kelvollinen tyyppi");
    }
}
