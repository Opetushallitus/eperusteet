package fi.vm.sade.eperusteet.service.dokumentti.impl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.ammattitaitovaatimukset.AmmattitaitovaatimuksenKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohde;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.*;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.*;
import fi.vm.sade.eperusteet.domain.tuva.KoulutuksenOsa;
import fi.vm.sade.eperusteet.domain.tuva.TuvaLaajaAlainenOsaaminen;
import fi.vm.sade.eperusteet.domain.vst.KotoKielitaitotaso;
import fi.vm.sade.eperusteet.domain.vst.KotoLaajaAlainenOsaaminen;
import fi.vm.sade.eperusteet.domain.vst.KotoOpinto;
import fi.vm.sade.eperusteet.domain.vst.KotoSisalto;
import fi.vm.sade.eperusteet.domain.vst.Opintokokonaisuus;
import fi.vm.sade.eperusteet.domain.vst.TavoiteAlueTyyppi;
import fi.vm.sade.eperusteet.domain.vst.Tavoitesisaltoalue;
import fi.vm.sade.eperusteet.domain.yl.*;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteJulkinenDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.Ammattitaitovaatimus2019Dto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.repository.TermistoRepository;
import fi.vm.sade.eperusteet.repository.TutkintonimikeKoodiRepository;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.LiiteService;
import fi.vm.sade.eperusteet.service.LocalizedMessagesService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiNewBuilderService;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.CharapterNumberGenerator;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.DokumenttiPeruste;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.DokumenttiRivi;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.DokumenttiRiviTyyppi;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.DokumenttiTaulukko;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static fi.vm.sade.eperusteet.service.dokumentti.impl.util.DokumenttiUtils.*;

/**
 * @author isaul
 */
@Slf4j
@Service
@Profile("!test")
public class DokumenttiNewBuilderServiceImpl implements DokumenttiNewBuilderService {

    private static final float COMPRESSION_LEVEL = 0.9f;

    @Autowired
    private TutkintonimikeKoodiRepository tutkintonimikeKoodiRepository;

    @Autowired
    private LiiteService liiteService;

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private KoodistoClient koodistoService;

    @Autowired
    private LocalizedMessagesService messages;

    @Autowired
    private TermistoRepository termistoRepository;

    @Autowired
    private PerusteService perusteService;

    @Override
    public Document generateXML(Peruste peruste, Dokumentti dokumentti)
            throws ParserConfigurationException, IOException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        // Luodaan XHTML pohja
        Element rootElement = doc.createElement("html");
        rootElement.setAttribute("lang", dokumentti.getKieli().toString());
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
        DokumenttiPeruste docBase = new DokumenttiPeruste();
        docBase.setDocument(doc);
        docBase.setHeadElement(headElement);
        docBase.setBodyElement(bodyElement);
        docBase.setGenerator(new CharapterNumberGenerator());
        docBase.setKieli(dokumentti.getKieli());
        docBase.setPeruste(peruste);
        docBase.setKvLiiteJulkinenDto(perusteService.getJulkinenKVLiite(peruste.getId()));
        docBase.setDokumentti(dokumentti);
        docBase.setMapper(mapper);
        docBase.setSisalto(peruste.getSisallot(dokumentti.getSuoritustapakoodi()));
        docBase.setAipeOpetuksenSisalto(peruste.getAipeOpetuksenPerusteenSisalto());

        // Tästä aloitetaan varsinaisen dokumentin muodostus
        addDokumentti(docBase);

