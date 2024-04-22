package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.dokumentti.KVLiiteBuilderService;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@DirtiesContext
public class KVLiiteBuilderServiceIT extends AbstractIntegrationTest {

    @Autowired
    PerusteService perusteService;

    @Autowired
    PerusteRepository perusteRepository;

    @Autowired
    KVLiiteBuilderService kvLiiteBuilderService;

    private Peruste peruste;

    @Before
    public void setup() {
        Peruste peruste = perusteService.luoPerusteRunko(
                KoulutusTyyppi.AMMATTITUTKINTO,
                null,
                LaajuusYksikko.OSAAMISPISTE,
                PerusteTyyppi.NORMAALI,
                true
        );

        // Nimi
        Map<Kieli, String> tekstit = new HashMap<>();
        tekstit.put(Kieli.FI, "Nimi");
        TekstiPalanen nimi = TekstiPalanen.of(tekstit);
        peruste.setNimi(nimi);

        Diaarinumero diaarinumero = new Diaarinumero("OPH-0001-2017");
        peruste.setDiaarinumero(diaarinumero);

        peruste = perusteRepository.save(peruste);
        this.peruste = peruste;
    }

    @Test
    @Ignore
    @Transactional
    public void testKVliitePDFLuonti() throws IOException, TransformerException, ParserConfigurationException {
        for (Kieli kieli : Kieli.values()) {
            kvLiiteBuilderService.generateXML(peruste, kieli);
        }
    }
}
