/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.service;


import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiNewBuilderService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiService;
import fi.vm.sade.eperusteet.service.dokumentti.KVLiiteBuilderService;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author isaul
 */
@DirtiesContext
public class DokumenttiNewBuilderServiceIT extends AbstractIntegrationTest {

    @Autowired
    DokumenttiNewBuilderService dokumenttiNewBuilderService;

    @Autowired
    DokumenttiService dokumenttiService;

    @Autowired
    PerusteService perusteService;

    @Autowired
    PerusteRepository perusteRepository;

    @Autowired
    @Koodisto
    private DtoMapper mapper;

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
    @Ignore
    public void testPerustePDFLuonti() throws IOException, TransformerException, ParserConfigurationException {
        for (Kieli kieli : Kieli.values()) {
            Dokumentti dokumentti = new Dokumentti();
            DokumenttiDto dokumenttiDto = dokumenttiService.createDtoFor(
                    peruste.getId(),
                    kieli,
                    Suoritustapakoodi.REFORMI,
                    GeneratorVersion.UUSI
            );
            mapper.map(dokumenttiDto, dokumentti);
            dokumenttiNewBuilderService.generateXML(peruste, dokumentti);
        }
    }
}
