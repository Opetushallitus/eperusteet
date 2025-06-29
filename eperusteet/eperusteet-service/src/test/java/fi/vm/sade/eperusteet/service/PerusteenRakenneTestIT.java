package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuliRooli;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne;
import org.junit.After;
import org.junit.AfterClass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PerusteenRakenneTestIT {

    public PerusteenRakenneTestIT() {
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
    public void testValidoiRyhmaMaksimiPelastus() {
        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(null, TestUtils.teeRyhma(
            100, 120, -1, -1,
            TestUtils.teeRakenneOsa(1, 10, 70),
            TestUtils.teeRakenneOsa(2, 20),
            TestUtils.teeRakenneOsa(3, 30)
        ), true);
        assertTrue(validoitu.ongelmat.isEmpty());
    }

    @Test
    public void testValidoiRyhmaLiianPieni() {


        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(null, TestUtils.teeRyhma(
            100, 100, -1, -1,
            TestUtils.teeRakenneOsa(1, 10),
            TestUtils.teeRakenneOsa(2, 20),
            TestUtils.teeRakenneOsa(3, 30)
        ));
        assertFalse(validoitu.ongelmat.isEmpty());
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

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(null, rakenne);
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

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(null, rakenne);
        assertTrue(validoitu.ongelmat.size() == 2);
    }

    @Test
    public void testValidoiRyhmaTyhja() {
        RakenneModuuli rakenne = TestUtils.teeRyhma(0, 0, -1, -1);

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(null, rakenne);
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
        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(null, rakenne);
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
        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(null, rakenne);
        assertThat(validoitu.ongelmat).hasSize(3);
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
        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(null, rakenne);
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
        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(null, rakenne);
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
                    -1, -1, -1, -1)),
            TestUtils.teeOsaamisalaRyhma(-1, -1, -1, -1),
            TestUtils.teeRyhma(-1, -1, -1, -1));

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(null, rakenne);
        assertThat(validoitu.ongelmat)
                .extracting(PerusteenRakenne.Ongelma::getOngelma)
                .containsExactlyInAnyOrder(
                        "tutkintonimikkeelta-tai-osaamisalalta-puuttuu-sisalto",
                        "tutkintonimikkeelta-tai-osaamisalalta-puuttuu-sisalto",
                        "tutkinnolle-ei-maaritetty-kokonaislaajuutta",
                        "ryhman-osaamisalaa-ei-perusteella",
                        "ryhman-osaamisalaa-ei-perusteella",
                        "rakenteessa-osaamisaloja-useassa-ryhmassa",
                        "rakenteessa-sisakkaisia-osaamisalaryhmia",
                        "ryhman-osaamisalaa-ei-perusteella",
                        "rakenteessa-osaamisaloja-useassa-ryhmassa");
    }

    @Test
    public void testValidoiTutkinnossaMaariteltavatRyhmat() {
        RakenneModuuli rakenne = TestUtils.teeRyhma(-1, -1, -1, -1,
                TestUtils.teeRakenneOsa(4, -1)
        );
        rakenne.setRooli(RakenneModuuliRooli.VIRTUAALINEN);

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(null, rakenne);
        assertThat(validoitu.ongelmat)
                .extracting(PerusteenRakenne.Ongelma::getOngelma)
                .containsExactlyInAnyOrder(
                        "tutkinnolle-ei-maaritetty-kokonaislaajuutta",
                        "paatason-muodostumisen-rooli-virheellinen",
                        "rakennehierarkia-ei-saa-sisaltaa-tutkinnossa-maariteltavia-ryhmia-joihin-liitetty-osia");
    }

    @Test
    public void testRakenteenKoko() {
        RakenneModuuli rakenne = TestUtils.rakenneModuuli()
                .laajuus(180)
                .koko(2, 2)
                .build();

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(null, rakenne);
        assertThat(validoitu.ongelmat)
                .extracting(PerusteenRakenne.Ongelma::getOngelma)
                .containsExactlyInAnyOrder(
                        "laskettu-laajuuksien-summan-minimi-on-pienempi-kuin-ryhman-vaadittu-minimi",
                        "laskettu-koko-on-pienempi-kuin-vaadittu-minimi");
    }

    @Test
    public void testRakenteenKokoLiianSuuri() {
        RakenneModuuli rakenne = TestUtils.rakenneModuuli()
                .laajuus(150, 180)
                .koko(2, 4)
                .ryhma(r -> r
                        .laajuus(50)
                        .tayta())
                .ryhma(r -> r
                        .laajuus(50)
                        .tayta())
                .ryhma(r -> r
                        .laajuus(60)
                        .tayta())
                .build();

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(null, rakenne);
        assertThat(validoitu.ongelmat)
                .extracting(PerusteenRakenne.Ongelma::getOngelma)
                .containsExactlyInAnyOrder(
                        "laskettu-laajuuksien-summan-maksimi-on-pienempi-kuin-ryhman-vaadittu-maksimi",
                        "laskettu-koko-on-pienempi-kuin-ryhman-vaadittu-maksimi");
    }

}
