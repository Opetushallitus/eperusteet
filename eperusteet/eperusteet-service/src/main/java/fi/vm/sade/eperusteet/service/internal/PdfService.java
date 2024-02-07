package fi.vm.sade.eperusteet.service.internal;

import fi.vm.sade.eperusteet.domain.GeneratorVersion;
import fi.vm.sade.eperusteet.utils.dto.dokumentti.DokumenttiMetaDto;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.IOException;

public interface PdfService {
    byte[] xhtml2pdf(Document document, GeneratorVersion version, DokumenttiMetaDto meta) throws IOException, TransformerException, SAXException;
    byte[] xhtml2pdf(Document document, DokumenttiMetaDto meta) throws IOException, TransformerException, SAXException;
}
