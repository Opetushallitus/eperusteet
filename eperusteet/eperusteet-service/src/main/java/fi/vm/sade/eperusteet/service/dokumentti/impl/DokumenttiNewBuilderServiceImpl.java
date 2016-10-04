package fi.vm.sade.eperusteet.service.dokumentti.impl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.ammattitaitovaatimukset.AmmattitaitovaatimuksenKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohde;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.*;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.*;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.repository.TermistoRepository;
import fi.vm.sade.eperusteet.repository.TutkintonimikeKoodiRepository;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.LocalizedMessagesService;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.CharapterNumberGenerator;
import fi.vm.sade.eperusteet.service.dokumentti.impl.util.DokumenttiBase;
import fi.vm.sade.eperusteet.service.internal.DokumenttiNewBuilderService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.Pair;
import org.apache.commons.lang.NotImplementedException;
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
import java.util.*;

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

    @Autowired
    private TermistoRepository termistoRepository;

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

        // Tutkinnon muodostuminen
        addSisaltoelementit(docBase);

        // Tutkinnonosat
        addTutkinnonosat(docBase);

        // Tekstikappaleet
        addTekstikappaleet(docBase, docBase.getSisalto());

        // Käsitteet
        addKasitteet(docBase);

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
        taulukko.setAttribute("border", "1");
        docBase.getBodyElement().appendChild(taulukko);
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
            td.setAttribute("class", "td" + String.valueOf(depth));
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
                break;
        }
    }


    private void addTutkinnonosat(DokumenttiBase docBase) {
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
                addTavoitteet(docBase, osa);
                addAmmattitaitovaatimukset(docBase, osa.getAmmattitaitovaatimuksetLista(), osa.getAmmattitaitovaatimukset());
                addValmatelmaSisalto(docBase, osa.getValmaTelmaSisalto());
                addAmmattitaidonOsoittamistavat(docBase, osa);
                addArviointi(docBase, osa.getArviointi(), tyyppi);

            } else if (tyyppi == TutkinnonOsaTyyppi.TUTKE2) {
                addTutke2Osat(docBase, osa);
            }

            docBase.getGenerator().increaseNumber();
        });

        docBase.getGenerator().decreaseDepth();
        docBase.getGenerator().increaseNumber();
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

    private void addAmmattitaitovaatimukset(DokumenttiBase docBase,
                                            List<AmmattitaitovaatimuksenKohdealue> ammattitaitovaatimuksetLista,
                                            TekstiPalanen ammattitaitovaatimukset) {
        String ammattitaitovaatimuksetText = getTextString(docBase, ammattitaitovaatimukset);
        if(StringUtils.isEmpty(ammattitaitovaatimuksetText) && ammattitaitovaatimuksetLista.isEmpty()) {
            return;
        }

        addTeksti(docBase, messages.translate("docgen.ammattitaitovaatimukset.title", docBase.getKieli()), "h5");
        if(StringUtils.isNotEmpty(ammattitaitovaatimuksetText)) {
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
                            if(vaatimus.getAmmattitaitovaatimusKoodi() != null
                                    && !vaatimus.getAmmattitaitovaatimusKoodi().isEmpty()){
                                ktaso += " (" + vaatimus.getAmmattitaitovaatimusKoodi() + ")";
                            }
                            Element vaatimusAlkio = docBase.getDocument().createElement("li");
                            vaatimusAlkio.setTextContent(ktaso);
                            vaatimusLista.appendChild(vaatimusAlkio);
                        });
                    });
                });
    }

    private void addValmatelmaSisalto(DokumenttiBase docBase, ValmaTelmaSisalto valmaTelmaSisalto) {
        if(valmaTelmaSisalto == null) {
            return;
        }

        addValmaOsaamistavoitteet(docBase, valmaTelmaSisalto);
        addValmaArviointi(docBase, valmaTelmaSisalto);
    }

    private void addValmaOsaamistavoitteet(DokumenttiBase docBase, ValmaTelmaSisalto valmaTelmaSisalto) {
        if(valmaTelmaSisalto.getOsaamistavoite().size() > 0) {
            addTeksti(docBase, messages.translate("docgen.valma.osaamistavoitteet.title", docBase.getKieli()), "h5");
        }

        for (OsaamisenTavoite osaamisenTavoite : valmaTelmaSisalto.getOsaamistavoite()) {
            if(osaamisenTavoite.getNimi() != null) {
                addTeksti(docBase, getTextString(docBase, osaamisenTavoite.getNimi()), "h6");
            }

            if(osaamisenTavoite.getKohde() != null) {
                addTeksti(docBase, getTextString(docBase, osaamisenTavoite.getKohde()), "div");
            }

            Element lista = docBase.getDocument().createElement("ul");
            docBase.getBodyElement().appendChild(lista);
            osaamisenTavoite.getTavoitteet().forEach(tavoite -> {
                Element alkio = docBase.getDocument().createElement("li");
                alkio.setTextContent(getTextString(docBase, tavoite));
                lista.appendChild(alkio);
            });

            if(osaamisenTavoite.getSelite() != null) {
                addTeksti(docBase, getTextString(docBase, osaamisenTavoite.getSelite()), "div");
            }
        }
    }

    private void addValmaArviointi(DokumenttiBase docBase, ValmaTelmaSisalto valmaTelmaSisalto) {
        if(valmaTelmaSisalto.getOsaamisenarviointi() != null || valmaTelmaSisalto.getOsaamisenarviointiTekstina() != null) {
            addTeksti(docBase, messages.translate("docgen.valma.osaamisenarviointi.title", docBase.getKieli()), "h5");

            if(valmaTelmaSisalto.getOsaamisenarviointi() != null) {
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

            if(valmaTelmaSisalto.getOsaamisenarviointiTekstina() != null){
                addTeksti(docBase,
                        valmaTelmaSisalto.getOsaamisenarviointiTekstina().getTeksti().get(docBase.getKieli()),
                        "div");
            }
        }
    }

    private void addAmmattitaidonOsoittamistavat(DokumenttiBase docBase, TutkinnonOsa osa) {
        String ammattitaidonOsoittamistavatText = getTextString(docBase, osa.getAmmattitaidonOsoittamistavat());
        if (StringUtils.isEmpty(ammattitaidonOsoittamistavatText)) {
            return;
        }

        addTeksti(docBase, messages.translate("docgen.ammattitaidon_osoittamistavat.title", docBase.getKieli()), "h5");
        addTeksti(docBase, ammattitaidonOsoittamistavatText, "div");
    }

    private void addArviointi(DokumenttiBase docBase, Arviointi arviointi, TutkinnonOsaTyyppi tyyppi) {
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
                String kohdeTeksti = getTextString(docBase, kohde.getOtsikko());

                Element taulukko = docBase.getDocument().createElement("table");
                taulukko.setAttribute("border", "1");
                docBase.getBodyElement().appendChild(taulukko);
                Element tbody = docBase.getDocument().createElement("tbody");
                taulukko.appendChild(tbody);

                Element tr = docBase.getDocument().createElement("tr");
                tr.setAttribute("bgcolor", "#EEEEEE");
                tbody.appendChild(tr);

                Element th = docBase.getDocument().createElement("th");
                th.setAttribute("colspan", "4");
                th.appendChild(newBoldElement(docBase.getDocument(), kohdeTeksti));
                tr.appendChild(th);

                Set<OsaamistasonKriteeri> osaamistasonKriteerit = kohde.getOsaamistasonKriteerit();
                List<OsaamistasonKriteeri> kriteerilista = new ArrayList<>(osaamistasonKriteerit);
                java.util.Collections.sort(kriteerilista,
                        (o1, o2) -> (int) (o1.getOsaamistaso().getId() - o2.getOsaamistaso().getId()));

                kriteerilista.stream()
                        .sorted((k1, k2) -> k2.getOsaamistaso().getId().compareTo(
                                k1.getOsaamistaso().getId()))
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
                                    Element kriteeriListanAlkio = docBase.getDocument().createElement("li");
                                    kriteeriListanAlkio.setTextContent(kriteeriKriteeriText);
                                    kriteeriLista.appendChild(kriteeriListanAlkio);
                                }
                            });
                        });
            }
        }
    }

    private void addTavoitteet(DokumenttiBase docBase, TutkinnonOsa osa) {
        String TavoitteetText = getTextString(docBase, osa.getTavoitteet());
        if (StringUtils.isEmpty(TavoitteetText)) {
            return;
        }

        addTeksti(docBase, messages.translate("docgen.tavoitteet.title", docBase.getKieli()), "h5");
        addTeksti(docBase, TavoitteetText, "div");
    }

    private void addTutke2Osat(DokumenttiBase docBase, TutkinnonOsa osa) {
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
                    String otsikko = messages.translate(otsikkoAvain, docBase.getKieli()) +
                            getLaajuusSuffiksi(tavoite.getLaajuus(), LaajuusYksikko.OSAAMISPISTE, docBase.getKieli());
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

    private void addKasitteet(DokumenttiBase docBase) {
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

    private String getOtsikko(DokumenttiBase docBase, TutkinnonOsaViite viite) {
        TutkinnonOsa osa = viite.getTutkinnonOsa();
        return getTextString(docBase, osa.getNimi()) +
                getLaajuusSuffiksi(viite.getLaajuus(), LaajuusYksikko.OSAAMISPISTE, docBase.getKieli());
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
