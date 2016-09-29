package fi.vm.sade.eperusteet.service.dokumentti.impl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.MuodostumisSaanto;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.repository.TutkintonimikeKoodiRepository;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.LocalizedMessagesService;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.CharapterNumberGenerator;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.DokumenttiBase;
import fi.vm.sade.eperusteet.service.internal.DokumenttiNewBuilderService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import static fi.vm.sade.eperusteet.service.dokumentti.impl.util.DokumenttiUtils.*;

/**
 * @author isaul
 */
@Service
public class DokumenttiNewBuilderServiceImpl implements DokumenttiNewBuilderService {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiNewBuilderServiceImpl.class);

    @Autowired
    private TutkintonimikeKoodiRepository tutkintonimikeKoodiRepository;

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private KoodistoClient koodistoService;

    @Autowired
    private LocalizedMessagesService messages;

    @Override
    public Document generateXML(Peruste peruste, Dokumentti dokumentti, Kieli kieli,
                                Suoritustapakoodi suoritustapakoodi)
            throws ParserConfigurationException, IOException, TransformerException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        // Luodaan XHTML pohja
        Element rootElement = doc.createElement("html");
        rootElement.setAttribute("lang", kieli.toString());
        doc.appendChild(rootElement);

        Element headElement = doc.createElement("head");

        // Poistetaan HEAD:in <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
        if (headElement.hasChildNodes()) {
            headElement.removeChild(headElement.getFirstChild());
        }

        Element bodyElement = doc.createElement("body");

        rootElement.appendChild(headElement);
        rootElement.appendChild(bodyElement);

        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        PerusteenOsaViite sisalto = suoritustapa.getSisalto();

        DokumenttiBase docBase = new DokumenttiBase();
        docBase.setDocument(doc);
        docBase.setHeadElement(headElement);
        docBase.setBodyElement(bodyElement);
        docBase.setGenerator(new CharapterNumberGenerator());
        docBase.setKieli(kieli);
        docBase.setPeruste(peruste);
        docBase.setDokumentti(dokumentti);
        docBase.setMapper(mapper);
        docBase.setSisalto(sisalto);

        // Kansilehti & Infosivu
        addMetaPages(docBase);

        // sisältöelementit (proosa)
        addSisaltoelementit(docBase);

        // pudotellaan tutkinnonosat paikalleen
        addTutkinnonosat(docBase);

        // lisätään tekstikappaleet
        addTekstikappaleet(docBase, docBase.getSisalto());

        // Tulostetaan dokumentti
        printDocument(docBase.getDocument());

        return doc;
    }

    private void addMetaPages(DokumenttiBase docBase) {
        // Nimi
        Element title = docBase.getDocument().createElement("title");
        String nimi = getTextString(docBase, docBase.getPeruste().getNimi());
        if (nimi != null && nimi.length() != 0) {
            title.appendChild(docBase.getDocument().createTextNode(nimi));
            docBase.getHeadElement().appendChild(title);
        }

        // Kuvaus
        String kuvaus = getTextString(docBase, docBase.getPeruste().getKuvaus());
        if (kuvaus != null && kuvaus.length() != 0) {
            Element description = docBase.getDocument().createElement("description");
            addTeksti(docBase, kuvaus, "div", description);
            docBase.getHeadElement().appendChild(description);
        }

        // Diaarinumero
        if (docBase.getPeruste().getDiaarinumero() != null) {
            Element diary = docBase.getDocument().createElement("meta");
            diary.setAttribute("name", "diary");
            diary.setAttribute("content", docBase.getPeruste().getDiaarinumero().toString());
            docBase.getHeadElement().appendChild(diary);
        }

        // Korvaavat määräykset
        if (docBase.getPeruste().getKorvattavatDiaarinumerot() != null
                && !docBase.getPeruste().getKorvattavatDiaarinumerot().isEmpty()) {
            Element korvaavat = docBase.getDocument().createElement("korvaavat");

            docBase.getPeruste().getKorvattavatDiaarinumerot().stream()
                    .map(Diaarinumero::getDiaarinumero)
                    .forEach(numero -> {
                        Element korvaava = docBase.getDocument().createElement("korvaava");
                        korvaava.setTextContent(numero);
                        korvaavat.appendChild(korvaava);
                    });

            docBase.getHeadElement().appendChild(korvaavat);
        }

        // Muutosmääräykset
        if (docBase.getPeruste().getMuutosmaaraykset() != null
                && !docBase.getPeruste().getMuutosmaaraykset().isEmpty()) {
            Element muutosmaaraykset = docBase.getDocument().createElement("muutosmaaraykset");

            docBase.getPeruste().getMuutosmaaraykset().forEach(muutosmaarays -> {
                Element muutosmaaraysEl = docBase.getDocument().createElement("muutosmaarays");

                Element linkki = docBase.getDocument().createElement("a");
                linkki.setAttribute("href", getTextString(docBase, muutosmaarays.getUrl()));
                if (muutosmaarays.getNimi() != null) {
                    linkki.setTextContent(getTextString(docBase, muutosmaarays.getNimi()));
                } else {
                    linkki.setTextContent(getTextString(docBase, muutosmaarays.getUrl()));
                }
                muutosmaaraysEl.appendChild(linkki);

                muutosmaaraykset.appendChild(muutosmaaraysEl);
            });

            docBase.getHeadElement().appendChild(muutosmaaraykset);
        }

        // Koulutuskoodit
        if (docBase.getPeruste().getKoulutukset() != null && docBase.getPeruste().getKoulutukset().size() != 0) {
            Element koulutukset = docBase.getDocument().createElement("koulutukset");
            docBase.getPeruste().getKoulutukset().forEach(koulutus -> {
                String koulutusNimi = getTextString(docBase, koulutus.getNimi());
                if (StringUtils.isNotEmpty(koulutus.getKoulutuskoodiArvo())) {
                    koulutusNimi += " (" + koulutus.getKoulutuskoodiArvo() + ")";
                }
                Element koulutusEl = docBase.getDocument().createElement("koulutus");
                koulutusEl.setTextContent(koulutusNimi);
                koulutukset.appendChild(koulutusEl);
            });
            docBase.getHeadElement().appendChild(koulutukset);
        }

        // Osaamisalat
        if (docBase.getPeruste().getOsaamisalat() != null && docBase.getPeruste().getOsaamisalat().size() != 0) {
            Element osaamisalat = docBase.getDocument().createElement("osaamisalat");
            docBase.getPeruste().getOsaamisalat().forEach(osaamisala -> {
                String osaamisalaNimi = getTextString(docBase, osaamisala.getNimi());
                if (StringUtils.isNotEmpty(osaamisala.getArvo())) {
                    osaamisalaNimi += " (" + osaamisala.getArvo() + ")";
                }
                Element osaamisalaEl = docBase.getDocument().createElement("osaamisala");
                osaamisalaEl.setTextContent(osaamisalaNimi);
                osaamisalat.appendChild(osaamisalaEl);
            });
            docBase.getHeadElement().appendChild(osaamisalat);
        }

        // Tutkintonimikkeet
        List<TutkintonimikeKoodi> nimikeKoodit = tutkintonimikeKoodiRepository
                .findByPerusteId(docBase.getPeruste().getId());
        if (nimikeKoodit != null && nimikeKoodit.size() != 0) {
            Element tutkintonimikkeet = docBase.getDocument().createElement("tutkintonimikkeet");
            nimikeKoodit.forEach(tnkoodi -> {
                KoodistoKoodiDto koodiDto = koodistoService.get("tutkintonimikkeet", tnkoodi.getTutkintonimikeUri());

                for (KoodistoMetadataDto meta : koodiDto.getMetadata()) {
                    if (meta.getKieli().toLowerCase().equals(docBase.getKieli().toString().toLowerCase())) {
                        Element tutkintonimike = docBase.getDocument().createElement("tutkintonimike");
                        tutkintonimike.setTextContent(meta.getNimi() + " (" + tnkoodi.getTutkintonimikeArvo() + ")");
                        tutkintonimikkeet.appendChild(tutkintonimike);
                    } else {
                        LOG.debug("{} was no match", meta.getKieli());
                    }
                }
            });
            if (tutkintonimikkeet.hasChildNodes()) {
                docBase.getHeadElement().appendChild(tutkintonimikkeet);
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        // Voimaantulo
        if (docBase.getPeruste().getVoimassaoloAlkaa() != null) {
            Element description = docBase.getDocument().createElement("meta");
            description.setAttribute("name", "voimaantulo");
            description.setAttribute("content", dateFormat.format(docBase.getPeruste().getVoimassaoloAlkaa()));
            docBase.getHeadElement().appendChild(description);
        }

        // Voimaantulon päättyminen
        if (docBase.getPeruste().getVoimassaoloLoppuu() != null) {
            Element description = docBase.getDocument().createElement("meta");
            description.setAttribute("name", "voimassaolo-paattyminen");
            description.setAttribute("content", dateFormat.format(docBase.getPeruste().getVoimassaoloLoppuu()));
            docBase.getHeadElement().appendChild(description);
        }
    }

    private void addSisaltoelementit(DokumenttiBase docBase) {
        for (PerusteenOsaViite lapsi : docBase.getSisalto().getLapset()) {
            PerusteenOsa po = lapsi.getPerusteenOsa();
            if (po == null) {
                continue;
            }

            if (po.getTunniste() == PerusteenOsaTunniste.RAKENNE) {
                // poikkeustapauksena perusteen rakennepuun rendaus
                addTutkinnonMuodostuminen(docBase);
            }
        }
    }

    private void addTutkinnonMuodostuminen(DokumenttiBase docBase) {
        addHeader(docBase, messages.translate("docgen.tutkinnon_muodostuminen.title", docBase.getKieli()));
        RakenneModuuli rakenne = docBase.getSisalto().getSuoritustapa().getRakenne();

        String kuvaus = getTextString(docBase, rakenne.getKuvaus());
        if (StringUtils.isNotEmpty(kuvaus)) {
            addTeksti(docBase, kuvaus, "div");
        }

        // Luodaan muodostumistaulukko
        Element taulukko = docBase.getDocument().createElement("table");
        docBase.getBodyElement().appendChild(taulukko);
        taulukko.setAttribute("border", "1");
        Element tbody = docBase.getDocument().createElement("tbody");
        taulukko.appendChild(tbody);

        addRakenneOsa(docBase, rakenne, tbody, 0);

        docBase.getGenerator().increaseNumber();
    }

    private void addRakenneOsa(DokumenttiBase docBase, AbstractRakenneOsa osa, Element tbody, int depth) {
        if (osa instanceof RakenneModuuli) {
            // Ryhmä
            RakenneModuuli rakenneModuuli = (RakenneModuuli) osa;

            addRakenneModuuli(docBase, rakenneModuuli, tbody, depth);

            // Rekursiivisesti koko puu
            for (AbstractRakenneOsa lapsi : rakenneModuuli.getOsat()) {
                addRakenneOsa(docBase, lapsi, tbody, depth + 1);
            }
        } else if (osa instanceof RakenneOsa) {
            // Tutkinnon osa
            RakenneOsa rakenneOsa = (RakenneOsa) osa;

            BigDecimal laajuus = rakenneOsa.getTutkinnonOsaViite().getLaajuus();
            LaajuusYksikko laajuusYksikko = rakenneOsa.getTutkinnonOsaViite().getSuoritustapa().getLaajuusYksikko();
            String laajuusStr = "";
            String yks = "";
            if (laajuus != null && laajuus.compareTo(BigDecimal.ZERO) > 0) {
                laajuusStr = laajuus.stripTrailingZeros().toPlainString();
                if (laajuusYksikko == LaajuusYksikko.OSAAMISPISTE) {
                    yks = messages.translate("docgen.laajuus.osp", docBase.getKieli());
                } else {
                    yks = messages.translate("docgen.laajuus.ov", docBase.getKieli());
                }
            }

            String nimi = getTextString(docBase, rakenneOsa.getTutkinnonOsaViite().getTutkinnonOsa().getNimi());
            String kuvaus = getTextString(docBase, rakenneOsa.getKuvaus());

            StringBuilder nimiBuilder = new StringBuilder();
            nimiBuilder.append(" ").append(nimi);
            if (!laajuusStr.isEmpty()) {
                nimiBuilder.append(", ").append(laajuusStr).append(" ").append(yks);
            }

            Element tr = docBase.getDocument().createElement("tr");
            Element td = docBase.getDocument().createElement("td");
            Element p = docBase.getDocument().createElement("p");

            tbody.appendChild(tr);
            tr.appendChild(td);
            td.appendChild(p);
            p.setTextContent(nimiBuilder.toString());
            if (StringUtils.isNotEmpty(kuvaus)) {
                td.appendChild(newItalicElement(docBase.getDocument(), kuvaus));
            }

            if (rakenneOsa.isPakollinen()) {
                String glyph = messages.translate("docgen.rakenneosa.pakollinen.glyph", docBase.getKieli());
                nimiBuilder.append(", ");
                p.appendChild(docBase.getDocument().createTextNode(", "));
                p.appendChild(newBoldElement(docBase.getDocument(), glyph));
            }
        }
    }

    private void addRakenneModuuli(DokumenttiBase docBase, RakenneModuuli rakenneModuuli, Element tbody, int depth) {
        String nimi = getTextString(docBase, rakenneModuuli.getNimi());
        MuodostumisSaanto muodostumisSaanto = rakenneModuuli.getMuodostumisSaanto();

        String kokoTeksti = getKokoTeksti(muodostumisSaanto, docBase.getKieli());
        String laajuusTeksti = getLaajuusTeksti(muodostumisSaanto,
                docBase.getSisalto().getSuoritustapa().getLaajuusYksikko(), docBase.getKieli());

        String kuvaus = getTextString(docBase, rakenneModuuli.getKuvaus());

        // TODO: yksiköt, kappaleet
        if (kokoTeksti != null) {
            nimi += " | " + kokoTeksti;
        }
        if (laajuusTeksti != null) {
            nimi += " | " + laajuusTeksti;
        }

        Element tr = docBase.getDocument().createElement("tr");
        Element th = docBase.getDocument().createElement("th");
        Element td = docBase.getDocument().createElement("td");
        Element p = docBase.getDocument().createElement("p");

        switch (depth) {
            case 0:

                break;
            case 1:

                tr.setAttribute("bgcolor", "#AAAAAA");

                tbody.appendChild(tr);
                tr.appendChild(th);
                th.appendChild(p);
                p.appendChild(newBoldElement(docBase.getDocument(), nimi.toUpperCase()));
                if (StringUtils.isNotEmpty(kuvaus)) {
                    th.appendChild(newItalicElement(docBase.getDocument(), kuvaus));
                }

                break;
            case 2:
                tr.setAttribute("bgcolor", "#EEEEEE");
                tbody.appendChild(tr);
                tr.appendChild(td);
                td.appendChild(p);
                p.appendChild(newBoldElement(docBase.getDocument(), nimi));
                if (StringUtils.isNotEmpty(kuvaus)) {
                    td.appendChild(newItalicElement(docBase.getDocument(), kuvaus));
                }

                break;
            case 3:
                tbody.appendChild(tr);
                tr.appendChild(td);
                td.appendChild(p);
                p.appendChild(newBoldElement(docBase.getDocument(), nimi));
                if (StringUtils.isNotEmpty(kuvaus)) {
                    td.appendChild(newItalicElement(docBase.getDocument(), kuvaus));
                }

                break;
            default:

                break;
        }
    }


    private void addTutkinnonosat(DokumenttiBase docBase) {

    }

    private void addTekstikappaleet(DokumenttiBase docBase, PerusteenOsaViite parent) {
        for (PerusteenOsaViite lapsi : parent.getLapset()) {
            PerusteenOsa po = lapsi.getPerusteenOsa();
            if (po == null || !(po instanceof TekstiKappale)) {
                continue;
            }
            TekstiKappale tk = (TekstiKappale) po;

            if (po.getTunniste() != PerusteenOsaTunniste.RAKENNE
                    && po.getTunniste() != PerusteenOsaTunniste.LAAJAALAINENOSAAMINEN) {
                String nimi = getTextString(docBase, tk.getNimi());
                addHeader(docBase, nimi);

                String teksti = getTextString(docBase, tk.getTeksti());
                addTeksti(docBase, teksti, "div");

                docBase.getGenerator().increaseDepth();

                // Rekursiivisesti
                addTekstikappaleet(docBase, lapsi);

                docBase.getGenerator().decreaseDepth();
                docBase.getGenerator().increaseNumber();
            }
        }
    }

    private Element newBoldElement(Document doc, String teksti) {
        Element strong = doc.createElement("strong");
        strong.appendChild(doc.createTextNode(teksti));
        return strong;
    }

    private Element newItalicElement(Document doc, String teksti) {
        Element emphasis = doc.createElement("em");
        emphasis.appendChild(doc.createTextNode(teksti));
        return emphasis;
    }

    private String getKokoTeksti(MuodostumisSaanto saanto, Kieli kieli) {
        if (saanto == null || saanto.getKoko() == null) {
            return null;
        }

        MuodostumisSaanto.Koko koko = saanto.getKoko();
        Integer min = koko.getMinimi();
        Integer max = koko.getMaksimi();
        StringBuilder kokoBuilder = new StringBuilder("");
        if (min != null) {
            kokoBuilder.append(min.toString());
        }
        if (min != null && max != null && !min.equals(max)) {
            kokoBuilder.append("-");
        }
        if (max != null && !max.equals(min)) {
            kokoBuilder.append(max.toString());
        }

        String yks = messages.translate("docgen.koko.kpl", kieli);
        kokoBuilder.append(" ");
        kokoBuilder.append(yks);
        return kokoBuilder.toString();
    }

    private String getLaajuusTeksti(MuodostumisSaanto saanto, LaajuusYksikko yksikko, Kieli kieli) {
        if (saanto == null || saanto.getLaajuus() == null) {
            return null;
        }

        MuodostumisSaanto.Laajuus laajuus = saanto.getLaajuus();
        Integer min = laajuus.getMinimi();
        Integer max = laajuus.getMaksimi();
        StringBuilder laajuusBuilder = new StringBuilder("");
        if (min != null) {
            laajuusBuilder.append(min.toString());
        }
        if (min != null && max != null && !min.equals(max)) {
            laajuusBuilder.append("-");
        }
        if (max != null && !max.equals(min)) {
            laajuusBuilder.append(max.toString());
        }

        String yks = messages.translate("docgen.laajuus.ov", kieli);
        if (yksikko == LaajuusYksikko.OSAAMISPISTE) {
            yks = messages.translate("docgen.laajuus.osp", kieli);
        }

        laajuusBuilder.append(" ");
        laajuusBuilder.append(yks);
        return laajuusBuilder.toString();
    }

    private void printDocument(Document doc) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
        LOG.debug(out.toString());
    }
}
