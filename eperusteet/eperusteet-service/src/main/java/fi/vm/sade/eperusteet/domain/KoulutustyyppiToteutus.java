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
import lombok.EqualsAndHashCode;

/**
 * @author nkala
 *
 * Koulutustyyppi ei enää yksilöi toteutusta ja toteutus voi olla jaettu eri koulutustyyppien välillä.
 *
 */

public enum KoulutustyyppiToteutus {
    YKSINKERTAINEN("yksinkertainen"), // Sisältää ainoastaan tekstikappaleita
    PERUSOPETUS("perusopetus"),
    LOPS("lops"),
    AMMATILLINEN("ammatillinen"),
    TPO("taiteenperusopetus"),
    LOPS2019("lops2019");

    private final String tyyppi;

    KoulutustyyppiToteutus(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    @JsonCreator
    public static KoulutustyyppiToteutus of(String tila) {
        for (KoulutustyyppiToteutus s : values()) {
            if (s.tyyppi.equalsIgnoreCase(tila)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tila + " ei ole kelvollinen toteutus");
    }

    @Override
    public String toString() {
        return tyyppi;
    }

}
