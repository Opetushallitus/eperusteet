package fi.vm.sade.eperusteet.service.dokumentti.impl.util;

import fi.vm.sade.eperusteet.domain.Dokumentti;
import fi.vm.sade.eperusteet.domain.LokalisoituTeksti;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import org.apache.commons.lang.time.DateUtils;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.apache.pdfbox.preflight.utils.ByteArrayDataSource;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * @author isaul
 */
public class DokumenttiUtils {
    private static final int MAX_TIME_IN_MINUTES = 5;

    public static void addLokalisoituteksti(DokumenttiBase docBase, LokalisoituTekstiDto lTekstiDto, String tagi) {
        if (lTekstiDto != null) {
            addLokalisoituteksti(docBase, docBase.getMapper().map(lTekstiDto, LokalisoituTeksti.class), tagi);
        }
    }

    public static void addLokalisoituteksti(DokumenttiBase docBase, LokalisoituTeksti lTeksti, String tagi) {
        if (lTeksti != null && lTeksti.getTeksti() != null && lTeksti.getTeksti() != null) {
            String teksti = lTeksti.getTeksti();
            teksti = "<" + tagi + ">" + unescapeHtml5(teksti) + "</" + tagi + ">";

            Document tempDoc = new W3CDom().fromJsoup(Jsoup.parseBodyFragment(teksti));
            Node node = tempDoc.getDocumentElement().getChildNodes().item(1).getFirstChild();

            docBase.getBodyElement().appendChild(docBase.getDocument().importNode(node, true));
        }
    }

    public static void addTeksti(DokumenttiBase docBase, String teksti, String tagi) {
        addTeksti(docBase, teksti, tagi, docBase.getBodyElement());
    }

    public static void addTeksti(DokumenttiBase docBase, String teksti, String tagi, Element element) {
        if (teksti != null) {

            teksti = unescapeHtml5(teksti);
            teksti = "<" + tagi + ">" + teksti + "</" + tagi + ">";

            Document tempDoc = new W3CDom().fromJsoup(Jsoup.parseBodyFragment(teksti));
            Node node = tempDoc.getDocumentElement().getChildNodes().item(1).getFirstChild();

            element.appendChild(docBase.getDocument().importNode(node, true));
        }
    }

    public static void addHeader(DokumenttiBase docBase, String text) {
        if (text != null) {
            Element header = docBase.getDocument().createElement("h" + docBase.getGenerator().getDepth());
            header.setAttribute("number", docBase.getGenerator().generateNumber());
            header.appendChild(docBase.getDocument().createTextNode(unescapeHtml5(text)));
            docBase.getBodyElement().appendChild(header);
        }
    }

    public static String getTextString(DokumenttiBase docBase, TekstiPalanen tekstiPalanen) {
        if (tekstiPalanen != null
                && tekstiPalanen.getTeksti() != null
                && tekstiPalanen.getTeksti().containsKey(docBase.getKieli())
                && tekstiPalanen.getTeksti().get(docBase.getKieli()) != null) {
            return unescapeHtml5(tekstiPalanen.getTeksti().get(docBase.getKieli()));
        } else {
            return "";
        }
    }

    public static String getTextString(DokumenttiBase docBase, LokalisoituTekstiDto lokalisoituTekstiDto) {
        LokalisoituTeksti lokalisoituTeksti = docBase.getMapper().map(lokalisoituTekstiDto, LokalisoituTeksti.class);
        return getTextString(docBase, lokalisoituTeksti);
    }

    public static String getTextString(DokumenttiBase docBase, LokalisoituTeksti lokalisoituTeksti) {
        if (lokalisoituTeksti == null || lokalisoituTeksti.getTeksti() == null
                || lokalisoituTeksti.getTeksti() == null) {
            return "";
        } else {
            return unescapeHtml5(lokalisoituTeksti.getTeksti());
        }
    }

    public static String unescapeHtml5(String string) {
        return Jsoup.clean(stripNonValidXMLCharacters(string), ValidHtml.WhitelistType.NORMAL.getWhitelist());
    }

    public static String stripNonValidXMLCharacters(String in) {
        StringBuilder out = new StringBuilder();
        char current;

        if (in == null || ("".equals(in))) return "";
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i);
            if (current == 0x9
                    || current == 0xA
                    || current == 0xD
                    || current >= 0x20 && current <= 0xD7FF
                    || current >= 0xE000 && current <= 0xFFFD) {
                out.append(current);
            }
        }

        return out.toString();
    }

    public static boolean isTimePass(Dokumentti dokumentti) {
        return isTimePass(dokumentti.getAloitusaika());
    }

    public static boolean isTimePass(DokumenttiDto dokumenttiDto) {
        return isTimePass(dokumenttiDto.getAloitusaika());
    }

    public static boolean isTimePass(Date date) {
        if (date == null) {
            return true;
        }

        Date newDate = DateUtils.addMinutes(date, MAX_TIME_IN_MINUTES);
        return newDate.before(new Date());
    }

    public static ValidationResult validatePdf(byte[] pdf) throws IOException {
        ValidationResult result;
        InputStream is = new ByteArrayInputStream(pdf);
        PreflightParser parser = new PreflightParser(new ByteArrayDataSource(is));

        try {
            parser.parse();

            PreflightDocument document = parser.getPreflightDocument();
            document.validate();

            // Get validation result
            result = document.getResult();
            document.close();

        } catch (SyntaxValidationException e) {
            result = e.getResult();
        }

        return result;
    }
}
