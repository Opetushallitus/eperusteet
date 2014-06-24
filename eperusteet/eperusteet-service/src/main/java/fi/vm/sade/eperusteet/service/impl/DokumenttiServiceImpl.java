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

import com.google.code.docbook4j.Docbook4JException;
import fi.vm.sade.eperusteet.domain.ArvioinninKohde;
import fi.vm.sade.eperusteet.domain.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.Arviointi;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.OsaamistasonKriteeri;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.DokumenttiService;
import com.google.code.docbook4j.renderer.PerustePDFRenderer;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author jussini
 */
@Service
public class DokumenttiServiceImpl implements DokumenttiService {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiServiceImpl.class);

    private Kieli kieli = Kieli.FI;
    
    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;
    
    @Override
    @Transactional(readOnly = true)
    public void generateWithToken(long id, String token){ 
        LOG.debug("generate with token {},{}", id, token);
        
        try {            
            File fout = getTmpFile(token);
            fout.createNewFile(); // so that tell whether it exists if queried
            File finalFile = getFinalFile(token);
            byte[] doc = generateFor(id);
            try (FileOutputStream fos = new FileOutputStream(fout)) {
                LOG.debug("dumping to file {}", fout.getAbsolutePath());
                fos.write(doc);
            }
            
            LOG.debug("renaming file to {}", finalFile.getAbsolutePath());
            fout.renameTo(finalFile);
            
        } catch (IOException ex) {
            LOG.error("IOException when writing doc: {}", ex);
        }
    }

    @Override
    public String getNewTokenFor(long id) {
        return new StringBuilder()
                .append("peruste_")
                .append(id)
                .append("_")
                .append(new Date().getTime())
                .toString();                
    }
    
    @Override
    public byte[] getWithToken(String token) {
        File f = getFinalFile(token);
        try {            
            ByteArrayOutputStream baos;
            try (FileInputStream fis = new FileInputStream(f)) {
                baos = new ByteArrayOutputStream();
                IOUtils.copy(fis, baos);
            }
            baos.close();
            return baos.toByteArray();
        } catch (FileNotFoundException ex) {
            LOG.debug("{} does not exist", f.getAbsolutePath());
            return null;
        } catch (IOException ex) {
            LOG.error("IOException when copying: {}", ex);
            return null;
        }        
    }

    @Override
    public DokumenttiDto query(String token) {
        boolean tmpExists = getTmpFile(token).exists();
        boolean finalExists = getFinalFile(token).exists();
        
        DokumenttiDto dto = new DokumenttiDto();
        dto.setToken(token);
        if (finalExists) {
            dto.setTila(DokumenttiDto.Tila.VALMIS);
        } else if (tmpExists) {
            dto.setTila(DokumenttiDto.Tila.LUODAAN);
        } else {
            dto.setTila(DokumenttiDto.Tila.EI_OLE);
        }
        
        return dto;
    }
    
    @Override
    @Transactional(readOnly = true)
    public byte[] generateFor(long id) {

        LOG.info("generateFor: id {}", id);

        Peruste peruste = perusteprojektiRepository.findOne(id).getPeruste();
        LOG.info("Peruste: {}", peruste);
        TekstiPalanen nimi = peruste.getNimi();
        LOG.info("Nimi: {}", getTextString(nimi));

        try {
            String xmlpath = generateXML(peruste);
            LOG.debug("Temporary xml file: \n{}", xmlpath);
            // we could also use 
            //String style = "file:///full/path/to/docbookstyle.xsl";            
            String style = "res:docgen/docbookstyle.xsl";

            //PDFRenderer r = PDFRenderer.create(xmlpath, style);
            PerustePDFRenderer r = new PerustePDFRenderer().xml(xmlpath).xsl(style);
            r.parameter("l10n.gentext.language", "fi");
            InputStream is;
            is = r.render();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // TODO, maybe use ioutils as it seems to be project dependency anyways
            byte[] buf = new byte[2048];
            int n = -1;
            while ((n = is.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }

            baos.close();
            is.close();

            return baos.toByteArray();

        } catch (Docbook4JException | IOException | TransformerException | ParserConfigurationException ex) {
            LOG.error(ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    private String generateXML(Peruste peruste) throws TransformerConfigurationException, IOException, TransformerException, ParserConfigurationException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("book");
        rootElement.setAttribute("xmlns", "http://docbook.org/ns/docbook");
        rootElement.setAttribute("xmlns:h", "http://www.w3.org/1999/xhtml");
        doc.appendChild(rootElement);

        String nimi = getTextString(peruste.getNimi());
        Element titleElement = doc.createElement("title");
        titleElement.appendChild(doc.createTextNode(nimi));
        rootElement.appendChild(titleElement);

        rootElement.appendChild(doc.createElement("info"));

        // Luku: Johdanto (hmm, oikeesti?)
        addJohdanto(doc, peruste);

        // Luku: tutkinnon muodostuminen (tutkinnon osat ja tutkinnon rakenne)
        addTutkinnonMuodostuminen(doc, peruste);

        // Luku: Perusteiden toimeenpano ammatillisessa peruskoulutuksessa
        addToimeenpanoPeruskoulutuksessa(doc, peruste);

        // Luku: Perustutkinnon suorittaminen näyttötutkintona
        addSuorittaminenNayttotutkintona(doc, peruste);

        // Luku: Tutkinnonosat, ammattitaitovaatimukset ja arviointi
        addTutkinnonosat(doc, peruste);

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
     **/
    private void jsoupIntoDOMNode(Document rootDoc, Node parentNode, org.jsoup.nodes.Node jsoupNode) {
        for (org.jsoup.nodes.Node child : jsoupNode.childNodes()) {
            createDOM(child, parentNode, rootDoc, new HashMap<String, String>());
        }
    }

    /**
     * The helper that copies content from the specified Jsoup Node into 
     * a W3C Node.
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

    private static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
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

    private void addJohdanto(Document doc, Peruste peruste) {
        Element chapterElement = doc.createElement("chapter");
        Element chapterTitleElement = doc.createElement("title");
        // TODO: localize
        chapterTitleElement.appendChild(doc.createTextNode("Johdanto"));
        Element chapterParaElement = doc.createElement("para");
        chapterParaElement.appendChild(doc.createTextNode(""));
        chapterElement.appendChild(chapterTitleElement);
        chapterElement.appendChild(chapterParaElement);
        doc.getDocumentElement().appendChild(chapterElement);
    }

    private void addTutkinnonMuodostuminen(Document doc, Peruste peruste) {
        Element chapterElement = doc.createElement("chapter");
        Element chapterTitleElement = doc.createElement("title");
        // TODO: localize
        chapterTitleElement.appendChild(doc.createTextNode("Perustutkinnon tavoitteet ja tutkinnon muodostuminen"));
        Element chapterParaElement = doc.createElement("para");
        chapterParaElement.appendChild(doc.createTextNode(""));
        chapterElement.appendChild(chapterTitleElement);
        chapterElement.appendChild(chapterParaElement);

        chapterElement.appendChild(getTutkinnonMuodostuminenPeruskoulutuksessa(doc, peruste));
        chapterElement.appendChild(getTutkinnonMuodostuminenNayttotutkinnossa(doc, peruste));

        doc.getDocumentElement().appendChild(chapterElement);
    }

    private Element getTutkinnonMuodostuminenPeruskoulutuksessa(
            Document doc, Peruste peruste) 
    {
        return getTutkinnonMuodostuminenGeneric(
                doc, 
                peruste, 
                // TODO: localize
                "Ammatillisessa peruskoulutuksessa",
                Suoritustapakoodi.OPS);
    }

    private Element getTutkinnonMuodostuminenNayttotutkinnossa(
            Document doc, Peruste peruste) 
    {
        return getTutkinnonMuodostuminenGeneric(
                doc, 
                peruste, 
                // TODO: localize
                "Näyttötutkinnossa",
                Suoritustapakoodi.NAYTTO);
    }

    private Element getTutkinnonMuodostuminenGeneric(
            Document doc, Peruste peruste, String title, 
            Suoritustapakoodi suoritustapakoodi) 
    {
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        RakenneModuuli rakenne = suoritustapa.getRakenne();
        String nimi = getTextString(rakenne.getNimi());
        String kuvaus = getTextString(rakenne.getKuvaus());

        Element sectionElement = doc.createElement("section");
        Element sectionTitleElement = doc.createElement("title");
        sectionTitleElement.appendChild(doc.createTextNode(title));

        Element rakenneSectionElement = doc.createElement("section");
        Element rakenneSectionTitleElement = doc.createElement("title");
        rakenneSectionTitleElement.appendChild(doc.createTextNode(nimi));
        Element rakenneSectionParaElement = doc.createElement("para");
        rakenneSectionParaElement.appendChild(doc.createTextNode(kuvaus));

        rakenneSectionElement.appendChild(rakenneSectionTitleElement);
        rakenneSectionElement.appendChild(rakenneSectionParaElement);

        List<AbstractRakenneOsa> osat = rakenne.getOsat();
        addRakenneOsatRec(rakenneSectionElement, osat, doc);

        sectionElement.appendChild(sectionTitleElement);
        sectionElement.appendChild(rakenneSectionElement);

        return sectionElement;
    }

    private void addRakenneOsatRec(Element parent,
            List<AbstractRakenneOsa> osat, Document doc) {
        if (!osat.isEmpty()) {
            Element modlist = doc.createElement("itemizedlist");
            for (AbstractRakenneOsa osa : osat) {
                try {
                    if (osa instanceof RakenneModuuli) {
                        RakenneModuuli moduuli = (RakenneModuuli) osa;

                        Element modListItem = doc.createElement("listitem");
                        String modnimi = getTextString(moduuli.getNimi());
                        modListItem.appendChild(doc.createTextNode(modnimi));

                        // dwell deeper
                        addRakenneOsatRec(modListItem, moduuli.getOsat(), doc);

                        modlist.appendChild(modListItem);

                    } else if (osa instanceof RakenneOsa) {
                        RakenneOsa rakenneOsa = (RakenneOsa) osa;
                        String modnimi = getTextString(rakenneOsa.getTutkinnonOsaViite().getTutkinnonOsa().getNimi());
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

    private void addToimeenpanoPeruskoulutuksessa(Document doc, Peruste peruste) {
        addSuorittaminenGeneric(
                doc,
                peruste,
                // TODO: localize
                "Perustutkinnon perusteiden toimeenpano ammatillisessa peruskoulutuksessa",
                Suoritustapakoodi.OPS);
    }

    private void addSuorittaminenNayttotutkintona(Document doc, Peruste peruste) {
        addSuorittaminenGeneric(
                doc,
                peruste,
                // TODO: localize
                "Perustutkinnon suorittaminen näyttötutkintona",
                Suoritustapakoodi.NAYTTO);
    }

    private void addSuorittaminenGeneric(Document doc, Peruste peruste, String title, Suoritustapakoodi suoritustapa) {
        Element chapterElement = doc.createElement("chapter");
        Element chapterTitleElement = doc.createElement("title");
        chapterTitleElement.appendChild(doc.createTextNode(title));
        Element chapterParaElement = doc.createElement("para");
        chapterParaElement.appendChild(doc.createTextNode(""));
        chapterElement.appendChild(chapterTitleElement);
        chapterElement.appendChild(chapterParaElement);
        doc.getDocumentElement().appendChild(chapterElement);

        PerusteenOsaViite sisalto = peruste.getSuoritustapa(suoritustapa).getSisalto();
        addSisaltoElement(doc, chapterElement, sisalto);
    }

    private void addSisaltoElement(Document doc, Element parentElement, PerusteenOsaViite sisalto) {
        for (PerusteenOsaViite lapsi : sisalto.getLapset()) {
            if (lapsi.getPerusteenOsa() == null) {
                continue;
            }
            
            TekstiKappale tk = (TekstiKappale) lapsi.getPerusteenOsa();
            if (tk == null) {
                continue;
            }

            Element sectionElement = doc.createElement("section");
            Element sectionTitleElement = doc.createElement("title");
            sectionTitleElement.appendChild(doc.createTextNode(getTextString(tk.getNimi())));
            String teksti = getTextString(tk.getTeksti());

            org.jsoup.nodes.Document fragment = Jsoup.parseBodyFragment(teksti);
            jsoupIntoDOMNode(doc, sectionElement, fragment.body());

            sectionElement.appendChild(sectionTitleElement);
            parentElement.appendChild(sectionElement);
        }
    }

    private void addTutkinnonosat(Document doc, Peruste peruste) {
        Element chapterElement = doc.createElement("chapter");
        Element chapterTitleElement = doc.createElement("title");
        // TODO: localize
        chapterTitleElement.appendChild(doc.createTextNode("Perustutkinnon ammatilliset tutkinnonosat, ammattitaitovaatimukset ja arviointi"));
        Element chapterParaElement = doc.createElement("para");
        chapterParaElement.appendChild(doc.createTextNode(""));
        chapterElement.appendChild(chapterTitleElement);
        chapterElement.appendChild(chapterParaElement);

        Set<Suoritustapa> suoritustavat = peruste.getSuoritustavat();
        for (Suoritustapa suoritustapa : suoritustavat) {

            // Tutkinnonosat
            for (TutkinnonOsaViite tutkinnonOsaViite : suoritustapa.getTutkinnonOsat()) {
                TutkinnonOsa osa = tutkinnonOsaViite.getTutkinnonOsa();
                String osanNimi = getTextString(osa.getNimi());

                Element sectionElement = doc.createElement("section");
                String refid = "tutkinnonosa" + osa.getId();
                sectionElement.setAttribute("id", refid);

                Element sectionTitleElement = doc.createElement("title");
                sectionTitleElement.appendChild(doc.createTextNode(osanNimi));

                sectionElement.appendChild(sectionTitleElement);

                addTavoitteet(doc, sectionElement, osa);
                addAmmattitaitovaatimukset(doc, sectionElement, osa);
                addAmmattitaidonOsoittamistavat(doc, sectionElement, osa);
                addArviointi(doc, sectionElement, osa);
                
                chapterElement.appendChild(sectionElement);
            }
        }
        doc.getDocumentElement().appendChild(chapterElement);
    }

    private void addTavoitteet(Document doc, Element parent, TutkinnonOsa tutkinnonOsa) {

        String TavoitteetText = getTextString(tutkinnonOsa.getTavoitteet());
        if (StringUtils.isEmpty(TavoitteetText)) {
            LOG.info("Oops, no tavoitteetText :(");
            return;
        }
        // TODO: localize
        addTekstiSectionGeneric(doc, parent, TavoitteetText, "Tavoitteet");
    }
    
    private void addAmmattitaitovaatimukset(Document doc, Element parent, TutkinnonOsa tutkinnonOsa) {

        String ammattitaitovaatimuksetText = getTextString(tutkinnonOsa.getAmmattitaitovaatimukset());
        if (StringUtils.isEmpty(ammattitaitovaatimuksetText)) {
            LOG.info("Oops, no ammattitaitovaatimuksetText :(");
            return;
        }
        // TODO: localize
        addTekstiSectionGeneric(doc, parent, ammattitaitovaatimuksetText, "Ammattitaitovaatimukset");
    }
    
    private void addAmmattitaidonOsoittamistavat(Document doc, Element parent, TutkinnonOsa tutkinnonOsa) {

        String ammattitaidonOsoittamistavatText = getTextString(tutkinnonOsa.getAmmattitaidonOsoittamistavat());
        if (StringUtils.isEmpty(ammattitaidonOsoittamistavatText)) {
            LOG.info("Oops, no ammattitaidonOsoittamistavatText for {}:(", getTextString(tutkinnonOsa.getNimi()));
            return;
        }
        // TODO: localize
        addTekstiSectionGeneric(doc, parent, ammattitaidonOsoittamistavatText, "Ammattitaidon osoittamistavat");
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
    
    private void addArviointi(Document doc, Element parent, TutkinnonOsa tutkinnonOsa) {

        Element arviointiSection = doc.createElement("section");
        Element arviointiSectionTitle = doc.createElement("title");
        arviointiSectionTitle.appendChild(doc.createTextNode("Arviointi"));
        arviointiSection.appendChild(arviointiSectionTitle);

        Arviointi arviointi = tutkinnonOsa.getArviointi();
        
        if (arviointi == null) {
            return;
        }

        parent.appendChild(arviointiSection);
                
        TekstiPalanen lisatiedot = arviointi.getLisatiedot();
        if (lisatiedot != null) {
            Element lisatietoPara = doc.createElement("para");
            String lisatietoteksti = getTextString(lisatiedot);
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

            String otsikkoTeksti = getTextString(ka.getOtsikko());
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
                String kohdeTeksti = getTextString(kohde.getOtsikko());
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
                addTableCell(doc, rowElement, "Osaamistaso");
                addTableCell(doc, rowElement, "Osaamistason kriteeri");
                headerElement.appendChild(rowElement);

                Element bodyElement = doc.createElement("tbody");
                // TODO asteikko may be needed for proper ordering,
                // we'll see...
                //ArviointiAsteikko arviointiAsteikko = kohde.getArviointiAsteikko();
                Set<OsaamistasonKriteeri> osaamistasonKriteerit = kohde.getOsaamistasonKriteerit();
                for (OsaamistasonKriteeri krit : osaamistasonKriteerit) {
                    String taso = getTextString(krit.getOsaamistaso().getOtsikko());
                    List<String> kriteerit = asStringList(krit.getKriteerit());

                    Element bodyRowElement = doc.createElement("row");
                    addTableCell(doc, bodyRowElement, taso);
                    addTableCell(doc, bodyRowElement, kriteerit);
                    bodyElement.appendChild(bodyRowElement);
                }

                groupElement.appendChild(headerElement);
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
    
    private List<String> asStringList(List<TekstiPalanen> palaset) {
        List<String> list = new ArrayList();
        for(TekstiPalanen palanen : palaset) {
            list.add(getTextString(palanen));
        }        
        return list;
    }
 
    private <T> List<T> sanitizeList(List<T> list) {
        if (list == null) {
            return new ArrayList(); 
        }
        return list;
    }
    
    private String getTextString(TekstiPalanen teksti) {
        return getTextString(teksti, this.kieli);
    }
    
    private String getTextString(TekstiPalanen teksti, Kieli kieli) {
        if(teksti == null || 
           teksti.getTeksti() == null || 
           teksti.getTeksti().get(kieli) == null) 
        {
            return "";
        }
        return teksti.getTeksti().get(kieli);
    }
    
    
    private File getTmpFile(String token) {        
        String tmppath =  getFilenameBase(token) + ".tmp";        
        return new File(tmppath);
    }
    
    private File getFinalFile(String token) {        
        String finalpath = getFilenameBase(token) + ".pdf";        
        return new File(finalpath);
    }
    
    private String getFilenameBase(String token) {
        String sane = token.replace("/", "");
        String tmpdir = System.getProperty("java.io.tmpdir");
        return tmpdir + File.separator + sane;     
    }
}
