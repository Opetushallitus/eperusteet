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
package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Arviointi.ArvioinninKohde;
import fi.vm.sade.eperusteet.domain.Arviointi.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.Arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.OsaamistasonKriteeri;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.service.internal.DokumenttiBuilderService;
import fi.vm.sade.eperusteet.service.LocalizedMessagesService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author jussi
 */
@Service
public class DokumenttiBuilderServiceImpl implements DokumenttiBuilderService {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiBuilderServiceImpl.class);

    @Autowired
    private LocalizedMessagesService messages;

    @Override
    public String generateXML(Peruste peruste, Kieli kieli) throws
            TransformerConfigurationException,
            IOException,
            TransformerException,
            ParserConfigurationException
    {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("book");
        rootElement.setAttribute("xmlns", "http://docbook.org/ns/docbook");
        rootElement.setAttribute("xmlns:h", "http://www.w3.org/1999/xhtml");
        doc.appendChild(rootElement);

        String nimi = getTextString(peruste.getNimi(), kieli);
        Element titleElement = doc.createElement("title");
        titleElement.appendChild(doc.createTextNode(nimi));
        rootElement.appendChild(titleElement);

        //rootElement.appendChild(doc.createElement("info"));

        // TODO: should we process suoritustavat in some specific order?
        for (Suoritustapa st : peruste.getSuoritustavat()) {
            PerusteenOsaViite sisalto = peruste.getSuoritustapa(st.getSuoritustapakoodi()).getSisalto();
            addSisaltoElement(doc, peruste, rootElement, sisalto, 0, st, kieli);
        }

        // add tutkinnonosat as distinct chapters
        // TODO: Ordering?
        addTutkinnonosat(doc, peruste, kieli);

        // For dev/debugging love
        printDocument(doc, System.out);

        // so far doc contains mixed content from docbook and xhtml namespaces
        // lets transform xhtml bits onto docbook-format
        DOMSource source = new DOMSource(doc);
        SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();

        File resultFile;
        try (InputStream xslresource = getClass().getClassLoader().getResourceAsStream("docgen/epdoc-markup.xsl")) {
            Templates templates = stf.newTemplates(new StreamSource(xslresource));
            // or
            //Templates templates = stf.newTemplates(new StreamSource(new File("/full/path/to/epdoc-markup.xsl")));
            TransformerHandler th = stf.newTransformerHandler(templates);
            resultFile = File.createTempFile("peruste_" + UUID.randomUUID().toString(), ".xml");
            th.setResult(new StreamResult(resultFile));
            Transformer transformer = stf.newTransformer();
            transformer.transform(source, new SAXResult(th));
        }

        return resultFile.getAbsolutePath();
    }

    /**
     * Copies content from all the children of Jsoup Node on under W3C DOM Node
     * on given W3C document.
     *
     */
    private void jsoupIntoDOMNode(Document rootDoc, Node parentNode, org.jsoup.nodes.Node jsoupNode) {
        for (org.jsoup.nodes.Node child : jsoupNode.childNodes()) {
            createDOM(child, parentNode, rootDoc, new HashMap<String, String>());
        }
    }

    /**
     * The helper that copies content from the specified Jsoup Node into a W3C
     * Node.
     *
     * @param node The Jsoup node containing the content to copy to the
     * specified W3C Node.
     * @param out The W3C Node that receives the DOM content.
     */
    private void createDOM(org.jsoup.nodes.Node node, Node out, Document doc, Map<String, String> ns) {

        if (node instanceof org.jsoup.nodes.Document) {

            org.jsoup.nodes.Document d = ((org.jsoup.nodes.Document) node);
            for (org.jsoup.nodes.Node n : d.childNodes()) {
                createDOM(n, out, doc, ns);
            }

        } else if (node instanceof org.jsoup.nodes.Element) {

            org.jsoup.nodes.Element e = ((org.jsoup.nodes.Element) node);
            // create all new elements into xhtml namespace
            org.w3c.dom.Element _e = doc.createElementNS("http://www.w3.org/1999/xhtml", e.tagName());
            out.appendChild(_e);
            org.jsoup.nodes.Attributes atts = e.attributes();

            for (org.jsoup.nodes.Attribute a : atts) {
                String attName = a.getKey();
                //omit xhtml namespace
                if (attName.equals("xmlns")) {
                    continue;
                }
                String attPrefix = getNSPrefix(attName);
                if (attPrefix != null) {
                    if (attPrefix.equals("xmlns")) {
                        ns.put(getLocalName(attName), a.getValue());
                    } else if (!attPrefix.equals("xml")) {
                        String namespace = ns.get(attPrefix);
                        if (namespace == null) {
                            //fix attribute names looking like qnames
                            attName = attName.replace(':', '_');
                        }
                    }
                }
                _e.setAttribute(attName, a.getValue());
            }

            for (org.jsoup.nodes.Node n : e.childNodes()) {
                createDOM(n, _e, doc, ns);
            }

        } else if (node instanceof org.jsoup.nodes.TextNode) {

            org.jsoup.nodes.TextNode t = ((org.jsoup.nodes.TextNode) node);
            if (!(out instanceof Document)) {
                out.appendChild(doc.createTextNode(t.getWholeText()));
            }
        }
    }

    // some hacks for handling namespace in jsoup2DOM conversion
    private String getNSPrefix(String name) {
        if (name != null) {
            int pos = name.indexOf(':');
            if (pos > 0) {
                return name.substring(0, pos);
            }
        }
        return null;
    }

    private String getLocalName(String name) {
        if (name != null) {
            int pos = name.lastIndexOf(':');
            if (pos > 0) {
                return name.substring(pos + 1);
            }
        }
        return name;
    }

    private void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc),
                new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }

    private void addTutkinnonMuodostuminen(Document doc, Element parentElement, Peruste peruste, int depth, Suoritustapa tapa, Kieli kieli) {

        // ew, dodgy trycatching
        try {
            parentElement.appendChild(
                    getTutkinnonMuodostuminenGeneric(
                            doc,
                            peruste,
                            // TODO: jostain muualta???
                            messages.translate("docgen.tutkinnon_muodostuminen.title", kieli)
                             + tapa.getSuoritustapakoodi(),
                            depth,
                            tapa.getSuoritustapakoodi(),
                            kieli));
        } catch (Exception ex) {
            LOG.warn("adding getTutkinnonMuodostuminenGeneric failed miserably:", ex);
        }
    }

    private Element getTutkinnonMuodostuminenGeneric(
            Document doc, Peruste peruste, String title, int depth,
            Suoritustapakoodi suoritustapakoodi,
            Kieli kieli)
    {
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        RakenneModuuli rakenne = suoritustapa.getRakenne();

        Element returnElement;
        if (depth == 0) {
            returnElement = doc.createElement("chapter");
        } else {
            returnElement = doc.createElement("section");
        }

        Element titleElement = doc.createElement("title");
        titleElement.appendChild(doc.createTextNode(title));
        returnElement.appendChild(titleElement);

        List<AbstractRakenneOsa> osat = rakenne.getOsat();
        addRakenneOsatRec(returnElement, osat, doc, kieli);

        return returnElement;
    }

    private void addRakenneOsatRec(Element parent,
            List<AbstractRakenneOsa> osat, Document doc, Kieli kieli) {
        if (!osat.isEmpty()) {
            Element modlist = doc.createElement("itemizedlist");
            for (AbstractRakenneOsa osa : osat) {
                try {
                    if (osa instanceof RakenneModuuli) {
                        RakenneModuuli moduuli = (RakenneModuuli) osa;

                        Element modListItem = doc.createElement("listitem");
                        String modnimi = getTextString(moduuli.getNimi(), kieli);
                        modListItem.appendChild(doc.createTextNode(modnimi));

                        // dwell deeper
                        addRakenneOsatRec(modListItem, moduuli.getOsat(), doc, kieli);

                        modlist.appendChild(modListItem);

                    } else if (osa instanceof RakenneOsa) {
                        RakenneOsa rakenneOsa = (RakenneOsa) osa;
                        String modnimi = getTextString(rakenneOsa.getTutkinnonOsaViite().getTutkinnonOsa().getNimi(), kieli);
                        String refid = "tutkinnonosa" + rakenneOsa.getTutkinnonOsaViite().getTutkinnonOsa().getId();
                        Element modListItem = doc.createElement("listitem");
                        modListItem.appendChild(doc.createTextNode(modnimi));

                        Element linkElement = doc.createElement("link");
                        linkElement.setAttribute("linkend", refid);
                        linkElement.appendChild(doc.createTextNode(" (Seuraa -> " + refid + ")"));
                        modListItem.appendChild(linkElement);

                        modlist.appendChild(modListItem);
                    }

                } catch (ClassCastException cce) {
                    LOG.info("Oops, class cast exception");
                }
            }
            if (modlist.getChildNodes().getLength() > 0) {
                parent.appendChild(modlist);
            } else {
                LOG.debug("Not adding empty itemizedlist");
            }

        }
    }

    private void addSisaltoElement(Document doc, Peruste peruste, Element parentElement, PerusteenOsaViite sisalto, int depth, Suoritustapa tapa, Kieli kieli) {

        for (PerusteenOsaViite lapsi : sisalto.getLapset()) {
            if (lapsi.getPerusteenOsa() == null) {
                continue;
            }

            PerusteenOsa po = lapsi.getPerusteenOsa();
            TekstiKappale tk = null;
            if ( po instanceof TekstiKappale ){
                tk = (TekstiKappale)po;
            }

            if (tk == null) {
                LOG.error("*** eipä ole tekstikappale? " + po);
                continue;
            }

            Element element;
            if (depth == 0) {
                element = doc.createElement("chapter");
            } else {
                element = doc.createElement("section");
            }

            if (po.getTunniste() == PerusteenOsaTunniste.RAKENNE) {
                // special case, render TutkinnonRakenne here and continue with
                // rest of the sibligs
                addTutkinnonMuodostuminen(doc, parentElement, peruste, depth, tapa, kieli);
            } else {
                String nimi = getTextString(tk.getNimi(), kieli);
                Element titleElement = doc.createElement("title");
                titleElement.appendChild(doc.createTextNode(nimi));
                String teksti = getTextString(tk.getTeksti(), kieli);

                org.jsoup.nodes.Document fragment = Jsoup.parseBodyFragment(teksti);
                jsoupIntoDOMNode(doc, element, fragment.body());

                element.appendChild(titleElement);

                addSisaltoElement(doc, peruste, element, lapsi, depth + 1, tapa, kieli); // keep it rollin

                parentElement.appendChild(element);
            }

        }
    }

    private void addTutkinnonosat(Document doc, Peruste peruste, Kieli kieli) {

        // only distinct TutkinnonOsa
        Set<Suoritustapa> suoritustavat = peruste.getSuoritustavat();
        Set<TutkinnonOsa> osat = new HashSet<>();
        for (Suoritustapa suoritustapa : suoritustavat) {
            for (TutkinnonOsaViite viite : suoritustapa.getTutkinnonOsat()) {
                osat.add(viite.getTutkinnonOsa());
            }
        }

        for (TutkinnonOsa osa : osat) {
            String osanNimi = getTextString(osa.getNimi(), kieli);
            LOG.debug("handling {} - {}", osa.getId(), osanNimi);
            Element element = doc.createElement("chapter");
            String refid = "tutkinnonosa" + osa.getId();
            element.setAttribute("id", refid);

            Element titleElement = doc.createElement("title");
            titleElement.appendChild(doc.createTextNode(osanNimi));

            element.appendChild(titleElement);

            addTavoitteet(doc, element, osa, kieli);
            addAmmattitaitovaatimukset(doc, element, osa, kieli);
            addAmmattitaidonOsoittamistavat(doc, element, osa, kieli);
            addArviointi(doc, element, osa, kieli);

            doc.getDocumentElement().appendChild(element);
        }
    }

    private void addTavoitteet(Document doc, Element parent, TutkinnonOsa tutkinnonOsa, Kieli kieli) {

        String TavoitteetText = getTextString(tutkinnonOsa.getTavoitteet(), kieli);
        if (StringUtils.isEmpty(TavoitteetText)) {
            return;
        }

        addTekstiSectionGeneric(
                doc,
                parent,
                TavoitteetText,
                messages.translate("docgen.tavoitteet.title", kieli));
    }

    private void addAmmattitaitovaatimukset(Document doc, Element parent, TutkinnonOsa tutkinnonOsa, Kieli kieli) {

        String ammattitaitovaatimuksetText = getTextString(tutkinnonOsa.getAmmattitaitovaatimukset(), kieli);
        if (StringUtils.isEmpty(ammattitaitovaatimuksetText)) {
            return;
        }

        addTekstiSectionGeneric(
                doc,
                parent,
                ammattitaitovaatimuksetText,
                messages.translate("docgen.ammattitaitovaatimukset.title", kieli));
    }

    private void addAmmattitaidonOsoittamistavat(Document doc, Element parent, TutkinnonOsa tutkinnonOsa, Kieli kieli) {

        String ammattitaidonOsoittamistavatText = getTextString(tutkinnonOsa.getAmmattitaidonOsoittamistavat(), kieli);
        if (StringUtils.isEmpty(ammattitaidonOsoittamistavatText)) {
            return;
        }

        addTekstiSectionGeneric(
                doc,
                parent,
                ammattitaidonOsoittamistavatText,
                messages.translate("docgen.ammattitaidon_osoittamistavat.title", kieli));
    }

    private void addTekstiSectionGeneric(Document doc, Element parent, String teksti, String title) {

        Element section = doc.createElement("section");
        Element sectionTitle = doc.createElement("title");
        sectionTitle.appendChild(doc.createTextNode(title));
        section.appendChild(sectionTitle);

        org.jsoup.nodes.Document fragment = Jsoup.parseBodyFragment(teksti);
        jsoupIntoDOMNode(doc, section, fragment.body());

        parent.appendChild(section);
    }

    private void addArviointi(Document doc, Element parent, TutkinnonOsa tutkinnonOsa, Kieli kieli) {

        Element arviointiSection = doc.createElement("section");
        Element arviointiSectionTitle = doc.createElement("title");

        arviointiSectionTitle.appendChild(doc.createTextNode(
                messages.translate("docgen.arviointi.title", kieli)));

        arviointiSection.appendChild(arviointiSectionTitle);

        Arviointi arviointi = tutkinnonOsa.getArviointi();

        if (arviointi == null) {
            return;
        }

        parent.appendChild(arviointiSection);

        TekstiPalanen lisatiedot = arviointi.getLisatiedot();
        if (lisatiedot != null) {
            Element lisatietoPara = doc.createElement("para");
            String lisatietoteksti = getTextString(lisatiedot, kieli);
            org.jsoup.nodes.Document fragment = Jsoup.parseBodyFragment(lisatietoteksti);
            jsoupIntoDOMNode(doc, lisatietoPara, fragment.body());
            arviointiSection.appendChild(lisatietoPara);
        } else {
            LOG.info("Lisatiedot was null");
        }

        List<ArvioinninKohdealue> arvioinninKohdealueet = sanitizeList(arviointi.getArvioinninKohdealueet());
        for (ArvioinninKohdealue ka : arvioinninKohdealueet) {
            if (ka.getArvioinninKohteet() == null) {
                LOG.warn("Arvioinninkohteet was null");
                continue;
            }

            String otsikkoTeksti = getTextString(ka.getOtsikko(), kieli);
            Element kaSection = doc.createElement("section");
            Element kaSectionTitle = doc.createElement("title");
            kaSectionTitle.appendChild(doc.createTextNode(otsikkoTeksti));
            kaSection.appendChild(kaSectionTitle);
            arviointiSection.appendChild(kaSection);

            // initial implementation:
            // lets have each kohde as a table of their own, like in
            // website so that each osaamistaso with its kiteerit is on
            //its own row
            List<ArvioinninKohde> arvioinninKohteet = ka.getArvioinninKohteet();
            for (ArvioinninKohde kohde : arvioinninKohteet) {
                Element tableElement = doc.createElement("table");
                Element tableTitleElement = doc.createElement("title");
                String kohdeTeksti = getTextString(kohde.getOtsikko(), kieli);
                tableTitleElement.appendChild(doc.createTextNode(kohdeTeksti));
                tableElement.appendChild(tableTitleElement);

                Element groupElement = doc.createElement("tgroup");
                groupElement.setAttribute("cols", "2");
                Element colspecTaso = doc.createElement("colspec");
                colspecTaso.setAttribute("colwidth", "1*");
                groupElement.appendChild(colspecTaso);
                Element colspecKriteeri = doc.createElement("colspec");
                colspecKriteeri.setAttribute("colwidth", "3*");
                groupElement.appendChild(colspecKriteeri);

                Element headerElement = doc.createElement("thead");
                Element rowElement = doc.createElement("row");

                addTableCell(
                        doc,
                        rowElement,
                        messages.translate("docgen.osaamistaso.title", kieli));
                addTableCell(
                        doc,
                        rowElement,
                        messages.translate("docgen.osaamistason_kriteeri.title", kieli));
                headerElement.appendChild(rowElement);
                groupElement.appendChild(headerElement);

                Element bodyElement = doc.createElement("tbody");
                // TODO asteikko may be needed for proper ordering,
                // we'll see...
                //ArviointiAsteikko arviointiAsteikko = kohde.getArviointiAsteikko();
                Set<OsaamistasonKriteeri> osaamistasonKriteerit = kohde.getOsaamistasonKriteerit();

                for (OsaamistasonKriteeri krit : osaamistasonKriteerit) {
                    String taso = getTextString(krit.getOsaamistaso().getOtsikko(), kieli);
                    List<String> kriteerit = asStringList(krit.getKriteerit(), kieli);

                    Element bodyRowElement = doc.createElement("row");
                    addTableCell(doc, bodyRowElement, taso);
                    addTableCell(doc, bodyRowElement, kriteerit);
                    bodyElement.appendChild(bodyRowElement);
                }

                // dirty fix: a tbody must have a row which must have an entry
                if (osaamistasonKriteerit.isEmpty()) {
                    Element bodyRowElement = doc.createElement("row");
                    addTableCell(doc, bodyRowElement, "");
                    bodyElement.appendChild(bodyRowElement);
                }

                groupElement.appendChild(bodyElement);
                tableElement.appendChild(groupElement);
                kaSection.appendChild(tableElement);
            }
        }

    }

    private void addTableCell(Document doc, Element row, String text) {
        Element entry = doc.createElement("entry");
        entry.appendChild(doc.createTextNode(text));
        row.appendChild(entry);
    }

    private void addTableCell(Document doc, Element row, List<String> texts) {
        Element entry = doc.createElement("entry");
        for (String text : texts) {
            Element para = doc.createElement("para");
            para.appendChild(doc.createTextNode(text));
            entry.appendChild(para);
        }
        row.appendChild(entry);
    }

    private List<String> asStringList(List<TekstiPalanen> palaset, Kieli kieli) {
        List<String> list = new ArrayList<>();
        for (TekstiPalanen palanen : palaset) {
            list.add(getTextString(palanen, kieli));
        }
        return list;
    }

    private <T> List<T> sanitizeList(List<T> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    private String getTextString(TekstiPalanen teksti, Kieli kieli) {
        if (teksti == null
                || teksti.getTeksti() == null
                || teksti.getTeksti().get(kieli) == null) {
            return "";
        }
        return teksti.getTeksti().get(kieli);
    }
}
