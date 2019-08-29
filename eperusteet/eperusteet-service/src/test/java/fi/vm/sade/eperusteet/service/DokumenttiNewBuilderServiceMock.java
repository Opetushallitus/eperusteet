package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Dokumentti;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiNewBuilderService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

@Service
@Profile("test")
public class DokumenttiNewBuilderServiceMock implements DokumenttiNewBuilderService {
    @Override
    public Document generateXML(Peruste peruste, Dokumentti dokumentti) throws ParserConfigurationException, IOException, TransformerException {
        return null;
    }
}
