package fi.vm.sade.eperusteet.service.internal;


import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * @author isaul
 */
public interface PdfService {
    byte[] xhtml2pdf(Document document) throws IOException, TransformerException, SAXException;
}
