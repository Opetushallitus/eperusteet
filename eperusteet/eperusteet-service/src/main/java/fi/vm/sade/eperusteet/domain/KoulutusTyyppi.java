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

/**
 *
 * @author nkala
 */
public enum KoulutusTyyppi {
    PERUSTUTKINTO("koulutustyyppi_1"),
    LUKIOKOULUTUS("koulutustyyppi_2"),
    TELMA("koulutustyyppi_5"),
    LISAOPETUS("koulutustyyppi_6"),
    AMMATTITUTKINTO("koulutustyyppi_11"),
    ERIKOISAMMATTITUTKINTO("koulutustyyppi_12"),
    AIKUISTENLUKIOKOULUTUS("koulutustyyppi_14"),
    ESIOPETUS("koulutustyyppi_15"),
    PERUSOPETUS("koulutustyyppi_16"),
    AIKUISTENPERUSOPETUS("koulutustyyppi_17"),
    VALMA("koulutustyyppi_18"),
    VARHAISKASVATUS("koulutustyyppi_20"),
    PERUSOPETUSVALMISTAVA("koulutustyyppi_22"),
    LUKIOVALMISTAVAKOULUTUS("koulutustyyppi_23"),
    TPO("koulutustyyppi_999907");

    private final String tyyppi;

    KoulutusTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    @Override
    public String toString() {
        return tyyppi;
    }

    @JsonCreator
    public static KoulutusTyyppi of(String tila) {
        for (KoulutusTyyppi s : values()) {
            if (s.tyyppi.equalsIgnoreCase(tila)) {
                return s;
            }
        }
        throw new fi.vm.sade.eperusteet.service.exception.IllegalArgumentException("tila-ei-ole-kelvollinen-tila",
                new HashMap<String, Object>(){{ put("tila", tila); }});
    }

    public boolean isOneOf(KoulutusTyyppi... tyypit) {
        for (KoulutusTyyppi toinen : tyypit) {
            if (toinen.toString().equals(this.tyyppi)) {
                return true;
            }
        }
        return false;
    }

    public boolean isValmaTelma() {
        return isOneOf(VALMA, TELMA);
    }

    public boolean isAmmatillinen() {
        return isOneOf(AMMATTITUTKINTO, ERIKOISAMMATTITUTKINTO, PERUSTUTKINTO);
    }
}