        return doc;
    }

    private void addDokumentti(DokumenttiPeruste docBase) {
        // Kansilehti & Infosivu
        addMetaPages(docBase);

        // Aipe sisältö
        addAipeSisalto(docBase);

        // Tutkinnon muodostuminen
        addTutkinnonMuodostuminen(docBase);

        // Tutkinnonosat
        addTutkinnonosat(docBase);

        // Tekstikappaleet
        addPerusteenOsat(docBase);

        // Alaviitteet
        addFootnotes(docBase);

        // Kuvat
        buildImages(docBase);
    }

    private void addFootnotes(DokumenttiPeruste docBase) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression expression = xpath.compile("//abbr");
            NodeList list = (NodeList) expression.evaluate(docBase.getDocument(), XPathConstants.NODESET);

            int noteNumber = 1;
            for (int i = 0; i < list.getLength(); i++) {
                Element element = (Element) list.item(i);
                element.setAttribute("text", element.getTextContent());

                Node node = list.item(i);
                if (node.getAttributes() != null & node.getAttributes().getNamedItem("data-viite") != null) {
                    String avain = node.getAttributes().getNamedItem("data-viite").getNodeValue();

                    if (docBase.getPeruste() != null && docBase.getPeruste().getId() != null) {
                        Termi termi = termistoRepository.findByPerusteIdAndAvain(docBase.getPeruste().getId(), avain);

                        if (termi != null && termi.isAlaviite() && termi.getSelitys() != null) {
                            element.setAttribute("number", String.valueOf(noteNumber));

                            TekstiPalanen tekstiDto = termi.getSelitys();
                            String selitys = getTextString(docBase, tekstiDto)
                                    .replaceAll("<(?!\\/?(a)(>|\\s))[^<]+?>", "");
                            addTeksti(docBase, selitys, "attrfootnote", element);
                            noteNumber++;
                        }
                    }
                }
            }

        } catch (XPathExpressionException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    private void addMetaPages(DokumenttiPeruste docBase) {
        // Nimi
        Element title = docBase.getDocument().createElement("title");
        String nimi = getTextString(docBase, docBase.getPeruste().getNimi());
        docBase.getDocument().getDocumentElement().setAttribute("opetushallitus", messages.translate("opetushallitus", docBase.getKieli()));

        if (nimi != null && nimi.length() != 0) {
            title.appendChild(docBase.getDocument().createTextNode(nimi));
            docBase.getHeadElement().appendChild(title);

            // Perusteen nimi
            Element perusteenNimi = docBase.getDocument().createElement("meta");
            perusteenNimi.setAttribute("name", "perusteenNimi");

            if (docBase.getPeruste().getTyyppi().equals(PerusteTyyppi.OPAS)) {
                Element opas = docBase.getDocument().createElement("opas");
                opas.setTextContent(nimi);
                docBase.getHeadElement().appendChild(opas);
                perusteenNimi.setAttribute("translate", messages.translate("oppaan-nimi", docBase.getKieli()));
            } else {
                Element peruste = docBase.getDocument().createElement("peruste");
                peruste.setTextContent(nimi);
                docBase.getHeadElement().appendChild(peruste);
                perusteenNimi.setAttribute("translate", messages.translate("perusteen-nimi", docBase.getKieli()));
            }

            docBase.getHeadElement().appendChild(perusteenNimi);
        }

        if (KoulutustyyppiToteutus.AMMATILLINEN.equals(docBase.getPeruste().getToteutus())) {
            Element etusivuYlaviite = docBase.getDocument().createElement("meta");
            etusivuYlaviite.setAttribute("name", "etusivuYlaviite");
            etusivuYlaviite.setAttribute("translate", messages.translate("tutkinnon-perusteet", docBase.getKieli()));
            docBase.getHeadElement().appendChild(etusivuYlaviite);
        }

        {
            Element description = docBase.getDocument().createElement("description");
            docBase.getHeadElement().appendChild(description);

            KVLiiteJulkinenDto kvLiiteJulkinenDto = docBase.getKvLiiteJulkinenDto();
            if (kvLiiteJulkinenDto != null) {

                // Tutkinnon suorittaneen osaaminen
                String suorittaneenOsaaminen = getTextString(docBase, kvLiiteJulkinenDto.getSuorittaneenOsaaminen());
                if (!ObjectUtils.isEmpty(suorittaneenOsaaminen)) {
                    addTeksti(docBase,
                            messages.translate("docgen.kvliite.tutkinnon-suorittaneen-osaaminen", docBase.getKieli()),
                            "h6",
                            description);
                    addTeksti(docBase, suorittaneenOsaaminen, "div", description);
                }

                // Työtehtäviä, joissa tutkinnon suorittanut voi toimia
                String tyotehtavat = getTextString(docBase, kvLiiteJulkinenDto.getTyotehtavatJoissaVoiToimia());
                if (!ObjectUtils.isEmpty(tyotehtavat)) {
                    addTeksti(docBase,
                            messages.translate("docgen.kvliite.tyotehtavat", docBase.getKieli()),
                            "h6",
                            description);
                    addTeksti(docBase, tyotehtavat, "div", description);
                }

            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        // sisallysluettelo
        Element sisalto = docBase.getDocument().createElement("meta");
        sisalto.setAttribute("name", "sisalto");
        sisalto.setAttribute("translate", messages.translate("sisalto", docBase.getKieli()));
        docBase.getHeadElement().appendChild(sisalto);

        // Voimaantulo
        if (docBase.getPeruste().getVoimassaoloAlkaa() != null) {
            Element description = docBase.getDocument().createElement("meta");
            description.setAttribute("name", "voimaantulo");
            description.setAttribute("content", dateFormat.format(docBase.getPeruste().getVoimassaoloAlkaa()));
            description.setAttribute("translate", messages.translate("voimaantulo", docBase.getKieli()));
            docBase.getHeadElement().appendChild(description);
        }

        // Voimaantulon päättyminen
        if (docBase.getPeruste().getVoimassaoloLoppuu() != null) {
            Element description = docBase.getDocument().createElement("meta");
            description.setAttribute("name", "voimassaolo-paattyminen");
            description.setAttribute("content", dateFormat.format(docBase.getPeruste().getVoimassaoloLoppuu()));
            description.setAttribute("translate", messages.translate("voimassaolo-paattyminen", docBase.getKieli()));
            docBase.getHeadElement().appendChild(description);
        }

        Element pdfluotu = docBase.getDocument().createElement("meta");
        pdfluotu.setAttribute("name", "pdfluotu");
        pdfluotu.setAttribute("content", dateFormat.format(new Date()));
        pdfluotu.setAttribute("translate", messages.translate("docgen.pdf-luotu", docBase.getKieli()));
        docBase.getHeadElement().appendChild(pdfluotu);

        // Oppaille ei lisätä perusteiden tietoja
        if (docBase.getPeruste().getTyyppi() == PerusteTyyppi.OPAS) {
            return;
        }

        // Diaarinumero
        if (docBase.getPeruste().getDiaarinumero() != null) {
            Element diary = docBase.getDocument().createElement("meta");
            diary.setAttribute("name", "diary");
            diary.setAttribute("content", docBase.getPeruste().getDiaarinumero().toString());
            diary.setAttribute("translate", messages.translate("maarayksen-diaarinumero", docBase.getKieli()));
            docBase.getHeadElement().appendChild(diary);
        }

        // Korvaavat määräykset
        if (docBase.getPeruste().getKorvattavatDiaarinumerot() != null
                && !docBase.getPeruste().getKorvattavatDiaarinumerot().isEmpty()) {
            Element korvaavat = docBase.getDocument().createElement("korvaavat");
            korvaavat.setAttribute("translate", messages.translate("korvattavat-maaraykset", docBase.getKieli()));

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
            muutosmaaraykset.setAttribute("translate", messages.translate("muutosmaaraykset", docBase.getKieli()));

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
            koulutukset.setAttribute("translate", messages.translate("koulutukset", docBase.getKieli()));

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
            osaamisalat.setAttribute("translate", messages.translate("osaamisalat", docBase.getKieli()));

            docBase.getPeruste().getOsaamisalat().stream()
                    .map(oa -> mapper.map(oa, KoodiDto.class))
                    .forEach(osaamisala -> {
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
            tutkintonimikkeet.setAttribute("translate", messages.translate("tutkintonimikkeet", docBase.getKieli()));

            nimikeKoodit.forEach(tnkoodi -> {
                if (tnkoodi.getTutkintonimikeUri().startsWith("temporary")) {
                    Element tutkintonimike = docBase.getDocument().createElement("tutkintonimike");
                    tutkintonimike.setTextContent(getTextString(docBase, tnkoodi.getNimi()));
                    tutkintonimikkeet.appendChild(tutkintonimike);
                } else {
                    KoodistoKoodiDto koodiDto = koodistoService.get("tutkintonimikkeet", tnkoodi.getTutkintonimikeUri());

                    for (KoodistoMetadataDto meta : koodiDto.getMetadata()) {
                        if (meta.getKieli().toLowerCase().equals(docBase.getKieli().toString().toLowerCase())) {
                            Element tutkintonimike = docBase.getDocument().createElement("tutkintonimike");
                            tutkintonimike.setTextContent(meta.getNimi() + " (" + tnkoodi.getTutkintonimikeArvo() + ")");
                            tutkintonimikkeet.appendChild(tutkintonimike);
                        } else {
                            log.warn("{} was no match", meta.getKieli());
                        }
                    }
                }
            });
            if (tutkintonimikkeet.hasChildNodes()) {
                docBase.getHeadElement().appendChild(tutkintonimikkeet);
            }
        }
    }

    private void addTutkinnonMuodostuminen(DokumenttiPeruste docBase) {
        if (!Optional.ofNullable(docBase.getSisalto())
                .map(PerusteenOsaViite::getSuoritustapa)
                .map(Suoritustapa::getRakenne).isPresent()) {
            return;
        }

        RakenneModuuli rakenne = docBase.getSisalto().getSuoritustapa().getRakenne();
        if (KoulutusTyyppi.of(docBase.getPeruste().getKoulutustyyppi()).equals(KoulutusTyyppi.VALMA)
                || KoulutusTyyppi.of(docBase.getPeruste().getKoulutustyyppi()).equals(KoulutusTyyppi.TELMA)) {
            addHeader(docBase, messages.translate("docgen.koulutuksen_muodostuminen.title", docBase.getKieli()));
        } else {
            addHeader(docBase, messages.translate("docgen.tutkinnon_muodostuminen.title", docBase.getKieli()));
        }

        String kuvaus = getTextString(docBase, rakenne.getKuvaus());
        if (StringUtils.isNotEmpty(kuvaus)) {
            addTeksti(docBase, kuvaus, "div");
        }

        // Luodaan muodostumistaulukko
        Element taulukko = docBase.getDocument().createElement("table");
        taulukko.setAttribute("border", "1");
        docBase.getBodyElement().appendChild(taulukko);
        Element tbody = docBase.getDocument().createElement("tbody");
        taulukko.appendChild(tbody);

        addRakenneOsa(docBase, rakenne, tbody, 0);

        docBase.getGenerator().increaseNumber();
    }

    private void addRakenneOsa(DokumenttiPeruste docBase, AbstractRakenneOsa osa, Element tbody, int depth) {
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

            String nimi = getOtsikko(docBase, rakenneOsa.getTutkinnonOsaViite(), false);
            String kuvaus = getTextString(docBase, rakenneOsa.getKuvaus());

            Element tr = docBase.getDocument().createElement("tr");
            Element td = docBase.getDocument().createElement("td");
            td.setAttribute("class", "td" + String.valueOf(depth));
            Element p = docBase.getDocument().createElement("p");

            tbody.appendChild(tr);
            tr.appendChild(td);
            td.appendChild(p);
            p.setTextContent(nimi);
            if (StringUtils.isNotEmpty(kuvaus)) {
                td.appendChild(newItalicElement(docBase, kuvaus));
            }

            if (rakenneOsa.getPakollinen() != null && rakenneOsa.getPakollinen()) {
                String glyph = messages.translate("docgen.rakenneosa.pakollinen.glyph", docBase.getKieli());
                p.appendChild(docBase.getDocument().createTextNode(", "));
                p.appendChild(newBoldElement(docBase.getDocument(), glyph));
            }
        }
    }

    private void addRakenneModuuli(DokumenttiPeruste docBase, RakenneModuuli rakenneModuuli, Element tbody, int depth) {
        RakenneModuuliDto rakenneModuuliDto = mapper.map(rakenneModuuli, RakenneModuuliDto.class);
        String nimi = getTextString(docBase, rakenneModuuliDto.getNimi());
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
        th.setAttribute("class", "th" + String.valueOf(depth));
        Element td = docBase.getDocument().createElement("td");
        td.setAttribute("class", "td" + String.valueOf(depth));
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
                    th.appendChild(newItalicElement(docBase, kuvaus));
                }

                break;
            case 2:
                tr.setAttribute("bgcolor", "#EEEEEE");

                tbody.appendChild(tr);
                tr.appendChild(td);
                td.appendChild(p);
                p.appendChild(newBoldElement(docBase.getDocument(), nimi));
                if (StringUtils.isNotEmpty(kuvaus)) {
                    td.appendChild(newItalicElement(docBase, kuvaus));
                }

                break;

            default:
                tbody.appendChild(tr);
                tr.appendChild(td);
                td.appendChild(p);
                p.appendChild(newBoldElement(docBase.getDocument(), nimi));
                if (StringUtils.isNotEmpty(kuvaus)) {
                    td.appendChild(newItalicElement(docBase, kuvaus));
                }

                break;
        }
    }

    private void addTutkinnonosat(DokumenttiPeruste docBase) {
        Set<Suoritustapa> suoritustavat = docBase.getPeruste().getSuoritustavat();
        if (suoritustavat.size() == 0) {
            return;
        }

        Set<TutkinnonOsaViite> osat = new TreeSet<>((o1, o2) -> {
            String nimi1 = getTextString(docBase, mapper.map(o1.getTutkinnonOsa(), TutkinnonOsaDto.class).getNimi());
            String nimi2 = getTextString(docBase, mapper.map(o2.getTutkinnonOsa(), TutkinnonOsaDto.class).getNimi());

            // Ensisijaisesti järjestysnumeron mukaan
            int o1i = o1.getJarjestys() != null ? o1.getJarjestys() : Integer.MAX_VALUE;
            int o2i = o2.getJarjestys() != null ? o2.getJarjestys() : Integer.MAX_VALUE;
            if (o1i < o2i) {
                return -1;
            } else if (o1i > o2i) {
                return 1;
            }

            // Toissijaisesti aakkosjärjestyksessä
            if (!nimi1.equals(nimi2)) {
                return nimi1.compareTo(nimi2);
            }

            // Viimeisenä kanta-avaimen mukaan
            Long id1 = o1.getTutkinnonOsa().getId();
            Long id2 = o2.getTutkinnonOsa().getId();
            if (id1 < id2) {
                return -1;
            } else if (id1 > id2) {
                return 1;
            }

            // Ovat samat
            return 0;
        });

        Optional.ofNullable(docBase.getSisalto())
                .map(PerusteenOsaViite::getSuoritustapa)
                .map(Suoritustapa::getSuoritustapakoodi)
                .ifPresent(suoritustapakoodi-> suoritustavat.stream()
                        .filter(suoritustapa -> suoritustapa.getSuoritustapakoodi()
                                .equals(suoritustapakoodi))
                        .forEach(suoritustapa -> osat.addAll(suoritustapa.getTutkinnonOsat())));
        if (KoulutusTyyppi.of(docBase.getPeruste().getKoulutustyyppi()).equals(KoulutusTyyppi.VALMA)
                || KoulutusTyyppi.of(docBase.getPeruste().getKoulutustyyppi()).equals(KoulutusTyyppi.TELMA)) {
            addHeader(docBase, messages.translate("docgen.koulutuksen_osat.title", docBase.getKieli()));
        } else {
            addHeader(docBase, messages.translate("docgen.tutkinnon_osat.title", docBase.getKieli()));
        }

        docBase.getGenerator().increaseDepth();

        osat.forEach(viite -> {
            TutkinnonOsa osa = viite.getTutkinnonOsa();

            String otsikko = getOtsikko(docBase, viite);
            addHeader(docBase, otsikko);

            String kuvaus = getTextString(docBase, osa.getKuvaus());
            if (StringUtils.isNotEmpty(kuvaus)) {
                addTeksti(docBase, kuvaus, "div");
            }

            TutkinnonOsaTyyppi tyyppi = osa.getTyyppi();
            if (tyyppi == TutkinnonOsaTyyppi.NORMAALI) {
                addTavoitteet(docBase, osa);
                addAmmattitaitovaatimukset(docBase,
                        osa.getAmmattitaitovaatimukset2019(),
                        osa.getAmmattitaitovaatimuksetLista(),
                        osa.getAmmattitaitovaatimukset());
                addGeneerinenArviointi(docBase, osa.getGeneerinenArviointiasteikko());
                if (osa.getGeneerinenArviointiasteikko() == null) {
                    addArviointi(docBase, osa.getArviointi(), tyyppi);
                }
                addValmatelmaSisalto(docBase, osa.getValmaTelmaSisalto());
                addAmmattitaidonOsoittamistavat(docBase, osa);
                addVapaatTekstit(docBase, osa);
            } else if (TutkinnonOsaTyyppi.isTutke(tyyppi)) {
                addTutke2Osat(docBase, osa);
            }

            docBase.getGenerator().increaseNumber();
        });

        docBase.getGenerator().decreaseDepth();
        docBase.getGenerator().increaseNumber();
    }

    private void addPerusteenOsat(DokumenttiPeruste docBase) {
        PerusteenOsaViite sisalto = docBase.getSisalto();
        if (sisalto == null) {
            return;
        }

        addPerusteenOsat(docBase, sisalto);
        addTekstikappaleLiitteet(docBase, sisalto);
    }

    private void addPerusteenOsat(DokumenttiPeruste docBase, PerusteenOsaViite parent) {
        if (parent == null) {
            return;
        }

        for (PerusteenOsaViite lapsi : parent.getLapset()) {
            PerusteenOsa po = lapsi.getPerusteenOsa();
            if (po == null) {
                continue;
            }
            if (po instanceof Taiteenala) {
                Taiteenala taiteenala = (Taiteenala) po;
                addTaiteenala(docBase, taiteenala, po, lapsi);
            } else if (po instanceof Opintokokonaisuus) {
                Opintokokonaisuus opintokokonaisuus = (Opintokokonaisuus) po;
                addOpintokokonaisuus(docBase, opintokokonaisuus, po, lapsi);
            } else if (po instanceof Tavoitesisaltoalue) {
                Tavoitesisaltoalue tavoitesisaltoalue = (Tavoitesisaltoalue) po;
                addTavoitesisaltoalue(docBase, tavoitesisaltoalue, po, lapsi);
            } else if (po instanceof KoulutuksenOsa) {
                KoulutuksenOsa koulutuksenOsa = (KoulutuksenOsa) po;
                addKoulutuksenOsa(docBase, koulutuksenOsa, po, lapsi);
            } else if (po instanceof TuvaLaajaAlainenOsaaminen) {
                TuvaLaajaAlainenOsaaminen tuvaLaajaAlainenOsaaminen = (TuvaLaajaAlainenOsaaminen) po;
                addTuvaLaajaAlainenOsaaminen(docBase, tuvaLaajaAlainenOsaaminen, po, lapsi);
            } else if (po instanceof KotoKielitaitotaso) {
                KotoKielitaitotaso kotoKielitaitotaso = (KotoKielitaitotaso) po;
                addKotoSisalto(docBase, kotoKielitaitotaso, po, lapsi);
            } else if (po instanceof KotoOpinto) {
                KotoOpinto kotoOpinto = (KotoOpinto) po;
                addKotoSisalto(docBase, kotoOpinto, po, lapsi);
            } else if (po instanceof KotoLaajaAlainenOsaaminen) {
                KotoLaajaAlainenOsaaminen kotoLao = (KotoLaajaAlainenOsaaminen) po;
                addKotoLaajaAlainenOsaaminen(docBase, kotoLao, po, lapsi);
            } else if (po instanceof TekstiKappale) {
                TekstiKappale tk = (TekstiKappale) po;
                if (!tk.isLiite()) {
                    addTekstikappale(docBase, tk, po, lapsi);
                }
            }
        }
    }

    private void addTekstikappaleLiitteet(DokumenttiPeruste docBase, PerusteenOsaViite parent) {
        if (parent == null) {
            return;
        }

        for (PerusteenOsaViite lapsi : parent.getLapset()) {
            PerusteenOsa po = lapsi.getPerusteenOsa();
            if (po == null) {
                continue;
            }
            if (po instanceof TekstiKappale) {
                TekstiKappale tk = (TekstiKappale) po;
                if (tk.isLiite()) {
                    addTekstikappale(docBase, tk, po, lapsi);
                }
            }

            addTekstikappaleLiitteet(docBase, lapsi);
        }
    }

    private void addTuvaLaajaAlainenOsaaminen(DokumenttiPeruste docBase, TuvaLaajaAlainenOsaaminen tuvaLaajaAlainenOsaaminen, PerusteenOsa po, PerusteenOsaViite lapsi) {

        KoodiDto nimiKoodiDto = mapper.map(tuvaLaajaAlainenOsaaminen.getNimiKoodi(), KoodiDto.class);
        if (nimiKoodiDto != null) {
            addHeader(docBase, getTextString(docBase, nimiKoodiDto.getNimi()));
        } else {
            addHeader(docBase, messages.translate("docgen.nimeton.laaja_alainenosaaminen", docBase.getKieli()));
        }

        String teksti = getTextString(docBase, tuvaLaajaAlainenOsaaminen.getTeksti());
        addTeksti(docBase, teksti, "div");

        docBase.getGenerator().increaseDepth();

        // Rekursiivisesti
        addPerusteenOsat(docBase, lapsi);

        docBase.getGenerator().decreaseDepth();
        docBase.getGenerator().increaseNumber();
    }

    private void addTekstikappale(DokumenttiPeruste docBase, TekstiKappale tk, PerusteenOsa po,
                                  PerusteenOsaViite lapsi) {
        PerusteenOsaTunniste tunniste = po.getTunniste();
        if (tunniste != PerusteenOsaTunniste.NORMAALI
                && tunniste != PerusteenOsaTunniste.LAAJAALAINENOSAAMINEN
                && tunniste != PerusteenOsaTunniste.RAKENNE) {
            String nimi = getTextString(docBase, tk.getNimi());
            addHeader(docBase, nimi);

            String teksti = getTextString(docBase, tk.getTeksti());
            addTeksti(docBase, teksti, "div");

            docBase.getGenerator().increaseDepth();

            // Rekursiivisesti
            addPerusteenOsat(docBase, lapsi);

            docBase.getGenerator().decreaseDepth();

        } else if (tunniste == PerusteenOsaTunniste.LAAJAALAINENOSAAMINEN
                && docBase.getAipeOpetuksenSisalto() != null) {
            AIPEOpetuksenSisalto aipeOpetuksenSisalto = docBase.getAipeOpetuksenSisalto();

            List<LaajaalainenOsaaminen> laajaalaisetosaamiset = aipeOpetuksenSisalto.getLaajaalaisetosaamiset();
            if (laajaalaisetosaamiset.size() > 0) {
                addHeader(docBase, messages.translate("docgen.laaja_alaiset_osaamiset.title", docBase.getKieli()));

                laajaalaisetosaamiset.forEach(laajaalainenOsaaminen -> {
                    String nimi = getTextString(docBase, laajaalainenOsaaminen.getNimi());
                    addTeksti(docBase, nimi, "h5");

                    String teksti = getTextString(docBase, laajaalainenOsaaminen.getKuvaus());
                    addTeksti(docBase, teksti, "div");
                });
            }
        }
        docBase.getGenerator().increaseNumber();
    }

    private void addTaiteenala(DokumenttiPeruste docBase, Taiteenala taiteenala, PerusteenOsa po,
                               PerusteenOsaViite lapsi) {

        // Nimi
        TekstiPalanen nimi = taiteenala.getNimi();
        addHeader(docBase, getTextString(docBase, nimi));

        // Kuvaus
        String kuvaus = getTextString(docBase, taiteenala.getTeksti());
        if (StringUtils.isNotEmpty(kuvaus)) {
            addTeksti(docBase, kuvaus, "div");
        }

        // Koodi
        Koodi koodi = taiteenala.getKoodi();
        if (koodi != null) {
            KoodiDto koodiDto = mapper.map(koodi, KoodiDto.class);
            addTeksti(docBase, messages.translate("docgen.taiteenala.koodi", docBase.getKieli()), "h5");
            addTeksti(docBase, koodiDto.getArvo(), "div");
        }


        // Aikuisten opetus
        addTaiteenalaSisalto(docBase, taiteenala.getAikuistenOpetus(), "docgen.taiteenala.aikuisten-opetus");

        // Kasvatus
        addTaiteenalaSisalto(docBase, taiteenala.getKasvatus(), "docgen.taiteenala.kasvatus");

        // Oppimisen arviointi
        addTaiteenalaSisalto(docBase,
                taiteenala.getOppimisenArviointiOpetuksessa(), "docgen.taiteenala.oppimisen-arvionti");

        // Teemaopinnot
        addTaiteenalaSisalto(docBase, taiteenala.getTeemaopinnot(), "docgen.taiteenala.teemaopinnot");

        // Työtavat opetuksessa
        addTaiteenalaSisalto(docBase, taiteenala.getTyotavatOpetuksessa(), "docgen.taiteenala.tyotavat");

        // Yhteiset opinnot
        addTaiteenalaSisalto(docBase, taiteenala.getYhteisetOpinnot(), "docgen.taiteenala.yhteiset-opinnot");
    }

    private void addOpintokokonaisuus(DokumenttiPeruste docBase, Opintokokonaisuus opintokokonaisuus, PerusteenOsa po,
                                      PerusteenOsaViite lapsi) {

        // Nimi
        KoodiDto nimiKoodiDto = mapper.map(opintokokonaisuus.getNimiKoodi(), KoodiDto.class);
        if (nimiKoodiDto != null) {
            String laajuusSuffix = ", " + opintokokonaisuus.getMinimilaajuus() + " " + messages.translate("docgen.laajuus.op", docBase.getKieli());
            addHeader(docBase, getTextString(docBase, nimiKoodiDto.getNimi()) + laajuusSuffix);
        } else {
            addHeader(docBase, messages.translate("docgen.opintokokonaisuus.nimeton-opintokokonaisuus", docBase.getKieli()));
        }

        // Kuvaus
        String kuvaus = getTextString(docBase, opintokokonaisuus.getKuvaus());
        if (StringUtils.isNotEmpty(kuvaus)) {
            addTeksti(docBase, kuvaus, "div");
        }

        addTeksti(docBase, messages.translate("docgen.opetuksen-tavoitteet.title", docBase.getKieli()), "h5");
        addTeksti(docBase, getTextString(docBase, opintokokonaisuus.getOpetuksenTavoiteOtsikko()), "h6");

        Element tavoitteetEl = docBase.getDocument().createElement("ul");

        opintokokonaisuus.getOpetuksenTavoitteet().forEach(tavoite -> {
            Element tavoiteEl = docBase.getDocument().createElement("li");
            KoodiDto tavoiteKoodiDto = mapper.map(tavoite, KoodiDto.class);

            String rivi = getTextString(docBase, tavoiteKoodiDto.getNimi());
            tavoiteEl.setTextContent(rivi);

            tavoitteetEl.appendChild(tavoiteEl);
        });
        docBase.getBodyElement().appendChild(tavoitteetEl);


        addTeksti(docBase, messages.translate("docgen.arviointi.title", docBase.getKieli()), "h5");
        addTeksti(docBase, messages.translate("docgen.opintokokonaisuus.opiskelijan-osaamisen-arvioinnin-kohteet", docBase.getKieli()), "h6");

        Element arvioinnitEl = docBase.getDocument().createElement("ul");
        opintokokonaisuus.getArvioinnit().forEach(arviointi -> {
            Element arviointiEl = docBase.getDocument().createElement("li");

            String rivi = getTextString(docBase, arviointi);
            arviointiEl.setTextContent(rivi);

            arvioinnitEl.appendChild(arviointiEl);
        });
        docBase.getBodyElement().appendChild(arvioinnitEl);

        docBase.getGenerator().increaseDepth();
        addPerusteenOsat(docBase, lapsi);
        docBase.getGenerator().decreaseDepth();
        docBase.getGenerator().increaseNumber();
    }

    private void addTavoitesisaltoalue(DokumenttiPeruste docBase, Tavoitesisaltoalue tavoitesisaltoalue, PerusteenOsa po,
                                       PerusteenOsaViite lapsi) {

        // Nimi
        KoodiDto nimiKoodiDto = mapper.map(tavoitesisaltoalue.getNimiKoodi(), KoodiDto.class);
        if (nimiKoodiDto != null) {
            addHeader(docBase, getTextString(docBase, nimiKoodiDto.getNimi()));
        } else {
            addHeader(docBase, messages.translate("docgen.opintokokonaisuus.nimeton-tavoitesisaltoalue", docBase.getKieli()));
        }

        // Teksti
        String teksti = getTextString(docBase, tavoitesisaltoalue.getTeksti());
        if (StringUtils.isNotEmpty(teksti)) {
            addTeksti(docBase, teksti, "div");
        }

        addTeksti(docBase, messages.translate("docgen.tavoitteet-ja-keskeiset-sisallot.title", docBase.getKieli()), "h5");

        DokumenttiTaulukko tavoitesisaltoalueTaulukko = new DokumenttiTaulukko();
        tavoitesisaltoalueTaulukko.addOtsikkosarakkeet(
                messages.translate("docgen.tavoitteet.title", docBase.getKieli()),
                messages.translate("docgen.keskeiset-sisaltoalueet.title", docBase.getKieli()));

        tavoitesisaltoalue.getTavoitealueet().forEach(tavoitealue -> {
            DokumenttiRivi rivi = new DokumenttiRivi();

            if (tavoitealue.getTavoiteAlueTyyppi().equals(TavoiteAlueTyyppi.OTSIKKO)) {
                KoodiDto otsikko = mapper.map(tavoitealue.getOtsikko(), KoodiDto.class);
                rivi.addSarake(getTextString(docBase, otsikko.getNimi()));
                rivi.setColspan(2);
                rivi.setTyyppi(DokumenttiRiviTyyppi.SUBHEADER);
            }

            if (tavoitealue.getTavoiteAlueTyyppi().equals(TavoiteAlueTyyppi.TAVOITESISALTOALUE)) {

                if (!CollectionUtils.isEmpty(tavoitealue.getTavoitteet())) {
                    StringBuffer sarake = new StringBuffer();
                    tavoitealue.getTavoitteet().forEach(tavoite -> {
                        KoodiDto tavoiteKoodi = mapper.map(tavoite, KoodiDto.class);
                        sarake.append(tagTeksti(getTextString(docBase, tavoiteKoodi.getNimi()), "div"));
                    });

                    rivi.addSarake(sarake.toString());
                }

                if (!CollectionUtils.isEmpty(tavoitealue.getKeskeisetSisaltoalueet())) {
                    StringBuffer sarake = new StringBuffer();
                    tavoitealue.getKeskeisetSisaltoalueet().forEach(keskeinenSisaltoalue -> {
                        sarake.append(tagTeksti(getTextString(docBase, keskeinenSisaltoalue), "div"));
                    });

                    rivi.addSarake(sarake.toString());
                }
            }

            tavoitesisaltoalueTaulukko.addRivi(rivi);
        });

        tavoitesisaltoalueTaulukko.addToDokumentti(docBase);

        docBase.getGenerator().increaseDepth();
        addPerusteenOsat(docBase, lapsi);
        docBase.getGenerator().decreaseDepth();
        docBase.getGenerator().increaseNumber();
    }

    private void addKoulutuksenOsa(DokumenttiPeruste docBase, KoulutuksenOsa koulutuksenOsa, PerusteenOsa po,
                                   PerusteenOsaViite lapsi) {

        Integer minimiLaajuus = koulutuksenOsa.getLaajuusMinimi() != null ? koulutuksenOsa.getLaajuusMinimi() : koulutuksenOsa.getLaajuusMaksimi() != null ? koulutuksenOsa.getLaajuusMaksimi() : 0;
        Integer maksimiLaajuus = koulutuksenOsa.getLaajuusMaksimi() != null ? koulutuksenOsa.getLaajuusMaksimi() : koulutuksenOsa.getLaajuusMinimi() != null ? koulutuksenOsa.getLaajuusMinimi() : 0;
        String nimi = getTextString(docBase, koulutuksenOsa.getNimi());
        if (koulutuksenOsa.getNimiKoodi() != null) {
            KoodiDto nimiKoodi = mapper.map(koulutuksenOsa.getNimiKoodi(), KoodiDto.class);
            nimi = getTextString(docBase, nimiKoodi.getNimi());
        }


        if (minimiLaajuus.compareTo(maksimiLaajuus) == 0) {
            String nimiSuffix = String.format(", %d %s", minimiLaajuus, messages.translate("docgen.laajuus.vk", docBase.getKieli()));
            addHeader(docBase, nimi + nimiSuffix);
        } else {
            String nimiSuffix = String.format(", %d - %d %s", minimiLaajuus, maksimiLaajuus, messages.translate("docgen.laajuus.vk", docBase.getKieli()));
            addHeader(docBase, nimi + nimiSuffix);
        }

        addTeksti(docBase, messages.translate("docgen.koulutustyyppi.title", docBase.getKieli()), "h5");
        if (koulutuksenOsa.getKoulutusOsanKoulutustyyppi() != null) {
            addTeksti(docBase, messages.translate("docgen.koulutuksenOsa.koulutustyyppi." + koulutuksenOsa.getKoulutusOsanKoulutustyyppi(), docBase.getKieli()), "div");
        }

        String kuvaus = getTextString(docBase, koulutuksenOsa.getKuvaus());
        if (StringUtils.isNotEmpty(kuvaus)) {
            addTeksti(docBase, kuvaus, "div");
        }

        addTeksti(docBase, messages.translate("docgen.opetuksen-tavoitteet.title", docBase.getKieli()), "h5");
        addTeksti(docBase, messages.translate("docgen.info.opiskelija", docBase.getKieli()), "h6");

        Element tavoitteetEl = docBase.getDocument().createElement("ul");

        koulutuksenOsa.getTavoitteet().forEach(tavoite -> {
            Element tavoiteEl = docBase.getDocument().createElement("li");

            String rivi = getTextString(docBase, tavoite);
            tavoiteEl.setTextContent(rivi);

            tavoitteetEl.appendChild(tavoiteEl);
        });
        docBase.getBodyElement().appendChild(tavoitteetEl);

        String laajaAlainenOsaaminenKuvaus = getTextString(docBase, koulutuksenOsa.getLaajaAlaisenOsaamisenKuvaus());
        if (StringUtils.isNotEmpty(laajaAlainenOsaaminenKuvaus)) {
            addTeksti(docBase, messages.translate("docgen.laaja_alainen_osaaminen.title", docBase.getKieli()), "h5");
            addTeksti(docBase, laajaAlainenOsaaminenKuvaus, "div");
        }

        String keskeinenSisalto = getTextString(docBase, koulutuksenOsa.getKeskeinenSisalto());
        if (StringUtils.isNotEmpty(keskeinenSisalto)) {
            addTeksti(docBase, messages.translate("docgen.keskeinen-sisalto.title", docBase.getKieli()), "h5");
            addTeksti(docBase, keskeinenSisalto, "div");
        }

        String arvioinninKuvaus = getTextString(docBase, koulutuksenOsa.getArvioinninKuvaus());
        if (StringUtils.isNotEmpty(arvioinninKuvaus)) {
            addTeksti(docBase, messages.translate("docgen.arviointi.title", docBase.getKieli()), "h5");
            addTeksti(docBase, arvioinninKuvaus, "div");
        }

        docBase.getGenerator().increaseDepth();
        addPerusteenOsat(docBase, lapsi);
        docBase.getGenerator().decreaseDepth();
        docBase.getGenerator().increaseNumber();
    }

    private void addKotoSisalto(DokumenttiPeruste docBase, KotoSisalto kotoSisalto, PerusteenOsa po,
                                PerusteenOsaViite lapsi) {

        KoodiDto nimiKoodi = mapper.map(kotoSisalto.getNimiKoodi(), KoodiDto.class);
        if (nimiKoodi != null) {
            addHeader(docBase, getTextString(docBase, nimiKoodi.getNimi()));
        } else {
            addHeader(docBase, messages.translate("nimeton-sisalto", docBase.getKieli()));
        }

        String kuvaus = getTextString(docBase, kotoSisalto.getKuvaus());
        if (StringUtils.isNotEmpty(kuvaus)) {
            addTeksti(docBase, kuvaus, "div");
        }

        kotoSisalto.getTaitotasot().forEach(taitotaso -> {

            KoodiDto taitotasoNimi = mapper.map(taitotaso.getNimi(), KoodiDto.class);
            addTeksti(docBase, getTextString(docBase, taitotasoNimi.getNimi()), "h5");

            String tavoitteet = getTextString(docBase, taitotaso.getTavoitteet());

            addKotoH6Teksti(tavoitteet, "docgen.tavoitteet.title", docBase);

            String kielenkayttotarkoitus = getTextString(docBase, taitotaso.getKielenkayttotarkoitus());
            String aihealueet = getTextString(docBase, taitotaso.getAihealueet());
            String viestintataidot = getTextString(docBase, taitotaso.getViestintataidot());
            String opiskelijantaidot = getTextString(docBase, taitotaso.getOpiskelijantaidot());

            String opiskelijanTyoelamataidot = getTextString(docBase, taitotaso.getOpiskelijanTyoelamataidot());
            String suullinenVastaanottaminen = getTextString(docBase, taitotaso.getSuullinenVastaanottaminen());
            String suullinenTuottaminen = getTextString(docBase, taitotaso.getSuullinenTuottaminen());
            String vuorovaikutusJaMediaatio = getTextString(docBase, taitotaso.getVuorovaikutusJaMediaatio());

            if (StringUtils.isNotEmpty(kielenkayttotarkoitus)
                    || StringUtils.isNotEmpty(aihealueet)
                    || StringUtils.isNotEmpty(viestintataidot)
                    || StringUtils.isNotEmpty(opiskelijantaidot)
                    || StringUtils.isNotEmpty(opiskelijanTyoelamataidot)
                    || StringUtils.isNotEmpty(suullinenVastaanottaminen)
                    || StringUtils.isNotEmpty(suullinenTuottaminen)
                    || StringUtils.isNotEmpty(vuorovaikutusJaMediaatio)) {
                addTeksti(docBase, messages.translate("docgen.keskeiset-sisallot.title", docBase.getKieli()), "h5");
            }

            addKotoH6Teksti(kielenkayttotarkoitus, "docgen.kielenkayttotarkoitus.title", docBase);
            addKotoH6Teksti(aihealueet, "docgen.aihealueet.title", docBase);
            addKotoH6Teksti(viestintataidot, "docgen.viestintataidot.title", docBase);
            addKotoH6Teksti(opiskelijantaidot, "docgen.opiskelijantaidot.title", docBase);

            addKotoH6Teksti(opiskelijanTyoelamataidot, "docgen.opiskelijan_tyoelamataidot.title", docBase);
            addKotoH6Teksti(suullinenVastaanottaminen, "docgen.suullinen_vastaanottaminen.title", docBase);
            addKotoH6Teksti(suullinenTuottaminen, "docgen.suullinen_tuottaminen.title", docBase);
            addKotoH6Teksti(vuorovaikutusJaMediaatio, "docgen.vuorovaikutus_ja_mediaatio.title", docBase);
        });

        docBase.getGenerator().increaseDepth();
        addPerusteenOsat(docBase, lapsi);
        docBase.getGenerator().decreaseDepth();
        docBase.getGenerator().increaseNumber();
    }

    private void addKotoLaajaAlainenOsaaminen(DokumenttiPeruste docBase, KotoLaajaAlainenOsaaminen kotoLao, PerusteenOsa po,
                                              PerusteenOsaViite lapsi) {

        addHeader(docBase, getTextString(docBase, kotoLao.getNimi()));

        String kuvaus = getTextString(docBase, kotoLao.getYleiskuvaus());
        if (StringUtils.isNotEmpty(kuvaus)) {
            addTeksti(docBase, kuvaus, "div");
        }

        kotoLao.getOsaamisAlueet().forEach(osaamisenAlue -> {

            KoodiDto osaamisalueNimi = mapper.map(osaamisenAlue.getKoodi(), KoodiDto.class);
            addTeksti(docBase, getTextString(docBase, osaamisalueNimi.getNimi()), "h5");

            String osaamisalueKuvaus = getTextString(docBase, osaamisenAlue.getKuvaus());
            if (StringUtils.isNotEmpty(osaamisalueKuvaus)) {
                addTeksti(docBase, osaamisalueKuvaus, "div");
            }
        });

        docBase.getGenerator().increaseDepth();
        addPerusteenOsat(docBase, lapsi);
        docBase.getGenerator().decreaseDepth();
        docBase.getGenerator().increaseNumber();
    }

    private void addKotoH6Teksti(String text, String translationKey, DokumenttiPeruste docBase) {
        if (StringUtils.isNotEmpty(text)) {
            addTeksti(docBase, messages.translate(translationKey, docBase.getKieli()), "h6");
            addTeksti(docBase, text, "div");
        }
    }

    private void addTaiteenalaSisalto(DokumenttiPeruste docBase, KevytTekstiKappale tekstiKappale, String placeholder) {
        if (tekstiKappale != null) {
            String teksti = getTextString(docBase, tekstiKappale.getTeksti());
            if (StringUtils.isNotEmpty(teksti)) {
                String otsikko = getTextString(docBase, tekstiKappale.getNimi());
                if (StringUtils.isNotEmpty(otsikko)) {
                    addTeksti(docBase, otsikko, "h5");
                } else {
                    addTeksti(docBase,
                            messages.translate(placeholder, docBase.getKieli()), "h5");
                }
                addTeksti(docBase, teksti, "div");
            }
        }
    }

    private void addAmmattitaitovaatimukset(
            DokumenttiPeruste docBase,
            Ammattitaitovaatimukset2019 ammattitaitovaatimukset2019,
            List<AmmattitaitovaatimuksenKohdealue> ammattitaitovaatimuksetLista,
            TekstiPalanen ammattitaitovaatimukset
    ) {
        String ammattitaitovaatimuksetText = getTextString(docBase, ammattitaitovaatimukset);
        // Ohitetaan jos ammattitaitovaatimuksia ei ole määritelty millään tavoilla
        if (StringUtils.isEmpty(ammattitaitovaatimuksetText) && CollectionUtils.isEmpty(ammattitaitovaatimuksetLista) && ammattitaitovaatimukset2019 == null) {
            return;
        }

        addTeksti(docBase, messages.translate("docgen.ammattitaitovaatimukset.title", docBase.getKieli()), "h5");

        if (StringUtils.isNotEmpty(ammattitaitovaatimuksetText)) {
            addTeksti(docBase, ammattitaitovaatimuksetText, "div");
        }

        addAmmattitaitovaatimukset2019(docBase, ammattitaitovaatimukset2019);

        if (ammattitaitovaatimuksetLista != null) {
            ammattitaitovaatimuksetLista.forEach(ka -> {
                Element taulukko = docBase.getDocument().createElement("table");
                taulukko.setAttribute("border", "1");
                docBase.getBodyElement().appendChild(taulukko);
                Element tbody = docBase.getDocument().createElement("tbody");
                taulukko.appendChild(tbody);

                Element tr = docBase.getDocument().createElement("tr");
                tr.setAttribute("bgcolor", "#EEEEEE");
                tbody.appendChild(tr);

                Element th = docBase.getDocument().createElement("th");
                th.appendChild(newBoldElement(docBase.getDocument(),
                        getTextString(docBase, ka.getOtsikko())));
                tr.appendChild(th);

                ka.getVaatimuksenKohteet().forEach(kohde -> {
                    Element kohdeRivi = docBase.getDocument().createElement("tr");
                    tbody.appendChild(kohdeRivi);

                    Element kohdeSolu = docBase.getDocument().createElement("td");
                    kohdeSolu.appendChild(newBoldElement(docBase.getDocument(),
                            getTextString(docBase, kohde.getOtsikko())));
                    kohdeRivi.appendChild(kohdeSolu);

                    Element kohdeSelite = docBase.getDocument().createElement("p");
                    kohdeSelite.setTextContent(getTextString(docBase, kohde.getSelite()));
                    kohdeSolu.appendChild(kohdeSelite);

                    Element vaatimusLista = docBase.getDocument().createElement("ul");
                    kohdeSolu.appendChild(vaatimusLista);
                    kohde.getVaatimukset().forEach(vaatimus -> {
                        String ktaso = getTextString(docBase, vaatimus.getSelite());
                        if (vaatimus.getAmmattitaitovaatimusKoodi() != null
                                && !vaatimus.getAmmattitaitovaatimusKoodi().isEmpty()) {
                            ktaso += " (" + vaatimus.getAmmattitaitovaatimusKoodi() + ")";
                        }
                        Element vaatimusAlkio = docBase.getDocument().createElement("li");
                        vaatimusAlkio.setTextContent(ktaso);
                        vaatimusLista.appendChild(vaatimusAlkio);
                    });
                });
            });
        }
    }

    private void addAmmattitaitovaatimukset2019(DokumenttiPeruste docBase, Ammattitaitovaatimukset2019 ammattitaitovaatimukset2019) {
        if (ammattitaitovaatimukset2019 != null) {
            TekstiPalanen kohde = ammattitaitovaatimukset2019.getKohde();
            List<Ammattitaitovaatimus2019> vaatimukset = ammattitaitovaatimukset2019.getVaatimukset();
            List<Ammattitaitovaatimus2019Kohdealue> kohdealueet = ammattitaitovaatimukset2019.getKohdealueet();

            if (!ObjectUtils.isEmpty(vaatimukset) || !ObjectUtils.isEmpty(kohdealueet)) {
                if (kohde != null && !ObjectUtils.isEmpty(vaatimukset)) {
                    addTeksti(docBase, getTextString(docBase, kohde), "p");
                }

                Element listaEl = docBase.getDocument().createElement("ul");
                docBase.getBodyElement().appendChild(listaEl);

                vaatimukset.forEach(vaatimus -> {
                    Element vaatimusEl = docBase.getDocument().createElement("li");
                    String rivi = getTextString(docBase, vaatimus.getVaatimus());
                    vaatimusEl.setTextContent(rivi);
                    listaEl.appendChild(vaatimusEl);
                });

                kohdealueet.forEach(alue -> {
                    Element alueEl = docBase.getDocument().createElement("div");
                    docBase.getBodyElement().appendChild(alueEl);

                    TekstiPalanen kuvaus = alue.getKuvaus();
                    if (kuvaus != null) {
                        Element kuvausEl = docBase.getDocument().createElement("strong");
                        kuvausEl.setTextContent(getTextString(docBase, kuvaus));
                        alueEl.appendChild(kuvausEl);
                    }

                    if (!ObjectUtils.isEmpty(alue.getVaatimukset())) {

                        Element kohdeEl = docBase.getDocument().createElement("p");
                        if (kohde != null) {
                            kohdeEl.setTextContent(getTextString(docBase, kohde));
                        } else {
                            kohdeEl.setTextContent(messages.translate("docgen.info.opiskelija", docBase.getKieli()));
                        }
                        alueEl.appendChild(kohdeEl);

                        Element alueListaEl = docBase.getDocument().createElement("ul");
                        alue.getVaatimukset().forEach(vaatimus -> {
                            Element vaatimusEl = docBase.getDocument().createElement("li");
                            Ammattitaitovaatimus2019Dto vaatimusDto = mapper.map(vaatimus, Ammattitaitovaatimus2019Dto.class);
                            String rivi = getTextString(docBase, vaatimusDto.getVaatimus());
                            vaatimusEl.setTextContent(rivi);
                            alueListaEl.appendChild(vaatimusEl);
                        });
                        alueEl.appendChild(alueListaEl);
                    }

                });
            }
        }
    }

    private void addValmatelmaSisalto(DokumenttiPeruste docBase, ValmaTelmaSisalto valmaTelmaSisalto) {
        if (valmaTelmaSisalto == null) {
            return;
        }

        addValmaOsaamistavoitteet(docBase, valmaTelmaSisalto);
        addValmaArviointi(docBase, valmaTelmaSisalto);
    }

    private void addValmaOsaamistavoitteet(DokumenttiPeruste docBase, ValmaTelmaSisalto valmaTelmaSisalto) {
        if (valmaTelmaSisalto.getOsaamistavoite().size() > 0) {
            addTeksti(docBase, messages.translate("docgen.valma.osaamistavoitteet.title", docBase.getKieli()), "h5");
        }

        for (OsaamisenTavoite osaamisenTavoite : valmaTelmaSisalto.getOsaamistavoite()) {
            if (osaamisenTavoite.getNimi() != null) {
                addTeksti(docBase, getTextString(docBase, osaamisenTavoite.getNimi()), "h6");
            }

            if (osaamisenTavoite.getKohde() != null) {
                addTeksti(docBase, getTextString(docBase, osaamisenTavoite.getKohde()), "div");
            }

            Element lista = docBase.getDocument().createElement("ul");
            docBase.getBodyElement().appendChild(lista);
            osaamisenTavoite.getTavoitteet().forEach(tavoite -> {
                Element alkio = docBase.getDocument().createElement("li");
                alkio.setTextContent(getTextString(docBase, tavoite));
                lista.appendChild(alkio);
            });

            if (osaamisenTavoite.getSelite() != null) {
                addTeksti(docBase, getTextString(docBase, osaamisenTavoite.getSelite()), "div");
            }
        }
    }

    private void addValmaArviointi(DokumenttiPeruste docBase, ValmaTelmaSisalto valmaTelmaSisalto) {
        if (valmaTelmaSisalto.getOsaamisenarviointi() != null || valmaTelmaSisalto.getOsaamisenarviointiTekstina() != null) {
            addTeksti(docBase, messages.translate("docgen.valma.osaamisenarviointi.title", docBase.getKieli()), "h5");

            if (valmaTelmaSisalto.getOsaamisenarviointi() != null) {
                if (valmaTelmaSisalto.getOsaamisenarviointi().getKohde() != null) {
                    addTeksti(docBase,
                            getTextString(docBase, valmaTelmaSisalto.getOsaamisenarviointi().getKohde()),
                            "div");
                }

                Element lista = docBase.getDocument().createElement("ul");
                docBase.getBodyElement().appendChild(lista);
                valmaTelmaSisalto.getOsaamisenarviointi().getTavoitteet().forEach(tavoite -> {
                    Element alkio = docBase.getDocument().createElement("li");
                    alkio.setTextContent(getTextString(docBase, tavoite));
                    lista.appendChild(alkio);
                });
            }

            if (valmaTelmaSisalto.getOsaamisenarviointiTekstina() != null) {
                addTeksti(docBase,
                        valmaTelmaSisalto.getOsaamisenarviointiTekstina().getTeksti().get(docBase.getKieli()),
                        "div");
            }
        }
    }

    private void addAmmattitaidonOsoittamistavat(DokumenttiPeruste docBase, TutkinnonOsa osa) {
        String ammattitaidonOsoittamistavatText = getTextString(docBase, osa.getAmmattitaidonOsoittamistavat());
        if (StringUtils.isEmpty(ammattitaidonOsoittamistavatText)) {
            return;
        }

        addTeksti(docBase, messages.translate("docgen.ammattitaidon_osoittamistavat.title", docBase.getKieli()), "h5");
        addTeksti(docBase, ammattitaidonOsoittamistavatText, "div");
    }

    private void addVapaatTekstit(DokumenttiPeruste docBase, TutkinnonOsa osa) {
        List<KevytTekstiKappale> vapaatTekstit = osa.getVapaatTekstit();
        vapaatTekstit.forEach(vapaaTeksti -> {
            addTeksti(docBase, getTextString(docBase, vapaaTeksti.getNimi()), "h5");
            addTeksti(docBase, getTextString(docBase, vapaaTeksti.getTeksti()), "div");
        });
    }

    private void addGeneerinenArviointi(DokumenttiPeruste docBase, GeneerinenArviointiasteikko geneerinenArviointiasteikko) {
        if (geneerinenArviointiasteikko != null) {
            addTeksti(docBase, messages.translate("docgen.geneerinen-arviointi.title", docBase.getKieli()), "h5");

            TekstiPalanen nimi = geneerinenArviointiasteikko.getNimi();
            TekstiPalanen kohde = geneerinenArviointiasteikko.getKohde();
            Set<GeneerisenOsaamistasonKriteeri> osaamistasonKriteerit = geneerinenArviointiasteikko.getOsaamistasonKriteerit();

            Element taulukko = docBase.getDocument().createElement("table");
            taulukko.setAttribute("border", "1");
            docBase.getBodyElement().appendChild(taulukko);
            Element tbody = docBase.getDocument().createElement("tbody");
            taulukko.appendChild(tbody);

            // Nimi ja kohde
            {
                Element tr = docBase.getDocument().createElement("tr");
                tr.setAttribute("bgcolor", "#EEEEEE");
                tbody.appendChild(tr);

                Element th = docBase.getDocument().createElement("th");
                th.setAttribute("colspan", "4");
                // EP-1996
                // th.appendChild(newBoldElement(docBase.getDocument(), getTextString(docBase, nimi)));
                Element kohdeEl = docBase.getDocument().createElement("p");
                kohdeEl.setTextContent(getTextString(docBase, kohde));
                th.appendChild(kohdeEl);
                tr.appendChild(th);
            }

            // Osaamistason kriteerit
            {
                osaamistasonKriteerit.stream()
                        .filter(ok -> ok.getOsaamistaso() != null && !ObjectUtils.isEmpty(ok.getKriteerit()))
                        .sorted(Comparator.comparing(k -> k.getOsaamistaso().getId()))
                        .forEach(osaamistasonKriteeri -> {
                            Element tr = docBase.getDocument().createElement("tr");
                            tbody.appendChild(tr);

                            Element kriteeriTaso = docBase.getDocument().createElement("td");
                            kriteeriTaso.setAttribute("colspan", "1");
                            kriteeriTaso.setTextContent(getTextString(docBase,
                                    osaamistasonKriteeri.getOsaamistaso().getOtsikko()));
                            tr.appendChild(kriteeriTaso);

                            Element kriteeriKriteerit = docBase.getDocument().createElement("td");
                            kriteeriKriteerit.setAttribute("colspan", "3");

                            Element kriteeriLista = docBase.getDocument().createElement("ul");
                            kriteeriKriteerit.appendChild(kriteeriLista);

                            osaamistasonKriteeri.getKriteerit().forEach(kriteeriKriteeri -> {
                                String kriteeriKriteeriText = getTextString(docBase, kriteeriKriteeri);
                                if (StringUtils.isNotEmpty(kriteeriKriteeriText)) {
                                    addTeksti(docBase, kriteeriKriteeriText, "li", kriteeriLista);
                                }
                            });
                            kriteeriKriteerit.appendChild(kriteeriLista);
                            tr.appendChild(kriteeriKriteerit);
                        });
            }
        }
    }

    private void addArviointi(
            DokumenttiPeruste docBase,
            Arviointi arviointi,
            TutkinnonOsaTyyppi tyyppi
    ) {
        if (arviointi == null) {
            return;
        }

        List<ArvioinninKohdealue> arvioinninKohdealueet = sanitizeList(arviointi.getArvioinninKohdealueet());
        if (tyyppi == TutkinnonOsaTyyppi.REFORMI_TUTKE2) {
            if (!arvioinninKohdealueet.isEmpty()) {
                ArvioinninKohdealue arvioinninKohdealue = arvioinninKohdealueet.get(0);
                if (arvioinninKohdealue.getArvioinninKohteet().isEmpty()) {
                    return;
                }
            }
        }

        addTeksti(docBase, messages.translate("docgen.arviointi.title", docBase.getKieli()), "h5");

        String lisatietoteksti = getTextString(docBase, arviointi.getLisatiedot());
        if (StringUtils.isNotEmpty(lisatietoteksti)) {
            addTeksti(docBase, lisatietoteksti, "div");
        }

        for (ArvioinninKohdealue ka : arvioinninKohdealueet) {
            List<ArvioinninKohde> arvioinninKohteet = ka.getArvioinninKohteet();
            if (arvioinninKohteet == null) {
                continue;
            } else if (arvioinninKohteet.isEmpty()) {
                continue;
            }

            if (TutkinnonOsaTyyppi.NORMAALI.equals(tyyppi)) {
                String otsikkoTeksti = getTextString(docBase, ka.getOtsikko());
                addTeksti(docBase, otsikkoTeksti, "h6");
            }

            for (ArvioinninKohde kohde : arvioinninKohteet) {
                TekstiPalanen otsikko = kohde.getOtsikko();
                TekstiPalanen selite = kohde.getSelite();

                Element taulukko = docBase.getDocument().createElement("table");
                taulukko.setAttribute("border", "1");
                docBase.getBodyElement().appendChild(taulukko);
                Element tbody = docBase.getDocument().createElement("tbody");
                taulukko.appendChild(tbody);

                if (otsikko != null && otsikko.getTeksti().containsKey(docBase.getKieli())) {
                    Element tr = docBase.getDocument().createElement("tr");
                    tr.setAttribute("bgcolor", "#EEEEEE");
                    tbody.appendChild(tr);

                    Element th = docBase.getDocument().createElement("th");
                    th.setAttribute("colspan", "4");
                    th.appendChild(newBoldElement(docBase.getDocument(), getTextString(docBase, otsikko)));
                    tr.appendChild(th);
                }

                if (selite != null && selite.getTeksti().containsKey(docBase.getKieli())) {
                    Element tr2 = docBase.getDocument().createElement("tr");
                    tbody.appendChild(tr2);

                    Element p = docBase.getDocument().createElement("p");
                    p.appendChild(docBase.getDocument().createTextNode(getTextString(docBase, selite)));

                    Element td = docBase.getDocument().createElement("td");
                    td.setAttribute("colspan", "4");
                    td.appendChild(p);
                    tr2.appendChild(td);
                }

                Set<OsaamistasonKriteeri> osaamistasonKriteerit = kohde.getOsaamistasonKriteerit();
                List<OsaamistasonKriteeri> kriteerilista = new ArrayList<>(osaamistasonKriteerit);

                kriteerilista.stream()
                        .sorted(Comparator.comparing(k -> k.getOsaamistaso().getId()))
                        .forEach(kriteeri -> {
                            String ktaso = getTextString(docBase, kriteeri.getOsaamistaso().getOtsikko());

                            Element kriteeriRivi = docBase.getDocument().createElement("tr");
                            tbody.appendChild(kriteeriRivi);

                            Element kriteeriTaso = docBase.getDocument().createElement("td");
                            kriteeriTaso.setAttribute("colspan", "1");
                            kriteeriTaso.setTextContent(ktaso);
                            kriteeriRivi.appendChild(kriteeriTaso);

                            Element kriteeriKriteerit = docBase.getDocument().createElement("td");
                            kriteeriKriteerit.setAttribute("colspan", "3");
                            kriteeriRivi.appendChild(kriteeriKriteerit);

                            Element kriteeriLista = docBase.getDocument().createElement("ul");
                            kriteeriKriteerit.appendChild(kriteeriLista);

                            kriteeri.getKriteerit().forEach(kriteeriKriteeri -> {
                                String kriteeriKriteeriText = getTextString(docBase, kriteeriKriteeri);
                                if (StringUtils.isNotEmpty(kriteeriKriteeriText)) {
                                    addTeksti(docBase, kriteeriKriteeriText, "li", kriteeriLista);
                                }
                            });
                        });
            }
        }
    }

    private void addTavoitteet(DokumenttiPeruste docBase, TutkinnonOsa osa) {
        String TavoitteetText = getTextString(docBase, osa.getTavoitteet());
        if (StringUtils.isEmpty(TavoitteetText)) {
            return;
        }

        addTeksti(docBase, messages.translate("docgen.tavoitteet.title", docBase.getKieli()), "h5");
        addTeksti(docBase, TavoitteetText, "div");
    }

    private void addTutke2Osat(DokumenttiPeruste docBase, TutkinnonOsa osa) {
        List<OsaAlue> osaAlueet = osa.getOsaAlueet();

        osaAlueet.stream()
                .forEach(osaAlue -> {

                    String nimi = getTextString(docBase, osaAlue.getNimi());
                    addTeksti(docBase, nimi, "h5");

                    List<Osaamistavoite> osaamistavoitteet = osaAlue.getOsaamistavoitteet();
                    ValmaTelmaSisalto valmatelma = osaAlue.getValmaTelmaSisalto();

                    addValmatelmaSisalto(docBase, valmatelma);

                    // Parita pakollinen ja valinnainen osaamistavoite
                    Map<Long, Pair<Osaamistavoite, Osaamistavoite>> tavoiteParit = new LinkedHashMap<>();

                    if (osaamistavoitteet != null) {
                        for (Osaamistavoite tavoite : osaamistavoitteet) {
                            Long key = tavoite.isPakollinen()
                                    ? tavoite.getId()
                                    : (tavoite.getEsitieto() != null ? tavoite.getEsitieto().getId() : tavoite.getId());

                            if (tavoiteParit.containsKey(key)) {
                                Pair<Osaamistavoite, Osaamistavoite> pari = tavoiteParit.get(key);
                                pari = tavoite.isPakollinen()
                                        ? Pair.of(tavoite, pari.getSecond()) : Pair.of(pari.getFirst(), tavoite);
                                tavoiteParit.put(key, pari);

                            } else {
                                Pair<Osaamistavoite, Osaamistavoite> pari = tavoite.isPakollinen()
                                        ? Pair.of(tavoite, (Osaamistavoite) null) : Pair
                                        .of((Osaamistavoite) null, tavoite);
                                tavoiteParit.put(key, pari);
                            }
                        }
                    }

                    for (Pair<Osaamistavoite, Osaamistavoite> tavoitePari : tavoiteParit.values()) {
                        Osaamistavoite pakollinen = tavoitePari.getFirst();
                        Osaamistavoite valinnainen = tavoitePari.getSecond();

                        Osaamistavoite otsikkoTavoite = pakollinen != null ? pakollinen : valinnainen;
                        if (otsikkoTavoite == null) {
                            continue;
                        }

                        if (osa.getTyyppi().equals(TutkinnonOsaTyyppi.TUTKE2)) {
                            String tavoitteenNimi = getTextString(docBase, otsikkoTavoite.getNimi());
                            addTeksti(docBase, tavoitteenNimi, "h6");
                        }

                        Osaamistavoite[] tavoiteLista = new Osaamistavoite[]{pakollinen, valinnainen};
                        for (Osaamistavoite tavoite : tavoiteLista) {
                            if (tavoite == null) {
                                continue;
                            }

                            String otsikkoAvain = tavoite.isPakollinen() ? "docgen.tutke2.pakolliset_osaamistavoitteet.title"
                                    : "docgen.tutke2.valinnaiset_osaamistavoitteet.title";
                            String otsikko = messages.translate(otsikkoAvain, docBase.getKieli())
                                    + getLaajuusSuffiksi(tavoite.getLaajuus(), docBase.getLaajuusYksikko(), docBase.getKieli());
                            String tavoitteet = getTextString(docBase, tavoite.getTavoitteet());
                            Arviointi arviointi = tavoite.getArviointi();
                            TekstiPalanen tunnustaminen = tavoite.getTunnustaminen();
                            List<AmmattitaitovaatimuksenKohdealue> ammattitaitovaatimukset
                                    = tavoite.getAmmattitaitovaatimuksetLista();

                            if (StringUtils.isNotEmpty(tavoitteet) || tunnustaminen != null) {
                                addTeksti(docBase, otsikko, "h5");
                            } else {
                                continue;
                            }

                            if (StringUtils.isNotEmpty(tavoitteet)) {
                                addTeksti(docBase, tavoitteet, "div");
                            }

                            addArviointi(docBase, arviointi, osa.getTyyppi());

                            if (tunnustaminen != null) {
                                addTeksti(docBase,
                                        messages.translate("docgen.tutke2.tunnustaminen.title", docBase.getKieli()), "h6");
                                addTeksti(docBase, getTextString(docBase, tunnustaminen), "div");
                            }

                            if (!ammattitaitovaatimukset.isEmpty()) {
                                addAmmattitaitovaatimukset(docBase, null, ammattitaitovaatimukset, null);
                            }

                        }
                    }

                    if (osaAlue.getPakollisetOsaamistavoitteet() != null && osaamistavoitteellaSisaltoa(osaAlue.getPakollisetOsaamistavoitteet().getTavoitteet2020(), docBase.getKieli())) {
                        String otsikko = messages.translate("docgen.tutke2.pakolliset_osaamistavoitteet.title", docBase.getKieli())
                                + getLaajuusSuffiksi(osaAlue.getPakollisetOsaamistavoitteet().getLaajuus(), docBase.getLaajuusYksikko(), docBase.getKieli());
                        addTeksti(docBase, otsikko, "h5");

                        addAmmattitaitovaatimukset2019(docBase, osaAlue.getPakollisetOsaamistavoitteet().getTavoitteet2020());
                    }

                    if (osaAlue.getValinnaisetOsaamistavoitteet() != null && osaamistavoitteellaSisaltoa(osaAlue.getValinnaisetOsaamistavoitteet().getTavoitteet2020(), docBase.getKieli())) {
                        String otsikko = messages.translate("docgen.tutke2.valinnaiset_osaamistavoitteet.title", docBase.getKieli())
                                + getLaajuusSuffiksi(osaAlue.getValinnaisetOsaamistavoitteet().getLaajuus(), docBase.getLaajuusYksikko(), docBase.getKieli());
                        addTeksti(docBase, otsikko, "h5");

                        addAmmattitaitovaatimukset2019(docBase, osaAlue.getValinnaisetOsaamistavoitteet().getTavoitteet2020());
                    }

                    addGeneerinenArviointi(docBase, osaAlue.getGeneerinenArviointiasteikko());

                });
    }

    private boolean osaamistavoitteellaSisaltoa(Ammattitaitovaatimukset2019 tavoitteet, Kieli kieli) {
        return tavoitteet.getVaatimukset().stream()
                    .anyMatch(vaatimus -> vaatimus.getVaatimus().getTeksti().containsKey(kieli) && StringUtils.isNotEmpty(vaatimus.getVaatimus().getTeksti().get(kieli)))
                || tavoitteet.getKohdealueet().stream()
                    .anyMatch(kohdeAlue -> kohdeAlue.getVaatimukset().stream()
                            .anyMatch(vaatimus -> vaatimus.getVaatimus() != null && vaatimus.getVaatimus().getTeksti().containsKey(kieli) && StringUtils.isNotEmpty(vaatimus.getVaatimus().getTeksti().get(kieli))));
    }

    private void addAipeSisalto(DokumenttiPeruste docBase) {
        AIPEOpetuksenSisalto aipeSisalto = docBase.getAipeOpetuksenSisalto();
        if (aipeSisalto != null) {
            addVaiheet(docBase, aipeSisalto);
        }
    }

    private void addVaiheet(DokumenttiPeruste docBase, AIPEOpetuksenSisalto aipeSisalto) {
        aipeSisalto.getVaiheet().forEach(aipeVaihe -> addVaihe(docBase, aipeVaihe));
    }

    private void addVaihe(DokumenttiPeruste docBase, AIPEVaihe vaihe) {
        addHeader(docBase, getTextString(docBase, vaihe.getNimi()));

        docBase.getGenerator().increaseDepth();

        addTekstiOsa(docBase, vaihe.getSiirtymaEdellisesta());
        addTekstiOsa(docBase, vaihe.getTehtava());
        addTekstiOsa(docBase, vaihe.getSiirtymaSeuraavaan());
        addTekstiOsa(docBase, vaihe.getPaikallisestiPaatettavatAsiat());

        if (vaihe.getOppiaineet().size() > 0) {
            addOppiaineet(docBase, vaihe.getOppiaineet());
        }

        docBase.getGenerator().decreaseDepth();
        docBase.getGenerator().increaseNumber();
    }

    private void addTekstiOsa(DokumenttiPeruste docBase, TekstiOsa tekstiOsa) {
        if (tekstiOsa != null) {
            addTeksti(docBase, getTextString(docBase, tekstiOsa.getOtsikko()), "h5");
            addTeksti(docBase, getTextString(docBase, tekstiOsa.getTeksti()), "div");
        }
    }

    private void addOppiaineet(DokumenttiPeruste docBase, List<AIPEOppiaine> oppiaineet) {
        oppiaineet.forEach(aipeOppiaine -> addOppiaine(docBase, aipeOppiaine));
    }

    private void addOppiaine(DokumenttiPeruste docBase, AIPEOppiaine oppiaine) {
        StringBuilder nimiBuilder = new StringBuilder();
        nimiBuilder.append(getTextString(docBase, oppiaine.getNimi()));
        if (oppiaine.getKoodi() != null && oppiaine.getKoodi().getUri() != null) {
            String uri = oppiaine.getKoodi().getUri();
            String[] splitArray = uri.split("_");
            if (splitArray.length > 0) {
                nimiBuilder.append(" (");
                nimiBuilder.append(splitArray[splitArray.length - 1].toUpperCase());
                nimiBuilder.append(")");
            }
        }

        if (oppiaine.getNimi() != null) {
            addHeader(docBase, nimiBuilder.toString());
        } else if (oppiaine.getOppiaine() == null) {
            addHeader(docBase, messages.translate("docgen.nimeton_oppiaine", docBase.getKieli()));
        } else {
            addHeader(docBase, messages.translate("docgen.nimeton_oppimaara", docBase.getKieli()));
        }

        docBase.getGenerator().increaseDepth();

        addTekstiOsa(docBase, oppiaine.getTyotavat());
        addTekstiOsa(docBase, oppiaine.getOhjaus());
        addTekstiOsa(docBase, oppiaine.getArviointi());
        addTekstiOsa(docBase, oppiaine.getSisaltoalueinfo());

        if (oppiaine.getPakollinenKurssiKuvaus() != null) {
            addTeksti(docBase, messages.translate("docgen.pakollinen_kurssi_kuvaus.title", docBase.getKieli()), "h5");
            addTeksti(docBase, getTextString(docBase, oppiaine.getPakollinenKurssiKuvaus()), "div");
        }

        if (oppiaine.getSyventavaKurssiKuvaus() != null) {
            addTeksti(docBase, messages.translate("docgen.syventava_kurssi_kuvaus.title", docBase.getKieli()), "h5");
            addTeksti(docBase, getTextString(docBase, oppiaine.getSyventavaKurssiKuvaus()), "div");
        }

        if (oppiaine.getSoveltavaKurssiKuvaus() != null) {
            addTeksti(docBase, messages.translate("docgen.soveltava_kurssi_kuvaus.title", docBase.getKieli()), "h5");
            addTeksti(docBase, getTextString(docBase, oppiaine.getSoveltavaKurssiKuvaus()), "div");
        }

        // Tavoitteet
        if (oppiaine.getTavoitteet().size() > 0) {
            addTeksti(docBase, messages.translate("docgen.tavoitteet.title", docBase.getKieli()), "h5");
            addOppiaineTavoitteet(docBase, oppiaine.getTavoitteet());
        }

        // Kurssit
        if (oppiaine.getKurssit().size() > 0) {
            addTeksti(docBase, messages.translate("docgen.kurssit.title", docBase.getKieli()), "h5");
            addKurssit(docBase, oppiaine.getKurssit());
        }

        // Oppimäärät
        if (oppiaine.getOppimaarat().size() > 0) {
            addOppiaineet(docBase, oppiaine.getOppimaarat());
        }

        docBase.getGenerator().decreaseDepth();
        docBase.getGenerator().increaseNumber();
    }

    private void addOppiaineTavoitteet(DokumenttiPeruste docBase, List<OpetuksenTavoite> tavoiteet) {
        tavoiteet.forEach(opetuksenTavoite -> {
            Element table = docBase.getDocument().createElement("table");
            docBase.getBodyElement().appendChild(table);
            table.setAttribute("border", "1");

            // Tavoitteen otsikko
            {
                Element tr = docBase.getDocument().createElement("tr");
                table.appendChild(tr);

                Element th = docBase.getDocument().createElement("th");
                tr.appendChild(th);
                tr.setAttribute("bgcolor", "#AAAAAA");
                th.setTextContent(getTextString(docBase, opetuksenTavoite.getTavoite()));
            }

            {
                Element tr = docBase.getDocument().createElement("tr");
                table.appendChild(tr);

                Element td = docBase.getDocument().createElement("td");
                tr.appendChild(td);

                // Tavoitteista johdetut oppimisen tavoitteet
                if (opetuksenTavoite.getTavoitteistaJohdetutOppimisenTavoitteet() != null) {
                    addTeksti(docBase, messages.translate("docgen.tavoitteista-johdetut-oppimisen-tavoitteet.title", docBase.getKieli()), "h6", td);
                    addTeksti(docBase, getTextString(docBase, opetuksenTavoite.getTavoitteistaJohdetutOppimisenTavoitteet()), "div", td);
                }

                // Tavoitealueet
                addTeksti(docBase, messages.translate("docgen.tavoitealueet.title", docBase.getKieli()), "h6", td);
                opetuksenTavoite.getKohdealueet().forEach(opetuksenKohdealue -> addTeksti(docBase,
                        getTextString(docBase, opetuksenKohdealue.getNimi()), "div", td));

                // Laaja-alainen osaaminen
                addTeksti(docBase, messages.translate("docgen.laaja_alainen_osaaminen.title", docBase.getKieli()), "h6", td);
                StringJoiner joiner = new StringJoiner(", ");
                opetuksenTavoite.getLaajattavoitteet().forEach(laajaalainenOsaaminen -> joiner
                        .add(getTextString(docBase, laajaalainenOsaaminen.getNimi())));
                addTeksti(docBase, joiner.toString(), "div", td);

                // Arviointi
                addTeksti(docBase, getTextString(docBase, opetuksenTavoite.getArvioinninOtsikko()), "h6", td);

                Element arviointiTable = docBase.getDocument().createElement("table");
                td.appendChild(arviointiTable);
                arviointiTable.setAttribute("border", "1");

                // Arviointi otsikkorivi
                Element arviointiTr = docBase.getDocument().createElement("tr");
                arviointiTable.appendChild(arviointiTr);
                arviointiTr.setAttribute("bgcolor", "#EEEEEE");

                Element arvioinninKuvausTh = docBase.getDocument().createElement("th");
                arviointiTr.appendChild(arvioinninKuvausTh);
                arvioinninKuvausTh.setTextContent(getTextString(docBase, opetuksenTavoite.getArvioinninKuvaus()));

                Element arvioinninOsaamisenKuvausTh = docBase.getDocument().createElement("th");
                arviointiTr.appendChild(arvioinninOsaamisenKuvausTh);
                arvioinninOsaamisenKuvausTh.setTextContent(getTextString(docBase,
                        opetuksenTavoite.getArvioinninOsaamisenKuvaus()));

                // Arvioinnin tavoitteet
                opetuksenTavoite.getArvioinninkohteet().forEach(tavoitteenArviointi -> {
                    Element kohdeTr = docBase.getDocument().createElement("tr");
                    arviointiTable.appendChild(kohdeTr);
                    Element kohdeTd = docBase.getDocument().createElement("td");
                    kohdeTr.appendChild(kohdeTd);
                    kohdeTd.setTextContent(getTextString(docBase, tavoitteenArviointi.getArvioinninKohde()));

                    lisaaOsaamisenKuvaukset(docBase, kohdeTr, tavoitteenArviointi.getOsaamisenKuvaus());
                    lisaaOsaamisenKuvaukset(docBase, kohdeTr, tavoitteenArviointi.getHyvanOsaamisenKuvaus());
                });
            }
        });
    }

    private void lisaaOsaamisenKuvaukset(DokumenttiPeruste docBase, Element kohdeTr, TekstiPalanen osaamisenKuvaus) {
        if (osaamisenKuvaus != null) {
            Element td = docBase.getDocument().createElement("td");
            kohdeTr.appendChild(td);
            td.setTextContent(getTextString(docBase, osaamisenKuvaus));
        }
    }

    private void addKurssit(DokumenttiPeruste docBase, List<AIPEKurssi> kurssit) {
        kurssit.forEach(aipeKurssi -> addKurssi(docBase, aipeKurssi));
    }

    private void addKurssi(DokumenttiPeruste docBase, AIPEKurssi kurssi) {
        StringBuilder nimiBuilder = new StringBuilder();
        nimiBuilder.append(getTextString(docBase, kurssi.getNimi()));
        if (kurssi.getKoodi() != null && kurssi.getKoodi().getUri() != null) {
            String uri = kurssi.getKoodi().getUri();
            String[] splitArray = uri.split("_");
            if (splitArray.length > 0) {
                nimiBuilder.append(" (");
                nimiBuilder.append(splitArray[splitArray.length - 1].toUpperCase());
                nimiBuilder.append(")");
            }
        }

        addTeksti(docBase, nimiBuilder.toString(), "h6");
        addTeksti(docBase, getTextString(docBase, kurssi.getKuvaus()), "div");

        // Liitetyt tavoitteet
        if (kurssi.getTavoitteet().size() > 0) {
            addTeksti(docBase, messages.translate("docgen.liitetyt_tavoitteet", docBase.getKieli()) + ":", "p");
            Element ul = docBase.getDocument().createElement("ul");
            docBase.getBodyElement().appendChild(ul);

            kurssi.getTavoitteet().forEach(opetuksenTavoite -> {
                Element li = docBase.getDocument().createElement("li");
                ul.appendChild(li);
                li.setTextContent(getTextString(docBase, opetuksenTavoite.getTavoite()));
            });
        }
    }

    @Deprecated
    private void addKasitteet(DokumenttiPeruste docBase) {
        List<Termi> termit = termistoRepository.findByPerusteId(docBase.getPeruste().getId());
        if (termit.size() == 0) {
            return;
        }

        addHeader(docBase, messages.translate("docgen.termit.kasitteet-otsikko", docBase.getKieli()));

        termit.forEach(termi -> {
            String termiTermi = getTextString(docBase, termi.getTermi());
            String termiSelitys = getTextString(docBase, termi.getSelitys());

            addTeksti(docBase, "<strong>" + termiTermi + "</strong>", "p");
            addTeksti(docBase, termiSelitys, "div");
        });

        docBase.getGenerator().increaseNumber();
    }

    private <T> List<T> sanitizeList(List<T> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    private String getLaajuusSuffiksi(final BigDecimal laajuus, final LaajuusYksikko yksikko, final Kieli kieli) {
        StringBuilder laajuusBuilder = new StringBuilder("");
        if (laajuus != null) {
            laajuusBuilder.append(", ");
            laajuusBuilder.append(laajuus.stripTrailingZeros().toPlainString());
            laajuusBuilder.append(" ");
            String yksikkoAvain;
            switch (yksikko) {
                case OPINTOVIIKKO:
                    yksikkoAvain = "docgen.laajuus.ov";
                    break;
                case OSAAMISPISTE:
                    yksikkoAvain = "docgen.laajuus.osp";
                    break;
                default:
                    throw new NotImplementedException("Tuntematon laajuusyksikko: " + yksikko);
            }
            laajuusBuilder.append(messages.translate(yksikkoAvain, kieli));
        }
        return laajuusBuilder.toString();
    }

    private String getLaajuusSuffiksi(final BigDecimal laajuus, final BigDecimal laajuusMaksimi, final LaajuusYksikko yksikko, final Kieli kieli) {
        StringBuilder laajuusBuilder = new StringBuilder("");
        if (laajuus != null) {
            laajuusBuilder.append(", ");
            laajuusBuilder.append(laajuus.stripTrailingZeros().toPlainString());
            laajuusBuilder.append("-");
            laajuusBuilder.append(laajuusMaksimi.stripTrailingZeros().toPlainString());
            laajuusBuilder.append(" ");
            String yksikkoAvain;
            switch (yksikko) {
                case OPINTOVIIKKO:
                    yksikkoAvain = "docgen.laajuus.ov";
                    break;
                case OSAAMISPISTE:
                    yksikkoAvain = "docgen.laajuus.osp";
                    break;
                default:
                    throw new NotImplementedException("Tuntematon laajuusyksikko: " + yksikko);
            }
            laajuusBuilder.append(messages.translate(yksikkoAvain, kieli));
        }
        return laajuusBuilder.toString();
    }

    private String getOtsikko(DokumenttiPeruste docBase, TutkinnonOsaViite viite) {
        return getOtsikko(docBase, viite, true);
    }

    private String getOtsikko(DokumenttiPeruste docBase, TutkinnonOsaViite viite, boolean withKoodi) {
        TutkinnonOsa osa = viite.getTutkinnonOsa();
        StringBuilder otsikkoBuilder = new StringBuilder();
        otsikkoBuilder.append(getTextString(docBase, mapper.map(osa, TutkinnonOsaDto.class).getNimi()));

        BigDecimal laajuusMaksimi = viite.getLaajuusMaksimi();
        if (laajuusMaksimi != null) {
            otsikkoBuilder.append(getLaajuusSuffiksi(viite.getLaajuus(), laajuusMaksimi,
                    docBase.getLaajuusYksikko(), docBase.getKieli()));
        } else {
            otsikkoBuilder.append(getLaajuusSuffiksi(viite.getLaajuus(), docBase.getLaajuusYksikko(), docBase.getKieli()));
        }

        if (withKoodi) {
            TutkinnonOsaDto osaDto = mapper.map(osa, TutkinnonOsaDto.class);
            String koodi = osaDto.getKoodiArvo();
            if (koodi != null) {
                otsikkoBuilder
                        .append(" (")
                        .append(koodi)
                        .append(")");
            }
        }

        return otsikkoBuilder.toString();
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

    private void buildImages(DokumenttiPeruste docBase) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression expression = xpath.compile("//img");
            NodeList list = (NodeList) expression.evaluate(docBase.getDocument(), XPathConstants.NODESET);

            for (int i = 0; i < list.getLength(); i++) {
                Element element = (Element) list.item(i);
                String id = element.getAttribute("data-uid");

                if (StringUtils.isEmpty(id)) {
                    continue;
                }

                UUID uuid = UUID.fromString(id);

                // Ladataan kuvan data muistiin
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                liiteService.export(docBase.getPeruste().getId(), uuid, byteArrayOutputStream);

                // Tehdään muistissa olevasta datasta kuva
                InputStream in = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                BufferedImage bufferedImage = ImageIO.read(in);

                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();

                // Muutetaan kaikkien kuvien väriavaruus RGB:ksi jotta PDF/A validointi menee läpi
                // Asetetaan lisäksi läpinäkyvien kuvien taustaksi valkoinen väri
                BufferedImage tempImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
                        BufferedImage.TYPE_3BYTE_BGR);
                tempImage.getGraphics().setColor(new Color(255, 255, 255, 0));
                tempImage.getGraphics().fillRect(0, 0, width, height);
                tempImage.getGraphics().drawImage(bufferedImage, 0, 0, null);
                bufferedImage = tempImage;

                ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
                ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
                jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                jpgWriteParam.setCompressionQuality(COMPRESSION_LEVEL);

                // Muunnetaan kuva base64 enkoodatuksi
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                MemoryCacheImageOutputStream imageStream = new MemoryCacheImageOutputStream(out);
                jpgWriter.setOutput(imageStream);
                IIOImage outputImage = new IIOImage(bufferedImage, null, null);
                jpgWriter.write(null, outputImage, jpgWriteParam);
                jpgWriter.dispose();
                String base64 = Base64.getEncoder().encodeToString(out.toByteArray());

                // Lisätään bas64 kuva img elementtiin
                element.setAttribute("width", String.valueOf(width));
                element.setAttribute("height", String.valueOf(height));
                element.setAttribute("src", "data:image/jpg;base64," + base64);
            }

        } catch (XPathExpressionException | IOException | NullPointerException e) {
            log.error(e.getLocalizedMessage());
        }
    }
}
