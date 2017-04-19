/*
 * Copyright (c) 2013 The Finnish National Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.service.dokumentti.impl;

import fi.vm.sade.eperusteet.domain.Dokumentti;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteJulkinenDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.LocalizedMessagesService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.dokumentti.KVLiiteBuilderService;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.DokumenttiUtils;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.KVLiiteDokumentti;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * @author isaul
 */
@Service
public class KVLiiteBuilderServiceImpl implements KVLiiteBuilderService {

    private static final Logger LOG = LoggerFactory.getLogger(KVLiiteBuilderServiceImpl.class);
    private static final float COMPRESSION_LEVEL = 0.9f;

    @Autowired
    private PerusteService perusteService;

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private LocalizedMessagesService messages;

    @Override
    public Document generateXML(Peruste peruste, Dokumentti dto, Kieli kieli) throws ParserConfigurationException, IOException, TransformerException {
        // Luodaan uusi dokumentti
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        // Luodaan XHTML pohja
        Element rootElement = doc.createElement("html");
        rootElement.setAttribute("lang", kieli.toString());
        doc.appendChild(rootElement);

        // Head-elementti
        Element headElement = doc.createElement("head");
        rootElement.appendChild(headElement);
        // Poistetaan HEAD:in <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
        if (headElement.hasChildNodes()) {
            headElement.removeChild(headElement.getFirstChild());
        }

        // Body-elementti
        Element bodyElement = doc.createElement("body");
        rootElement.appendChild(bodyElement);

        // Apuolio dataan siirtelyyn
        KVLiiteDokumentti docBase = new KVLiiteDokumentti();
        docBase.setDocument(doc);
        docBase.setHeadElement(headElement);
        docBase.setBodyElement(bodyElement);
        docBase.setMapper(mapper);
        docBase.setKieli(kieli);
        docBase.setPeruste(peruste);
        docBase.setKvLiiteJulkinenDto(perusteService.getJulkinenKVLiite(peruste.getId()));

        // Rakennetaan varsinainen dokumentti
        addKVLiite(docBase);
        LOG.debug(DokumenttiUtils.printDocument(docBase.getDocument()).toString());

        return doc;
    }

    private void addKVLiite(KVLiiteDokumentti docBase) {
        addHeader(docBase);
        addTutkintoNimiSuomeksi(docBase);
        addTutkinnonNimiDokumentinKielella(docBase);
        addAmmatillinenOsaaminen(docBase);
    }

    private void addHeader(KVLiiteDokumentti docBase) {
        // Hieman lisää tilaa
        Element p = docBase.getDocument().createElement("p");
        docBase.getBodyElement().appendChild(p);

        // Lisätään taulukko
        Element table = docBase.getDocument().createElement("table");
        docBase.getBodyElement().appendChild(table);
        Element tr = docBase.getDocument().createElement("tr");
        table.appendChild(tr);

        // Lisätään Europass kuva
        Element europassTd = docBase.getDocument().createElement("td");
        tr.appendChild(europassTd);
        europassTd.setAttribute("colspan", "1");
        Element europassImg = docBase.getDocument().createElement("img");
        europassTd.appendChild(europassImg);
        europassImg.setAttribute("src", "kvliite/europass.jpg");

        // Lisätään teksti
        Element titleTd = docBase.getDocument().createElement("td");
        tr.appendChild(titleTd);
        Element titleHeader = docBase.getDocument().createElement("h1");
        titleTd.appendChild(titleHeader);
        titleTd.setAttribute("colspan", "6");
        titleTd.setAttribute("align", "center");
        titleHeader.setTextContent(messages.translate("docgen.kvliite.nimi", docBase.getKieli()).toUpperCase());

        // Lisätään Suomen lippu
        Element flagTd = docBase.getDocument().createElement("td");
        tr.appendChild(flagTd);
        flagTd.setAttribute("colspan", "1");
        flagTd.setAttribute("align", "right");
        Element flagImg = docBase.getDocument().createElement("img");
        flagTd.appendChild(flagImg);
        flagImg.setAttribute("src", "kvliite/fi.jpg");

    }

