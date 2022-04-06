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

import java.util.Arrays;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.ArrayList;

/**
 * @author isaul
 */
public class DokumenttiTaulukko {

    private String otsikko;
    private ArrayList<DokumenttiRivi> rivit = new ArrayList<>();

    public void addOtsikko(String otsikko) {
        this.otsikko = otsikko;
    }

    public void addOtsikkosarakkeet(String... sarakkeet) {
        DokumenttiRivi otsikkoRivi = new DokumenttiRivi();
        Arrays.stream(sarakkeet).forEach(otsikkoRivi::addSarake);
        otsikkoRivi.setTyyppi(DokumenttiRiviTyyppi.HEADER);
        addRivi(otsikkoRivi);
    }

    public void addRivi(DokumenttiRivi rivi) {
        rivit.add(rivi);
    }

    public void addToDokumentti(DokumenttiBase docBase) {
        if (rivit.size() > 0) {
            Document tempDoc = new W3CDom().fromJsoup(Jsoup.parseBodyFragment(this.toString()));
            Node node = tempDoc.getDocumentElement().getChildNodes().item(1).getFirstChild();
            docBase.getBodyElement().appendChild(docBase.getDocument().importNode(node, true));
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<div>");

        if (otsikko != null) {
            builder.append("<strong>");
            builder.append(otsikko);
            builder.append("</strong>");
        }

        builder.append("<table border=\"1\">");

        rivit.forEach((rivi) -> {
            builder.append(String.format("<tr bgcolor=\"%s\" fontcolor=\"%s\">", rivi.getBackgroundColor(), rivi.getFontColor()));
            builder.append(rivi.toString());
            builder.append("</tr>");
        });

        builder.append("</table>");

        builder.append("</div>");
        return builder.toString();
    }
}
