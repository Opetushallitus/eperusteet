package fi.vm.sade.eperusteet.service.dokumentti.impl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.repository.TutkintonimikeKoodiRepository;
import fi.vm.sade.eperusteet.service.KoodistoClient;
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
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import static fi.vm.sade.eperusteet.service.dokumentti.impl.util.DokumenttiUtils.getTextString;

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

        DokumenttiBase docBase = new DokumenttiBase();
        docBase.setDocument(doc);
        docBase.setHeadElement(headElement);
        docBase.setBodyElement(bodyElement);
        docBase.setGenerator(new CharapterNumberGenerator());
        docBase.setKieli(kieli);
        docBase.setPeruste(peruste);
        docBase.setDokumentti(dokumentti);
        docBase.setMapper(mapper);

        // Kansilehti & Infosivu
        addMetaPages(docBase);

        // sisältöelementit (proosa)
        addSisaltoelementit(docBase);

        // pudotellaan tutkinnonosat paikalleen
        addTutkinnonosat(docBase);

        // lisätään tekstikappaleet
        addTekstikappaleet(docBase);

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
        String kuvaus = getTextString(docBase, docBase.getPeruste().getKuvaus()).replaceAll("<[^>]+>", "");
        if (kuvaus != null && kuvaus.length() != 0) {
            Element description = docBase.getDocument().createElement("meta");
            description.setAttribute("name", "description");
            description.setAttribute("content", kuvaus);
            docBase.getHeadElement().appendChild(description);
        }

        // Diaarinumero
        if (docBase.getPeruste().getDiaarinumero() != null) {
            Element description = docBase.getDocument().createElement("meta");
            description.setAttribute("name", "diary");
            description.setAttribute("content", docBase.getPeruste().getDiaarinumero()
                    .toString().replaceAll("<[^>]+>", ""));
            docBase.getHeadElement().appendChild(description);
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

    }

    private void addTutkinnonosat(DokumenttiBase docBase) {

    }

    private void addTekstikappaleet(DokumenttiBase docBase) {

    }
}
