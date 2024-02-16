package fi.vm.sade.eperusteet.service.dokumentti;

import fi.vm.sade.eperusteet.domain.Dokumentti;
import fi.vm.sade.eperusteet.domain.Peruste;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public interface DokumenttiNewBuilderService {
    Document generateXML(Peruste peruste, Dokumentti dokumentti)
            throws ParserConfigurationException, IOException, TransformerException;
}
