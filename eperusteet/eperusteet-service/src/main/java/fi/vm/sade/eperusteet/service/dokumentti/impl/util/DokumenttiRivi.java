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
package fi.vm.sade.eperusteet.service.dokumentti.impl.util;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

/**
 * @author isaul
 */
@Setter
@Getter
public class DokumenttiRivi {

    private ArrayList<String> sarakkeet = new ArrayList<>();
    private Integer colspan = 1;
    private DokumenttiRiviTyyppi tyyppi = DokumenttiRiviTyyppi.DEFAULT;

    public void addSarake(String sarake) {
        sarakkeet.add(sarake);
    }

    public String getBackgroundColor() {
        switch (tyyppi) {
            case HEADER:
                return DokumenttiRiviBgColor.HEADER;
            case SUBHEADER:
                return DokumenttiRiviBgColor.SUBHEADER;
        }

        return DokumenttiRiviBgColor.DEFAULT;
    }

    public String getFontColor() {
        switch (tyyppi) {
            case HEADER:
                return DokumenttiRiviFontColor.HEADER;
        }

        return DokumenttiRiviFontColor.DEFAULT;
    }

    public String getElementType() {
        switch (tyyppi) {
            case HEADER:
                return "th";
        }

        return "td";
    }

    public String getElementAlign() {
        return "start";
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        sarakkeet.forEach((sarake) -> {
            builder.append(String.format("<%s colspan=\"%d\" align=\"%s\">", getElementType(), getColspan(), getElementAlign()));
            builder.append(sarake);
            builder.append(String.format("</%s>", getElementType()));
        });
        return builder.toString();
    }
}
