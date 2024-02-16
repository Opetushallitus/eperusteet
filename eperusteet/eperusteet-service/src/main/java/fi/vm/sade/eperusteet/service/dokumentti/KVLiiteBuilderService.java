package fi.vm.sade.eperusteet.service.dokumentti;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Peruste;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public interface KVLiiteBuilderService {
    Document generateXML(Peruste peruste, Kieli kieli)
            throws ParserConfigurationException, IOException, TransformerException;
}
