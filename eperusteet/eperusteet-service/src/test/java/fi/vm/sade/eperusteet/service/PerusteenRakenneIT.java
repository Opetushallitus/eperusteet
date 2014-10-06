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

import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne;
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
public class PerusteenRakenneIT {

    public PerusteenRakenneIT() {
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

    @Test
    public void testValidoiRyhmaValidi() {
        RakenneModuuli rakenne = TestUtils.teeRyhma(
            120, 190, -1, -1,
            TestUtils.teeRakenneOsa(1, 10),
            TestUtils.teeRakenneOsa(2, 20),
            TestUtils.teeRakenneOsa(3, 30),
            TestUtils.teeRakenneOsa(4, 40),
            TestUtils.teeRyhma(
                90, 90, -1, -1,
                TestUtils.teeRakenneOsa(1, 10),
                TestUtils.teeRakenneOsa(2, 20),
                TestUtils.teeRyhma(
                    30, 60, -1, -1,
                    TestUtils.teeRakenneOsa(1, 10),
                    TestUtils.teeRakenneOsa(2, 20),
                    TestUtils.teeRakenneOsa(3, 30)
                )
            )
        );

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(rakenne);
        assertTrue(validoitu.ongelmat.isEmpty());
    }

    @Test
    public void testValidoiRyhmaIlmanJuurenMuodostumissaantoa() {
        RakenneModuuli rakenne = TestUtils.teeRyhma(
            -1, -1, -1, -1,
            TestUtils.teeRakenneOsa(1, 10),
            TestUtils.teeRakenneOsa(2, 20),
            TestUtils.teeRakenneOsa(3, 30),
            TestUtils.teeRakenneOsa(4, 40),
            TestUtils.teeRyhma(
                180, 180, -1, -1,
                TestUtils.teeRakenneOsa(1, 10),
                TestUtils.teeRakenneOsa(2, 20),
                TestUtils.teeRyhma(
                    180, 180, -1, -1,
                    TestUtils.teeRakenneOsa(1, 10),
                    TestUtils.teeRakenneOsa(2, 20),
                    TestUtils.teeRakenneOsa(3, 30)
                )
            )
        );

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(rakenne);
        assertTrue(validoitu.ongelmat.size() == 2);
    }

    @Test
    public void testValidoiRyhmaTyhja() {
        RakenneModuuli rakenne = TestUtils.teeRyhma(0, 0, -1, -1);

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(rakenne);
        assertTrue(validoitu.ongelmat.isEmpty());
    }

    @Test
    public void testValidoiRyhmaUniikit() {
        RakenneModuuli rakenne = TestUtils.teeRyhma(
            0, 130, -1, -1,
            TestUtils.teeRakenneOsa(1, 10),
            TestUtils.teeRakenneOsa(1, 20),
            TestUtils.teeRakenneOsa(3, 30),
            TestUtils.teeRakenneOsa(4, 40),
            TestUtils.teeRyhma(
                0, 30, -1, -1,
                TestUtils.teeRakenneOsa(1, 10),
                TestUtils.teeRakenneOsa(1, 20)
            )
        );
        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(rakenne);
        assertTrue(validoitu.ongelmat.size() == 2);
    }

    @Test
    public void testValidoiNullMuodostuminen() {
        RakenneModuuli rakenne = TestUtils.teeRyhma(
            -1, -1, -1, -1,
            TestUtils.teeRakenneOsa(1, 10),
            TestUtils.teeRakenneOsa(1, 20),
            TestUtils.teeRakenneOsa(3, 30),
            TestUtils.teeRakenneOsa(4, 40),
            TestUtils.teeRyhma(
                -1, -1, -1, -1,
                TestUtils.teeRakenneOsa(1, 10),
                TestUtils.teeRakenneOsa(1, 20)
            )
        );
        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(rakenne);
        assertTrue(validoitu.ongelmat.size() == 2);
    }

    @Test
    public void testValidoiNullKokoTaiLaajuus() {
        RakenneModuuli rakenne = TestUtils.teeRyhma(
            0, 130, null, null,
            TestUtils.teeRakenneOsa(1, 10),
            TestUtils.teeRakenneOsa(5, 20),
            TestUtils.teeRakenneOsa(3, 30),
            TestUtils.teeRakenneOsa(4, 40),
            TestUtils.teeRyhma(
                null, null, 0, 2,
                TestUtils.teeRakenneOsa(1, 10),
                TestUtils.teeRakenneOsa(2, 20)
            )
        );
        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(rakenne);
        assertTrue(validoitu.ongelmat.isEmpty());
    }

    @Test
    public void testValidoiRyhmaKoko() {
        RakenneModuuli rakenne = TestUtils.teeRyhma(
            120, 120, -1, -1,
            TestUtils.teeRyhma(
                60, 90, -1, -1,
                TestUtils.teeRakenneOsa(3, 10),
                TestUtils.teeRakenneOsa(4, 20),
                TestUtils.teeRyhma(
                    30, 30, -1, -1,
                    TestUtils.teeRakenneOsa(5, 20),
                    TestUtils.teeRakenneOsa(6, 20)
                )
            )
        );
        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(rakenne);
        assertTrue(validoitu.ongelmat.size() == 2);
    }

    @Test
    public void testValidoiSisakkaisetOsaamisalaRyhmat() {
        RakenneModuuli rakenne = TestUtils.teeRyhma(
            -1, -1, -1, -1,
            TestUtils.teeRakenneOsa(4, 40),
            TestUtils.teeOsaamisalaRyhma(
                180, 180, -1, -1,
                TestUtils.teeOsaamisalaRyhma(
                    -1, -1, -1, -1
                )
            ),
            TestUtils.teeOsaamisalaRyhma(-1, -1, -1, -1),
            TestUtils.teeRyhma(-1, -1, -1, -1)
        );

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(rakenne);
        assertTrue(validoitu.ongelmat.size() == 1);
    }
}