    private void addTutkintoNimiSuomeksi(KVLiiteDokumentti docBase) {
        // Lisätään taulukko
        Element table = docBase.getDocument().createElement("table");
        docBase.getBodyElement().appendChild(table);
        table.setAttribute("border", "1");

        // Lisätään tutkinnon nimi
        Element tr = docBase.getDocument().createElement("tr");
        table.appendChild(tr);
        tr.setAttribute("bgcolor", "#F2F2F2");
        Element th = docBase.getDocument().createElement("th");
        tr.appendChild(th);
        th.appendChild(DokumenttiUtils.newBoldElement(
                docBase.getDocument(), messages.translate("docgen.kvliite.tutkinnon-nimi", docBase.getKieli())));

        // Lisätään tutkinnon tiedot
        Element tr2 = docBase.getDocument().createElement("tr");
        table.appendChild(tr2);
        Element td = docBase.getDocument().createElement("td");
        tr2.appendChild(td);
        td.setAttribute("align", "center");

        // Nimi
        LokalisoituTekstiDto nimi = docBase.getKvLiiteJulkinenDto().getNimi();
        if (nimi != null && nimi.getTekstit() != null) {
            Element p = docBase.getDocument().createElement("p");
            td.appendChild(p);
            if (nimi.getTekstit().containsKey(Kieli.FI)) {
                Element perusteNimiEl = docBase.getDocument().createElement("strong");
                p.appendChild(perusteNimiEl);
                perusteNimiEl.appendChild(DokumenttiUtils.newItalicElement(docBase.getDocument(), nimi.get(Kieli.FI)));
                Element br = docBase.getDocument().createElement("br");
                p.appendChild(br);
            }

            if (nimi.getTekstit().containsKey(Kieli.SV)) {
                p.appendChild(DokumenttiUtils.newItalicElement(docBase.getDocument(),
                        nimi.get(Kieli.SV)));
            }
        }

        // Voimaantulopaiva ja diaari
        KVLiiteJulkinenDto kvLiiteJulkinenDto = docBase.getKvLiiteJulkinenDto();
        StringBuilder voimaantulopaivaJaDiaari = new StringBuilder();

        SimpleDateFormat dateFormat;
        if (docBase.getKieli().equals(Kieli.EN)) {
            dateFormat = new SimpleDateFormat("d MMMM yyyy");
        } else {
            dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        }

        if (kvLiiteJulkinenDto.getVoimassaoloAlkaa() != null) {
            voimaantulopaivaJaDiaari.append(messages
                    .translate("docgen.kvliite.tutkinnon-perusteiden-voimaantulopaiva", docBase.getKieli()));
            voimaantulopaivaJaDiaari.append(" ");
            voimaantulopaivaJaDiaari.append(dateFormat.format(kvLiiteJulkinenDto.getVoimassaoloAlkaa()));
            voimaantulopaivaJaDiaari.append(" ");
        }

        if (kvLiiteJulkinenDto.getDiaarinumero() != null) {
            voimaantulopaivaJaDiaari.append("(");
            voimaantulopaivaJaDiaari.append(kvLiiteJulkinenDto.getDiaarinumero());
            voimaantulopaivaJaDiaari.append(")");
        }

        td.appendChild(DokumenttiUtils.newItalicElement(docBase.getDocument(),
                voimaantulopaivaJaDiaari.toString()));
    }

    private void addTutkinnonNimiDokumentinKielella(KVLiiteDokumentti docBase) {
        KVLiiteJulkinenDto kvLiiteJulkinenDto = docBase.getKvLiiteJulkinenDto();

        // Jos dokumentin kieli ei ole suomi ja dokumentin kielellä löytyy nimi
        if (!docBase.getKieli().equals(Kieli.FI)
                && kvLiiteJulkinenDto.getNimi() != null
                && kvLiiteJulkinenDto.getNimi().getTekstit() != null
                && kvLiiteJulkinenDto.getNimi().getTekstit().containsKey(docBase.getKieli())) {
            // Lisätään taulukko
            Element table = docBase.getDocument().createElement("table");
            docBase.getBodyElement().appendChild(table);
            table.setAttribute("border", "1");

            // Lisätään tutkinnon nimi
            {
                Element tr = docBase.getDocument().createElement("tr");
                table.appendChild(tr);
                tr.setAttribute("bgcolor", "#F2F2F2");
                Element th = docBase.getDocument().createElement("th");
                tr.appendChild(th);
                th.appendChild(DokumenttiUtils.newBoldElement(docBase.getDocument(),
                        messages.translate("docgen.kvliite.tutkinnon-kaannetty-nimi", docBase.getKieli())));
            }

            // Lisätään tutkinnon nimelle rivi
            {
                Element tr = docBase.getDocument().createElement("tr");
                table.appendChild(tr);
                Element td = docBase.getDocument().createElement("td");
                tr.appendChild(td);
                td.setAttribute("align", "center");

                Element br = docBase.getDocument().createElement("br");
                td.appendChild(br);

                // Lisätään tutkinnon nimi dokumentin kielellä
                Element p = docBase.getDocument().createElement("p");
                td.appendChild(p);
                Element perusteNimiEl = docBase.getDocument().createElement("strong");
                p.appendChild(perusteNimiEl);
                perusteNimiEl.appendChild(DokumenttiUtils.newItalicElement(docBase.getDocument(),
                        kvLiiteJulkinenDto.getNimi().get(docBase.getKieli())));

                td.appendChild(br);
            }

            // Lisätään lailisuuskohta
            {
                Element tr = docBase.getDocument().createElement("tr");
                table.appendChild(tr);
                Element td = docBase.getDocument().createElement("td");
                tr.appendChild(td);
                td.setAttribute("align", "center");

                Element p = docBase.getDocument().createElement("p");
                td.appendChild(p);
                Element small = docBase.getDocument().createElement("small");
                p.appendChild(small);
                small.setTextContent(messages.translate("docgen.kvliite.kaanos-ei-ole-lainvoimainen", docBase.getKieli()));
            }
        }
    }

    private void addAmmatillinenOsaaminen(KVLiiteDokumentti docBase) {
        KVLiiteJulkinenDto kvLiiteJulkinenDto = docBase.getKvLiiteJulkinenDto();

        // Lisätään taulukko
        Element table = docBase.getDocument().createElement("table");
        docBase.getBodyElement().appendChild(table);
        table.setAttribute("border", "1");

        // Lisätään ammatillinen osaamisen otsikko
        {
            Element tr = docBase.getDocument().createElement("tr");
            table.appendChild(tr);
            tr.setAttribute("bgcolor", "#F2F2F2");
            Element th = docBase.getDocument().createElement("th");
            tr.appendChild(th);
            th.appendChild(DokumenttiUtils.newBoldElement(docBase.getDocument(),
                    messages.translate("docgen.kvliite.ammatillinen-osaaminen", docBase.getKieli())));
        }

        // Lisätään tutkinnon nimelle rivi
        {
            Element tr = docBase.getDocument().createElement("tr");
            table.appendChild(tr);
            Element td = docBase.getDocument().createElement("td");
            tr.appendChild(td);
            
            td.setTextContent("sisalto");
        }
    }
}
