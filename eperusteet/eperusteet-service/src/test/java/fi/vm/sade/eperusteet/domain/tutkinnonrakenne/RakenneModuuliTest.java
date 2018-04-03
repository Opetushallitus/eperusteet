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

package fi.vm.sade.eperusteet.domain.tutkinnonrakenne;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author harrik
 */
public class RakenneModuuliTest {

    public RakenneModuuliTest() {
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
    public void testIsSameEriRyhmaRakenne() {
        RakenneModuuli rakenneOld = TestUtils.teeRyhma(
            120, 240, -1, -1,
            TestUtils.teeRyhma(
                90, 90, -1, -1,
                TestUtils.teeRyhma(
                    30, 60, -1, -1
                )
            )
        );

        RakenneModuuli rakenneNew = TestUtils.teeRyhma(
                120, 240, -1, -1,
                TestUtils.teeRyhma(
                        90, 90, -1, -1,
                        TestUtils.teeRyhma(
                                30, 60, -1, -1
                        )
                ),
                TestUtils.teeRyhma(
                        30, 60, -1, -1
                )
        );

        Assert.assertFalse("Rakenteiden pitäisi olla erilaiset", rakenneOld.isSame(rakenneNew, false));
    }

    @Test
    public void testIsSameSamaRyhmaRakenne() {
        RakenneModuuli rakenneOld = TestUtils.teeRyhma(
            120, 240, -1, -1,
            TestUtils.teeRyhma(
                90, 90, -1, -1,
                TestUtils.teeRyhma(
                    30, 60, -1, -1
                )
            )
        );

        RakenneModuuli rakenneNew = TestUtils.teeRyhma(
            120, 240, -1, -1,
            TestUtils.teeRyhma(
                90, 90, -1, -1,
                TestUtils.teeRyhma(
                    30, 60, -1, -1
                )
            )
        );

//        Assert.assertTrue("Rakenteiden pitäisi olla samat", rakenneOld.isSame(rakenneNew));
    }

    @Test
    public void testIsSameEriRakenneOsat() {
        RakenneOsa osa1 = TestUtils.teeRakenneOsa(1, 10);
        RakenneOsa osa2 = TestUtils.teeRakenneOsa(1, 20);

        RakenneModuuli rakenneOld = TestUtils.teeRyhma(
            120, 240, -1, -1,
            osa2,
            TestUtils.teeRyhma(
                90, 90, -1, -1,
                osa1,
                osa2,
                TestUtils.teeRyhma(
                    30, 60, -1, -1,
                    osa1,
                    osa2
                )
            )
        );

        RakenneModuuli rakenneNew = TestUtils.teeRyhma(
            120, 240, -1, -1,
            osa1,
            TestUtils.teeRyhma(
                90, 90, -1, -1,
                osa1,
                osa2,
                TestUtils.teeRyhma(
                    30, 60, -1, -1,
                    osa1,
                    osa2
                )
            )
        );
//        Assert.assertFalse("Rakenneosien pitäisi olla erilaiset", rakenneOld.isSame(rakenneNew));
    }

    @Test
    public void testIsSameSamatRakenneOsat() {
        RakenneOsa osa1 = TestUtils.teeRakenneOsa(1, 10);
        RakenneOsa osa2 = TestUtils.teeRakenneOsa(1, 20);

        RakenneModuuli rakenneOld = TestUtils.teeRyhma(
            120, 240, -1, -1,
            osa1,
            TestUtils.teeRyhma(
                90, 90, -1, -1,
                osa1,
                osa2,
                TestUtils.teeRyhma(
                    30, 60, -1, -1,
                    osa1,
                    osa2
                )
            )
        );

        RakenneModuuli rakenneNew = TestUtils.teeRyhma(
            120, 240, -1, -1,
            osa1,
            TestUtils.teeRyhma(
                90, 90, -1, -1,
                osa1,
                osa2,
                TestUtils.teeRyhma(
                    30, 60, -1, -1,
                    osa1,
                    osa2
                )
            )
        );
//        Assert.assertTrue("Rakenneosien pitäisi olla samat", rakenneOld.isSame(rakenneNew));
    }

    @Test
    public void testIsSameNullInput() {
        RakenneModuuli rakenneOld = TestUtils.teeRyhma(
            120, 240, -1, -1,
            TestUtils.teeRyhma(
                90, 90, -1, -1
            )
        );
        Assert.assertFalse("Rakenneosien pitäisi olla erilaiset", rakenneOld.isSame(null, false));
    }

    @Test
    public void testIsSameMuodostumissaantoNull() {
        RakenneModuuli ryhma = TestUtils.teeRyhma(90, 90, -1, -1);
        RakenneModuuli ryhma2 = TestUtils.teeRyhma(90, 90, -1, -1);
        ryhma2.setMuodostumisSaanto(null);
        ryhma2.setOsat(ryhma.getOsat());

        RakenneModuuli rakenneOld = TestUtils.teeRyhma(
            120, 240, -1, -1,
            ryhma
        );

        RakenneModuuli rakenneNew = TestUtils.teeRyhma(
            120, 240, -1, -1,
            ryhma2
        );
        Assert.assertFalse("Rakenteiden pitäisi olla erilaiset", rakenneOld.isSame(rakenneNew, false));
    }

}