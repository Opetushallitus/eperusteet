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

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuliRooli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author harrik
 */
public class RakenneModuuliTestIT {

    public RakenneModuuliTestIT() {
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

        Assert.assertTrue("Rakenteiden pitäisi olla erilaiset", rakenneOld.isSame(rakenneNew, false).isPresent());
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
        Assert.assertTrue("Rakenneosien pitäisi olla erilaiset", rakenneOld.isSame(null, false).isPresent());
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
        Assert.assertTrue("Rakenteiden pitäisi olla erilaiset", rakenneOld.isSame(rakenneNew, false).isPresent());
    }


    @Test
    public void testOsaamisalatSamallaTasolla() {
        Koodi oak1 = new Koodi();
        Koodi oak2 = new Koodi();

        TestUtils.RakenneModuuliBuilder oa1 = TestUtils.rakenneModuuli()
                .laajuus(60)
                .rooli(RakenneModuuliRooli.OSAAMISALA)
                .osaamisala(oak1)
                .nimi(TekstiPalanen.of(Kieli.FI, "osaamisala 1"))
                .tayta();

        TestUtils.RakenneModuuliBuilder oa2 = TestUtils.rakenneModuuli()
                .laajuus(60)
                .rooli(RakenneModuuliRooli.OSAAMISALA)
                .osaamisala(oak2)
                .nimi(TekstiPalanen.of(Kieli.FI, "osaamisala 2"))
                .tayta();

        RakenneModuuli rakenne = TestUtils.rakenneModuuli()
                .laajuus(180)
                .ryhma(r -> r
                        .laajuus(60)
                        .nimi("Muut")
                        .tayta())
                .ryhma(r -> r
                        .nimi("Osaamisalat")
                        .laajuus(60)
                        .ryhma(oa2))
                .ryhma(r -> r
                        .nimi("Osaamisalat 2")
                        .laajuus(60)
                        .ryhma(oa1))
                .build();

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(
                new PerusteenRakenne.Context(
                        Stream.of(oak1, oak2).collect(Collectors.toSet()),
                        null),
                rakenne);
        assertThat(validoitu.ongelmat).hasSize(1);
    }

    @Test
    public void testTutkintonimikkeet() {
        Koodi tutkintonimike = new Koodi();

        TestUtils.RakenneModuuliBuilder tnRyhma = TestUtils.rakenneModuuli()
                .laajuus(180)
                .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                .osaamisala(tutkintonimike)
                .nimi(TekstiPalanen.of(Kieli.FI, "Tutkintonimike"))
                .tayta();

        RakenneModuuli rakenne = TestUtils.rakenneModuuli()
                .laajuus(180)
                .ryhma(tnRyhma)
                .build();

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(
                new PerusteenRakenne.Context(
                        null,
                        Stream.of(tutkintonimike).collect(Collectors.toSet())),
                rakenne);
        assertThat(validoitu.ongelmat).hasSize(0);
    }

    @Test
    public void testOsaamisalojenJaTutkintonimikkeidenLaajuusHuomioidaanVainKerran() {
        Koodi oak1 = new Koodi();
        Koodi oak2 = new Koodi();
        Koodi tnk1 = new Koodi();
        Koodi tnk2 = new Koodi();

        TestUtils.RakenneModuuliBuilder tn1 = TestUtils.rakenneModuuli()
                .laajuus(20)
                .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                .osaamisala(tnk1)
                .nimi(TekstiPalanen.of(Kieli.FI, "tutkintonimike 1"));

        TestUtils.RakenneModuuliBuilder tn2 = TestUtils.rakenneModuuli()
                .laajuus(10)
                .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                .osaamisala(tnk2)
                .nimi(TekstiPalanen.of(Kieli.FI, "tutkintonimike 2"));

        TestUtils.RakenneModuuliBuilder oa1 = TestUtils.rakenneModuuli()
                .laajuus(100)
                .rooli(RakenneModuuliRooli.OSAAMISALA)
                .osaamisala(oak1)
                .nimi(TekstiPalanen.of(Kieli.FI, "osaamisala 1"));

        TestUtils.RakenneModuuliBuilder oa2 = TestUtils.rakenneModuuli()
                .laajuus(150)
                .rooli(RakenneModuuliRooli.OSAAMISALA)
                .osaamisala(oak2)
                .nimi(TekstiPalanen.of(Kieli.FI, "osaamisala 2"));

        // Ryhmään kiinnitetyistä osaamisaloista käytetään ainoastaan pienimmän muodustmisen omaavaa
        RakenneModuuli rakenne = TestUtils.rakenneModuuli()
                .ryhma(oa1)
                .ryhma(oa2)
                .ryhma(tn1)
                .ryhma(tn2)
                .build();

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(
                new PerusteenRakenne.Context(
                        Stream.of(oak1, oak2).collect(Collectors.toSet()),
                        null),
                rakenne);

        // Huomioidaan vain pienimmät ryhmät -> 10 + 100 = 110
        assertThat(validoitu.getLaskettuLaajuus().compareTo(new BigDecimal(110))).isEqualTo(0);
    }


}