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

package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.MuodostumisSaanto;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author nkala
 */
public class PerusteenRakenneTest {

    public PerusteenRakenneTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    static private RakenneOsa teeRakenneOsa(long id, Integer laajuus) {
        TutkinnonOsa to = new TutkinnonOsa();
        to.setId(id);

        TutkinnonOsaViite tov = new TutkinnonOsaViite();
        tov.setTutkinnonOsa(to);
        tov.setLaajuus(laajuus);

        RakenneOsa ro = new RakenneOsa();
        ro.setTutkinnonOsaViite(tov);
        return ro;
    }

    static private RakenneModuuli teeRyhma(Integer minimi, Integer maksimi, AbstractRakenneOsa... osat) {
        RakenneModuuli rakenne = new RakenneModuuli();
        MuodostumisSaanto ms = new MuodostumisSaanto(new MuodostumisSaanto.Laajuus(minimi, maksimi, LaajuusYksikko.OPINTOVIIKKO));
        ArrayList<AbstractRakenneOsa> aosat = new ArrayList<>();
        aosat.addAll(Arrays.asList(osat));
        rakenne.setOsat(aosat);
        rakenne.setMuodostumisSaanto(ms);
        return rakenne;
    }

    @Test
    public void testValidoiRyhmaValidi() {
        RakenneModuuli rakenne = teeRyhma(
            120, 240,
            teeRakenneOsa(1, 10),
            teeRakenneOsa(2, 20),
            teeRakenneOsa(3, 30),
            teeRakenneOsa(4, 40),
            teeRyhma(
                90, 90,
                teeRakenneOsa(1, 10),
                teeRakenneOsa(2, 20),
                teeRyhma(
                    30, 60,
                    teeRakenneOsa(1, 10),
                    teeRakenneOsa(2, 20),
                    teeRakenneOsa(3, 30)
                )
            )
        );

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(rakenne);
        assertTrue(validoitu.ongelmat.isEmpty());
    }

    @Test
    public void testValidoiRyhmaTyhjä() {
        RakenneModuuli rakenne = teeRyhma(0, 0);

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(rakenne);
        assertTrue(validoitu.ongelmat.isEmpty());
    }

    @Test
    public void testValidoiRyhmaUniikit() {
        RakenneModuuli rakenne = teeRyhma(
            0, 240,
            teeRakenneOsa(1, 10),
            teeRakenneOsa(1, 20),
            teeRakenneOsa(3, 30),
            teeRakenneOsa(4, 40),
            teeRyhma(
                0, 90,
                teeRakenneOsa(1, 10),
                teeRakenneOsa(1, 20)
            )
        );
        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(rakenne);
        assertTrue(validoitu.ongelmat.size() == 2);
    }

    @Test
    public void testValidoiRyhmaKoko() {
        RakenneModuuli rakenne = teeRyhma(
            120, 120,
            teeRyhma(
                60, 90,
                teeRakenneOsa(3, 10),
                teeRakenneOsa(4, 20),
                teeRyhma(
                    30, 30,
                    teeRakenneOsa(5, 20),
                    teeRakenneOsa(6, 20)
                )
            )
        );
        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(rakenne);
        assertTrue(validoitu.ongelmat.size() == 2);
    }
}
