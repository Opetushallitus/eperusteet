package fi.vm.sade.eperusteet.service.internal;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public interface DokumenttiBuilderService {

    String generateXML(Peruste peruste, Kieli kieli, Suoritustapakoodi suoritustapakoodi)
            throws IOException, TransformerException, ParserConfigurationException;
}
