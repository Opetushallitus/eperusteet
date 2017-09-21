package fi.vm.sade.eperusteet.service.dokumentti.impl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.ammattitaitovaatimukset.AmmattitaitovaatimuksenKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohde;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.*;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.*;
import fi.vm.sade.eperusteet.domain.yl.*;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.repository.TermistoRepository;
import fi.vm.sade.eperusteet.repository.TutkintonimikeKoodiRepository;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.LiiteService;
import fi.vm.sade.eperusteet.service.LocalizedMessagesService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiNewBuilderService;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.CharapterNumberGenerator;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.DokumenttiPeruste;
import static fi.vm.sade.eperusteet.service.dokumentti.impl.util.DokumenttiUtils.*;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.Pair;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
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
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author isaul
 */
@Service
public class DokumenttiNewBuilderServiceImpl implements DokumenttiNewBuilderService {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiNewBuilderServiceImpl.class);
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

        DokumenttiPeruste docBase = new DokumenttiPeruste();
        docBase.setDocument(doc);
        docBase.setHeadElement(headElement);
        docBase.setBodyElement(bodyElement);
        docBase.setGenerator(new CharapterNumberGenerator());
        docBase.setKieli(kieli);
        docBase.setPeruste(peruste);
        docBase.setDokumentti(dokumentti);
        docBase.setMapper(mapper);

        if (suoritustapakoodi.equals(Suoritustapakoodi.AIPE)) {
            AIPEOpetuksenSisalto aipeOpetuksenPerusteenSisalto = peruste.getAipeOpetuksenPerusteenSisalto();
            docBase.setAipeOpetuksenSisalto(aipeOpetuksenPerusteenSisalto);
            docBase.setSisalto(aipeOpetuksenPerusteenSisalto.getSisalto());
        }
        else if (peruste.getTyyppi() == PerusteTyyppi.OPAS) {
            PerusteenOsaViite sisalto = peruste.getSisalto(null);
            docBase.setSisalto(sisalto);
        }
        else {
            Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
            PerusteenOsaViite sisalto = suoritustapa.getSisalto();
            docBase.setSisalto(sisalto);
        }

        // Kansilehti & Infosivu
        addMetaPages(docBase);

        if (suoritustapakoodi.equals(Suoritustapakoodi.AIPE)) {
            // AIPE-osat
            addAipeSisalto(docBase);
        }
        else if (peruste.getTyyppi() != PerusteTyyppi.OPAS) {
            // Tutkinnon muodostuminen
            addSisaltoelementit(docBase);

            // Tutkinnonosat
            addTutkinnonosat(docBase);
        }


        // Tekstikappaleet
        addTekstikappaleet(docBase, docBase.getSisalto());

        // Käsitteet
        addKasitteet(docBase);

        // Kuvat
        buildImages(docBase);

        // Tulostetaan dokumentti konsoliin
        LOG.debug(printDocument(docBase.getDocument()).toString());
        return doc;
    }

    private void addMetaPages(DokumenttiPeruste docBase) {
        // Nimi
        Element title = docBase.getDocument().createElement("title");
        String nimi = getTextString(docBase, docBase.getPeruste().getNimi());

        // Oppaille ei lisätä perusteiden tietoja
        if (docBase.getPeruste().getTyyppi() == PerusteTyyppi.OPAS) {
            return;
        }

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

    private void addSisaltoelementit(DokumenttiPeruste docBase) {
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

    private void addTutkinnonMuodostuminen(DokumenttiPeruste docBase) {
        addHeader(docBase, messages.translate("docgen.tutkinnon_muodostuminen.title", docBase.getKieli()));
        RakenneModuuli rakenne = docBase.getSisalto().getSuoritustapa().getRakenne();

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

            BigDecimal laajuus = rakenneOsa.getTutkinnonOsaViite().getLaajuus();
            BigDecimal laajuusMaksimi = rakenneOsa.getTutkinnonOsaViite().getLaajuusMaksimi();
            LaajuusYksikko laajuusYksikko = rakenneOsa.getTutkinnonOsaViite().getSuoritustapa().getLaajuusYksikko();
            String laajuusStr = "";
            String yks = "";
            if (laajuus != null && laajuus.compareTo(BigDecimal.ZERO) > 0) {
                laajuusStr = laajuus.stripTrailingZeros().toPlainString();
                if (laajuusMaksimi != null && laajuusMaksimi.compareTo(BigDecimal.ZERO) > 0) {
                    laajuusStr += "-" + laajuusMaksimi.stripTrailingZeros().toPlainString();
                }
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
            td.setAttribute("class", "td" + String.valueOf(depth));
            Element p = docBase.getDocument().createElement("p");

            tbody.appendChild(tr);
            tr.appendChild(td);
            td.appendChild(p);
            p.setTextContent(nimiBuilder.toString());
            if (StringUtils.isNotEmpty(kuvaus)) {
                td.appendChild(newItalicElement(docBase.getDocument(), kuvaus));
            }

            if (rakenneOsa.getPakollinen() != null && rakenneOsa.getPakollinen()) {
                String glyph = messages.translate("docgen.rakenneosa.pakollinen.glyph", docBase.getKieli());
                nimiBuilder.append(", ");
                p.appendChild(docBase.getDocument().createTextNode(", "));
                p.appendChild(newBoldElement(docBase.getDocument(), glyph));
            }
        }
    }

    private void addRakenneModuuli(DokumenttiPeruste docBase, RakenneModuuli rakenneModuuli, Element tbody, int depth) {
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
                tbody.appendChild(tr);
                tr.appendChild(td);
                td.appendChild(p);
                p.appendChild(newBoldElement(docBase.getDocument(), nimi));
                if (StringUtils.isNotEmpty(kuvaus)) {
                    td.appendChild(newItalicElement(docBase.getDocument(), kuvaus));
                }

                break;
        }
    }

    private void addTutkinnonosat(DokumenttiPeruste docBase) {
        Set<Suoritustapa> suoritustavat = docBase.getPeruste().getSuoritustavat();
        Set<TutkinnonOsaViite> osat = new TreeSet<>((o1, o2) -> {
            String nimi1 = getTextString(docBase, o1.getTutkinnonOsa().getNimi());
            String nimi2 = getTextString(docBase, o2.getTutkinnonOsa().getNimi());

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

        suoritustavat.stream()
                .filter(suoritustapa -> suoritustapa.getSuoritustapakoodi()
                .equals(docBase.getSisalto().getSuoritustapa().getSuoritustapakoodi()))
                .forEach(suoritustapa -> osat.addAll(suoritustapa.getTutkinnonOsat()));

        addHeader(docBase, messages.translate("docgen.tutkinnon_osat.title", docBase.getKieli()));

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
                //addKoodi(docBase, osa);
                addTavoitteet(docBase, osa);
                addAmmattitaitovaatimukset(docBase, osa.getAmmattitaitovaatimuksetLista(), osa.getAmmattitaitovaatimukset());
                addValmatelmaSisalto(docBase, osa.getValmaTelmaSisalto());
                addArviointi(docBase, osa.getArviointi(), tyyppi);
                addAmmattitaidonOsoittamistavat(docBase, osa);
            } else if (tyyppi == TutkinnonOsaTyyppi.TUTKE2) {
                addTutke2Osat(docBase, osa);
            }

            docBase.getGenerator().increaseNumber();
        });

        docBase.getGenerator().decreaseDepth();
        docBase.getGenerator().increaseNumber();
    }

    private void addTekstikappaleet(DokumenttiPeruste docBase, PerusteenOsaViite parent) {
        for (PerusteenOsaViite lapsi : parent.getLapset()) {
            PerusteenOsa po = lapsi.getPerusteenOsa();
            if (po == null || !(po instanceof TekstiKappale)) {
                continue;
            }
            TekstiKappale tk = (TekstiKappale) po;

            PerusteenOsaTunniste tunniste = po.getTunniste();
            if (tunniste != PerusteenOsaTunniste.NORMAALI && tunniste != PerusteenOsaTunniste.LAAJAALAINENOSAAMINEN) {
                String nimi = getTextString(docBase, tk.getNimi());
                addHeader(docBase, nimi);

                String teksti = getTextString(docBase, tk.getTeksti());
                addTeksti(docBase, teksti, "div");

                docBase.getGenerator().increaseDepth();

                // Rekursiivisesti
                addTekstikappaleet(docBase, lapsi);

                docBase.getGenerator().decreaseDepth();
                docBase.getGenerator().increaseNumber();

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
        }
    }

    private void addAmmattitaitovaatimukset(DokumenttiPeruste docBase,
            List<AmmattitaitovaatimuksenKohdealue> ammattitaitovaatimuksetLista,
            TekstiPalanen ammattitaitovaatimukset) {
        String ammattitaitovaatimuksetText = getTextString(docBase, ammattitaitovaatimukset);
        if (StringUtils.isEmpty(ammattitaitovaatimuksetText) && ammattitaitovaatimuksetLista.isEmpty()) {
            return;
        }

        addTeksti(docBase, messages.translate("docgen.ammattitaitovaatimukset.title", docBase.getKieli()), "h5");
        if (StringUtils.isNotEmpty(ammattitaitovaatimuksetText)) {
            addTeksti(docBase, ammattitaitovaatimuksetText, "div");
        }

        ammattitaitovaatimuksetLista.stream()
                .filter(ka -> ka.getVaatimuksenKohteet() != null && !ka.getVaatimuksenKohteet().isEmpty())
                .forEach(ka -> {
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

    private void addArviointi(DokumenttiPeruste docBase, Arviointi arviointi, TutkinnonOsaTyyppi tyyppi) {
        if (arviointi == null) {
            return;
        }

        addTeksti(docBase, messages.translate("docgen.arviointi.title", docBase.getKieli()), "h5");
        String lisatietoteksti = getTextString(docBase, arviointi.getLisatiedot());
        if (StringUtils.isNotEmpty(lisatietoteksti)) {
            addTeksti(docBase, lisatietoteksti, "div");
        }

        List<ArvioinninKohdealue> arvioinninKohdealueet = sanitizeList(arviointi.getArvioinninKohdealueet());
        for (ArvioinninKohdealue ka : arvioinninKohdealueet) {
            if (ka.getArvioinninKohteet() == null) {
                continue;
            } else if (ka.getArvioinninKohteet().isEmpty()) {
                continue;
            }

            String otsikkoTeksti = tyyppi == TutkinnonOsaTyyppi.NORMAALI
                    ? getTextString(docBase, ka.getOtsikko())
                    : messages.translate("docgen.tutke2.arvioinnin_kohteet.title", docBase.getKieli());

            addTeksti(docBase, otsikkoTeksti.toUpperCase(), "h6");

            List<ArvioinninKohde> arvioinninKohteet = ka.getArvioinninKohteet();

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

    private void addKoodi(DokumenttiPeruste docBase, TutkinnonOsa osa) {
        String koodiArvo = osa.getKoodiArvo();
        if (StringUtils.isEmpty(koodiArvo)) {
            return;
        }

        addTeksti(docBase, messages.translate("docgen.koodi.title", docBase.getKieli()), "h5");
        addTeksti(docBase, koodiArvo, "div");
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

        for (OsaAlue osaAlue : osaAlueet) {
            String nimi = getTextString(docBase, osaAlue.getNimi());
            addTeksti(docBase, nimi, "h5");

            List<Osaamistavoite> osaamistavoitteet = osaAlue.getOsaamistavoitteet();
            ValmaTelmaSisalto valmatelma = osaAlue.getValmaTelmaSisalto();

            addValmatelmaSisalto(docBase, valmatelma);

            // Parita pakollinen ja valinnainen osaamistavoite
            Map<Long, Pair<Osaamistavoite, Osaamistavoite>> tavoiteParit = new LinkedHashMap<>();
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

            for (Pair<Osaamistavoite, Osaamistavoite> tavoitePari : tavoiteParit.values()) {
                Osaamistavoite pakollinen = tavoitePari.getFirst();
                Osaamistavoite valinnainen = tavoitePari.getSecond();

                Osaamistavoite otsikkoTavoite = pakollinen != null ? pakollinen : valinnainen;
                if (otsikkoTavoite == null) {
                    continue;
                }

                String tavoitteenNimi = getTextString(docBase, otsikkoTavoite.getNimi());
                addTeksti(docBase, tavoitteenNimi, "h6");

                Osaamistavoite[] tavoiteLista = new Osaamistavoite[]{pakollinen, valinnainen};
                for (Osaamistavoite tavoite : tavoiteLista) {
                    if (tavoite == null) {
                        continue;
                    }

                    String otsikkoAvain = tavoite.isPakollinen() ? "docgen.tutke2.pakolliset_osaamistavoitteet.title"
                            : "docgen.tutke2.valinnaiset_osaamistavoitteet.title";
                    String otsikko = messages.translate(otsikkoAvain, docBase.getKieli())
                            + getLaajuusSuffiksi(tavoite.getLaajuus(), docBase.getLaajuusYksikko(), docBase.getKieli());
                    addTeksti(docBase, otsikko, "h6");

                    String tavoitteet = getTextString(docBase, tavoite.getTavoitteet());
                    if (StringUtils.isNotEmpty(tavoitteet)) {
                        addTeksti(docBase, tavoitteet, "div");
                    }

                    Arviointi arviointi = tavoite.getArviointi();
                    addArviointi(docBase, arviointi, TutkinnonOsaTyyppi.TUTKE2);

                    TekstiPalanen tunnustaminen = tavoite.getTunnustaminen();
                    if (tunnustaminen != null) {
                        addTeksti(docBase,
                                messages.translate("docgen.tutke2.tunnustaminen.title", docBase.getKieli()), "h6");
                        addTeksti(docBase, getTextString(docBase, tunnustaminen), "div");
                    }

                    List<AmmattitaitovaatimuksenKohdealue> ammattitaitovaatimukset
                            = tavoite.getAmmattitaitovaatimuksetLista();
                    if (!ammattitaitovaatimukset.isEmpty()) {
                        addAmmattitaitovaatimukset(docBase, ammattitaitovaatimukset, null);
                    }

                }
            }
        }
    }

    private void addAipeSisalto(DokumenttiPeruste docBase) {
        AIPEOpetuksenSisalto aipeSisalto = docBase.getAipeOpetuksenSisalto();
        addVaiheet(docBase, aipeSisalto);
    }

    private void addVaiheet(DokumenttiPeruste docBase, AIPEOpetuksenSisalto aipeSisalto) {

        // Vaiheet
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

                // Tavoitealueet
                addTeksti(docBase, messages.translate("docgen.tavoitealueet.title", docBase.getKieli()), "h6", td);
                opetuksenTavoite.getKohdealueet().forEach(opetuksenKohdealue -> addTeksti(docBase,
                        getTextString(docBase, opetuksenKohdealue.getNimi()), "p", td));

                // Laaja-alainen osaaminen
                addTeksti(docBase, messages.translate("docgen.laaja_alainen_osaaminen.title", docBase.getKieli()), "h6", td);
                StringJoiner joiner = new StringJoiner(", ");
                opetuksenTavoite.getLaajattavoitteet().forEach(laajaalainenOsaaminen -> joiner
                        .add(getTextString(docBase, laajaalainenOsaaminen.getNimi())));
                addTeksti(docBase, joiner.toString(), "p", td);

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

                    Element hyvanOsaamisenKuvausTd = docBase.getDocument().createElement("td");
                    kohdeTr.appendChild(hyvanOsaamisenKuvausTd);
                    hyvanOsaamisenKuvausTd.setTextContent(getTextString(docBase,
                            tavoitteenArviointi.getHyvanOsaamisenKuvaus()));
                });
            }
        });
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
        TutkinnonOsa osa = viite.getTutkinnonOsa();
        StringBuilder otsikkoBuilder = new StringBuilder();
        otsikkoBuilder.append(getTextString(docBase, osa.getNimi()));

        BigDecimal laajuusMaksimi = viite.getLaajuusMaksimi();
        if (laajuusMaksimi != null) {
            otsikkoBuilder.append(getLaajuusSuffiksi(viite.getLaajuus(), laajuusMaksimi,
                    docBase.getLaajuusYksikko(), docBase.getKieli()));
        } else {
            otsikkoBuilder.append(getLaajuusSuffiksi(viite.getLaajuus(), docBase.getLaajuusYksikko(), docBase.getKieli()));
        }

        String koodi = osa.getKoodiArvo();
        if (koodi != null) {
            otsikkoBuilder
                    .append(" (")
                    .append(koodi)
                    .append(")");
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
            LOG.error(e.getLocalizedMessage());
        }
    }
}
