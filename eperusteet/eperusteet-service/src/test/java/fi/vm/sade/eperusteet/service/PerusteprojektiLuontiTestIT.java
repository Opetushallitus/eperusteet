package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.MuodostumisSaanto;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuliRooli;
import fi.vm.sade.eperusteet.domain.yl.AIPEVaihe;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.opas.OpasDto;
import fi.vm.sade.eperusteet.dto.opas.OpasLuontiDto;
import fi.vm.sade.eperusteet.dto.peruste.*;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.*;
import fi.vm.sade.eperusteet.dto.yl.AIPEVaiheDto;
import fi.vm.sade.eperusteet.repository.KoulutusRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;


@Transactional
public class PerusteprojektiLuontiTestIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private OpasService opasService;

    @Autowired
    private PerusteenOsaViiteService povService;

    @Autowired
    private PerusteRepository repo;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private KoulutusRepository koulutusRepository;

    @Autowired
    @LockCtx(TutkinnonRakenneLockContext.class)
    private LockService<TutkinnonRakenneLockContext> lockService;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteprojektiTestUtils ppTestUtils;

    @Autowired
    private EntityManager em;

    @Test
    @Rollback
    public void testPerusteprojektiaEiVoiJulkaistaIlmanDiaaria() {
        PerusteprojektiDto projekti = ppTestUtils.createPerusteprojekti(perusteprojektiLuontiDto -> {
            perusteprojektiLuontiDto.setKoulutustyyppi(KoulutusTyyppi.ESIOPETUS.toString());
        });
        ppTestUtils.initPeruste(projekti.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setNimi(TestUtils.lt("zäääää"));
            peruste.getNimi().getTekstit().put(Kieli.SV, "ååå");
            peruste.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 2, Calendar.MARCH, 12).getTime());
        });
        TilaUpdateStatus status = perusteprojektiService.updateTila(projekti.getId(), ProjektiTila.VIIMEISTELY, null);
        assertThat(status.isVaihtoOk()).isTrue();
        status = perusteprojektiService.updateTila(projekti.getId(), ProjektiTila.VALMIS, null);
        assertThat(status.isVaihtoOk()).isTrue();
        status = perusteprojektiService.updateTila(projekti.getId(), ProjektiTila.JULKAISTU, TestUtils.createTiedote());
        assertThat(status.isVaihtoOk()).isTrue();

    }

    @Test
    @Rollback
    public void testTutkintoaEiVoiJulkaistaIlmanKokonaislaajuutta() {
        PerusteprojektiDto projekti = ppTestUtils.createPerusteprojekti(perusteprojektiLuontiDto -> {
            perusteprojektiLuontiDto.setReforminMukainen(true);
            perusteprojektiLuontiDto.setKoulutustyyppi(KoulutusTyyppi.ERIKOISAMMATTITUTKINTO.toString());
        });
        PerusteDto perusteDto = ppTestUtils.initPeruste(projekti.getPeruste().getIdLong());

        // Julkaisu
        TilaUpdateStatus status = perusteprojektiService.updateTila(projekti.getId(), ProjektiTila.VIIMEISTELY, null);
        assertThat(status.isVaihtoOk()).isFalse();
        assertThat(status.getInfot()).hasSize(1);
        assertThat(status.getInfot())
                .extracting("viesti")
                .contains("rakenteen-validointi-virhe");
        assertThat(status.getInfot().get(0).getValidointi().ongelmat.get(0).ongelma)
                .isEqualTo("tutkinnolle-ei-maaritetty-kokonaislaajuutta");
        RakenneModuuliDto rakenne = perusteService.getTutkinnonRakenne(perusteDto.getId(), Suoritustapakoodi.REFORMI, 0);
        rakenne.setMuodostumisSaanto(new MuodostumisSaantoDto(new MuodostumisSaantoDto.Laajuus(0, 180, LaajuusYksikko.OSAAMISPISTE)));
        lockService.lock(TutkinnonRakenneLockContext.of(perusteDto.getId(), Suoritustapakoodi.REFORMI));
        rakenne = perusteService.updateTutkinnonRakenne(perusteDto.getId(), Suoritustapakoodi.REFORMI, rakenne);
        status = perusteprojektiService.updateTila(projekti.getId(), ProjektiTila.VIIMEISTELY, null);
        assertThat(status.isVaihtoOk()).isFalse();
    }

    @Test
    @Rollback
    @Ignore
    public void testReforminmukaistaPerusteprojektiaEiVoiJulkaistaTutkinnonOsienTekstisisalloilla() {
        PerusteprojektiDto projekti = ppTestUtils.createPerusteprojekti(perusteprojektiLuontiDto -> {
            perusteprojektiLuontiDto.setReforminMukainen(true);
            perusteprojektiLuontiDto.setKoulutustyyppi(KoulutusTyyppi.ERIKOISAMMATTITUTKINTO.toString());
        });
        PerusteDto perusteDto = ppTestUtils.initPeruste(projekti.getPeruste().getIdLong());
        TutkinnonOsaViiteDto tovDto = ppTestUtils.addTutkinnonOsa(perusteDto.getId(), tov -> {
            tov.getTutkinnonOsaDto().setAmmattitaitovaatimukset(TestUtils.lt("ammatitaitovaatimukset tekstinä"));
//            AmmattitaitovaatimusKohdealueetDto list = new AmmattitaitovaatimusKohdealueetDto();
//            list.
//            tov.getTutkinnonOsaDto().setAmmattitaitovaatimuksetLista();
        });
        RakenneModuuliDto rakenne = perusteService.getTutkinnonRakenne(perusteDto.getId(), Suoritustapakoodi.REFORMI, 0);
        rakenne.setMuodostumisSaanto(new MuodostumisSaantoDto(new MuodostumisSaantoDto.Laajuus(0, 180, LaajuusYksikko.OSAAMISPISTE)));
        assertThat(rakenne.getOsat()).hasSize(0);
        rakenne.getOsat().add(RakenneOsaDto.of(tovDto));
        lockService.lock(TutkinnonRakenneLockContext.of(perusteDto.getId(), Suoritustapakoodi.REFORMI));
        rakenne = perusteService.updateTutkinnonRakenne(perusteDto.getId(), Suoritustapakoodi.REFORMI, rakenne);
        assertThat(rakenne.getOsat()).hasSize(1);

        // Julkaisu
        TilaUpdateStatus status = perusteprojektiService.updateTila(projekti.getId(), ProjektiTila.VIIMEISTELY, null);

        // FIXME
//        assertThat(status.isVaihtoOk()).isFalse();
//        assertThat(status.getInfot().get(0).getViesti()).isEqualTo("tutkinnon-osan-ammattitaitovaatukset-tekstina");
//        status = perusteprojektiService.updateTila(projekti.getId(), ProjektiTila.JULKAISTU, siirtyma);
//        assertThat(status.isVaihtoOk()).isTrue();
    }

    @Test
    @Rollback
    public void testDiaarinumerollaHaku() {
        PerusteprojektiDto projekti = ppTestUtils.createPerusteprojekti(perusteprojektiLuontiDto -> {
            perusteprojektiLuontiDto.setKoulutustyyppi(KoulutusTyyppi.ESIOPETUS.toString());
        });
        PerusteDto perusteDto = ppTestUtils.initPeruste(projekti.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setDiaarinumero("OPH-12345-1234");
            peruste.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 2, Calendar.MARCH, 12).getTime());
        });

        // Julkaisematon ei näy
        assertThat(perusteService.getByDiaari(new Diaarinumero(perusteDto.getDiaarinumero())))
                .isNull();

        // Julkaistu ei näy koska voimassaolo ei ole vielä alkanut
        ppTestUtils.julkaise(projekti.getId(), true);
        {
            PerusteInfoDto peruste = perusteService.getByDiaari(new Diaarinumero(perusteDto.getDiaarinumero()));
            assertThat(peruste).hasFieldOrPropertyWithValue("id", perusteDto.getId());
        }

        // Julkaistu ja voimassaoleva näkyy
        perusteDto = ppTestUtils.editPeruste(projekti.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 3, Calendar.MARCH, 12).getTime());
        });
        assertThat(perusteService.getByDiaari(new Diaarinumero(perusteDto.getDiaarinumero())))
                .hasFieldOrPropertyWithValue("id", perusteDto.getId());

        // Julkaistu näkyy vaikka voimassaolo olisi päättynyt
        perusteDto = ppTestUtils.editPeruste(projekti.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setVoimassaoloLoppuu(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 2, Calendar.MARCH, 12).getTime());
        });
        assertThat(perusteService.getByDiaari(new Diaarinumero(perusteDto.getDiaarinumero())))
                .hasFieldOrPropertyWithValue("id", perusteDto.getId());

        // Julkaistu näkyy vaikka siirtymäaika olisi päättynyt
        perusteDto = ppTestUtils.editPeruste(projekti.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setSiirtymaPaattyy(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 1, Calendar.MARCH, 12).getTime());
        });
        assertThat(perusteService.getByDiaari(new Diaarinumero(perusteDto.getDiaarinumero())))
                .hasFieldOrPropertyWithValue("id", perusteDto.getId());

        // Käytetään uudempaa voimassa olevaa perustetta jos useampia
        PerusteprojektiDto projekti2 = ppTestUtils.createPerusteprojekti(perusteprojektiLuontiDto -> {
            perusteprojektiLuontiDto.setKoulutustyyppi(KoulutusTyyppi.ESIOPETUS.toString());
        });
        PerusteDto perusteDto2 = ppTestUtils.initPeruste(projekti2.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setDiaarinumero("OPH-12345-1234");
            peruste.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 1, Calendar.MARCH, 12).getTime());
        });
        assertThat(perusteService.getByDiaari(new Diaarinumero(perusteDto.getDiaarinumero())))
                .hasFieldOrPropertyWithValue("id", perusteDto.getId());

        ppTestUtils.julkaise(projekti2.getId());
        assertThat(perusteService.getByDiaari(new Diaarinumero(perusteDto.getDiaarinumero())))
                .hasFieldOrPropertyWithValue("id", perusteDto2.getId());

        // Jos vain yksi voimassaoleva julkaistuna
        perusteDto2 = ppTestUtils.editPeruste(projekti2.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 1, Calendar.MARCH, 12).getTime());
        });
        assertThat(perusteService.getByDiaari(new Diaarinumero(perusteDto.getDiaarinumero())))
                .hasFieldOrPropertyWithValue("id", perusteDto.getId());
    }

    @Test
    @Rollback(true)
    public void testVirheellistenPerusteprojektienListaus() {
        PerusteprojektiDto projekti = ppTestUtils.createPerusteprojekti(perusteprojektiLuontiDto -> {
            perusteprojektiLuontiDto.setKoulutustyyppi(KoulutusTyyppi.ESIOPETUS.toString());
        });
        PerusteDto perusteDto = ppTestUtils.initPeruste(projekti.getPeruste().getIdLong(), (PerusteDto peruste) -> {
        });

        ppTestUtils.julkaise(projekti.getId());
        PageRequest preq = new PageRequest(0, 10);
        Page<TilaUpdateStatus> virheelliset = perusteprojektiService.getVirheelliset(preq);
        assertThat(virheelliset)
                .isEmpty();
    }

    @Test
    @Rollback
    public void testPerusteprojektiHakuNimella() {
        PerusteprojektiDto projekti = ppTestUtils.createPerusteprojekti(perusteprojektiLuontiDto -> {
            perusteprojektiLuontiDto.setKoulutustyyppi(KoulutusTyyppi.ESIOPETUS.toString());
        });
        PerusteDto perusteDto = ppTestUtils.initPeruste(projekti.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setNimi(TestUtils.lt("zäääää"));
            peruste.getNimi().getTekstit().put(Kieli.SV, "ååå");
            peruste.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 2, Calendar.MARCH, 12).getTime());
        });
        ppTestUtils.julkaise(projekti.getId());

        PerusteprojektiDto projekti2 = ppTestUtils.createPerusteprojekti(perusteprojektiLuontiDto -> {
            perusteprojektiLuontiDto.setKoulutustyyppi(KoulutusTyyppi.ESIOPETUS.toString());
        });
        PerusteDto perusteDto2 = ppTestUtils.initPeruste(projekti2.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setNimi(TestUtils.lt("xäöäöäöä"));
            peruste.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 2, Calendar.MARCH, 12).getTime());
        });
        ppTestUtils.julkaise(projekti2.getId());

        PerusteQuery pquery = new PerusteQuery();
        Page<PerusteHakuDto> haku = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(haku.getTotalElements()).isEqualTo(2);

        pquery.setNimi("x");
        haku = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(haku.getTotalElements()).isEqualTo(1);

        pquery.setNimi("äää");
        haku = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(haku.getTotalElements()).isEqualTo(1);
        assertThat(haku.getContent().iterator().next().getId()).isEqualTo(perusteDto.getId());

        pquery.setNimi("ö");
        haku = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(haku.getTotalElements()).isEqualTo(1);
        assertThat(haku.getContent().iterator().next().getId()).isEqualTo(perusteDto2.getId());

        pquery.setNimi("å");
        haku = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(haku.getTotalElements()).isEqualTo(1);
        assertThat(haku.getContent().iterator().next().getId()).isEqualTo(perusteDto.getId());
    }

    @Test
    @Rollback
    public void testLuonnissaOlevatEiHakuun() {
        PerusteprojektiDto perusteprojekti = ppTestUtils.createPerusteprojekti((PerusteprojektiLuontiDto pp) -> {
            pp.setKoulutustyyppi(KoulutusTyyppi.ESIOPETUS.toString());
        });
        PerusteDto perusteDto = ppTestUtils.initPeruste(perusteprojekti.getPeruste().getIdLong());
        PerusteQuery pquery = new PerusteQuery();
        Page<PerusteHakuDto> perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);
        ppTestUtils.asetaTila(perusteprojekti.getId(), ProjektiTila.VIIMEISTELY);
        perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);
        ppTestUtils.asetaTila(perusteprojekti.getId(), ProjektiTila.VALMIS);
        perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);
    }

    @Test
    @Rollback
    public void testKoosteenLuominen() {
        OpasLuontiDto luontiDto = new OpasLuontiDto();
        luontiDto.setNimi("Opas");
        luontiDto.setRyhmaOid("abc");
        OpasDto opas = opasService.save(luontiDto);

        ppTestUtils.julkaise(opas.getId());

        List<PerusteKoosteDto> kooste = perusteService.getKooste();
        assertThat(kooste).isEmpty();
    }

    @Test
    @Rollback
    public void testPerusteHakuMapping() {
        Perusteprojekti projekti = new Perusteprojekti();
        projekti.setId(42L);
        Peruste peruste = new Peruste();
        peruste.setPerusteprojekti(projekti);
        PerusteHakuInternalDto hakuDto = mapper.map(peruste, PerusteHakuInternalDto.class);
        assertThat(hakuDto.getPerusteprojekti().getId()).isEqualTo(42L);
    }

    @Test
    @Rollback
    public void testAmosaaJaettuPohja() {
        PerusteKaikkiDto pohja = perusteService.getAmosaaYhteinenPohja();
        assertThat(pohja).isNull();

        PerusteprojektiDto amosaaPohja1 = ppTestUtils.createPerusteprojekti((PerusteprojektiLuontiDto ppl) -> {
            ppl.setTyyppi(PerusteTyyppi.AMOSAA_YHTEINEN);
        });
        PerusteDto perusteDto = ppTestUtils.initPeruste(amosaaPohja1.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 2, Calendar.MARCH, 12).getTime());
        });
        ppTestUtils.luoValidiKVLiite(perusteDto.getId());
        ppTestUtils.asetaTila(amosaaPohja1.getId(), ProjektiTila.VIIMEISTELY);
        ppTestUtils.asetaTila(amosaaPohja1.getId(), ProjektiTila.VALMIS);
        ppTestUtils.asetaTila(amosaaPohja1.getId(), ProjektiTila.JULKAISTU);

        // Amosaa yhteiset eivät tule julkiseen hakuun
        PerusteQuery pquery = new PerusteQuery();
        Page<PerusteHakuDto> perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);

        pohja = perusteService.getAmosaaYhteinenPohja();
        assertThat(pohja).isNotNull().hasFieldOrPropertyWithValue("id", perusteDto.getId());

        PerusteprojektiDto amosaaPohja2 = ppTestUtils.createPerusteprojekti((PerusteprojektiLuontiDto ppl) -> {
            ppl.setTyyppi(PerusteTyyppi.AMOSAA_YHTEINEN);
        });
        PerusteDto perusteDto2 = ppTestUtils.initPeruste(amosaaPohja2.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 1, Calendar.MARCH, 12).getTime());
        });
        ppTestUtils.luoValidiKVLiite(perusteDto2.getId());
        ppTestUtils.asetaTila(amosaaPohja2.getId(), ProjektiTila.VIIMEISTELY);
        ppTestUtils.asetaTila(amosaaPohja2.getId(), ProjektiTila.VALMIS);
        ppTestUtils.asetaTila(amosaaPohja2.getId(), ProjektiTila.JULKAISTU);

        perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);
        PerusteKaikkiDto amosaaYhteinen = perusteService.getAmosaaYhteinenPohja();
        assertThat(amosaaYhteinen)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", perusteDto2.getId());

        // Kaikki tulevat sisäiseen hakuun
        pquery = new PerusteQuery();
        Page<PerusteHakuInternalDto> internalperusteet = perusteService.findByInternal(new PageRequest(0, 10), pquery);
        assertThat(internalperusteet.getTotalElements())
                .isEqualTo(2);
        assertThat(internalperusteet.getContent().stream())
                .extracting("id", "perusteprojekti.id")
                .contains(
                        tuple(perusteDto.getId(), amosaaPohja1.getId()),
                        tuple(perusteDto2.getId(), amosaaPohja2.getId()));
    }

    @Test
    public void testRakenneBuilder() {
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
                        .laajuus(120)
                        .nimi("Muut")
                        .tayta())
                .ryhma(r -> r
                        .nimi("Osaamisalat")
                        .laajuus(60)
                        .ryhma(oa1)
                        .ryhma(oa2))
                .build();

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(
                new PerusteenRakenne.Context(
                        Stream.of(oak1, oak2).collect(Collectors.toSet()),
                        null),
                rakenne);
        assertThat(validoitu.ongelmat).hasSize(0);
    }

    @Test
    public void testTutkintonimikeRyhmallaTaytyyOllaOsia() {
        Koodi koodi = new Koodi();

        TestUtils.RakenneModuuliBuilder nimike = TestUtils.rakenneModuuli()
                .laajuus(0)
                .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                .osaamisala(koodi)
                .nimi(TekstiPalanen.of(Kieli.FI, "nimike"));

        RakenneModuuli rakenne = TestUtils.rakenneModuuli()
                .laajuus(0)
                .ryhma(nimike)
                .build();

        PerusteenRakenne.Validointi validoitu = PerusteenRakenne.validoiRyhma(
                new PerusteenRakenne.Context(
                        Stream.of(koodi).collect(Collectors.toSet()),
                        null),
                rakenne);
        assertThat(validoitu.getOngelmat())
                .extracting(PerusteenRakenne.Ongelma::getOngelma)
                .contains("ryhmalta-puuttuu-sisalto");
    }

    @Test
    @Rollback(true)
    public void testOsaamisaloillaTaytyyOllaKuvaukset() {
        PerusteprojektiDto projekti = ppTestUtils.createPerusteprojekti();
        KoodiDto osaamisala1 = KoodiDto.of("osaamisalat", "1234");
        KoodiDto osaamisala2 = KoodiDto.of("osaamisalat", "12345");

        PerusteDto perusteDto = ppTestUtils.initPeruste(projekti.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            Set<KoodiDto> osaamisalat = Stream.of(osaamisala1, osaamisala2).collect(Collectors.toSet());
            peruste.setOsaamisalat(osaamisalat);
        });

        { // Ilman kuvauksia
            TilaUpdateStatus status = perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.JULKAISTU);
            assertThat(status.getInfot())
                    .extracting(TilaUpdateStatus.Status::getViesti)
                    .contains("osaamisalan-kuvauksia-puuttuu-sisallosta");
        }

        { // Kuvauksien kanssa
            PerusteenOsaViiteDto.Laaja st = perusteService.getSuoritustapaSisalto(perusteDto.getId(), Suoritustapakoodi.REFORMI);
            TekstiKappaleDto tekstiKappale1 = new TekstiKappaleDto();
            tekstiKappale1.setOsaamisala(osaamisala1);
            PerusteenOsaViiteDto.Matala a = perusteService.addSisaltoUUSI(perusteDto.getId(), Suoritustapakoodi.REFORMI, new PerusteenOsaViiteDto.Matala(tekstiKappale1));
            PerusteenOsaViiteDto.Matala b = perusteService.addSisaltoUUSI(perusteDto.getId(), Suoritustapakoodi.REFORMI, new PerusteenOsaViiteDto.Matala(new TekstiKappaleDto()));

            TekstiKappaleDto tekstiKappale2 = new TekstiKappaleDto();
            tekstiKappale2.setOsaamisala(osaamisala2);
            PerusteenOsaViiteDto.Matala c = perusteService.addSisaltoLapsi(perusteDto.getId(), b.getId(), new PerusteenOsaViiteDto.Matala(tekstiKappale2));
            em.flush();
            TilaUpdateStatus status = perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.JULKAISTU);
            assertThat(status.getInfot())
                    .extracting(TilaUpdateStatus.Status::getViesti)
                    .contains("rakenteen-validointi-virhe");
        }
    }

    @Test
    @Rollback(true)
    public void testOsaamisalaTutkintonimikeTutkinnonOsaYhdistelmat() {
        PerusteprojektiDto projekti = ppTestUtils.createPerusteprojekti();
        KoodiDto osaamisala1 = KoodiDto.of("osaamisalat", "1234");
        KoodiDto osaamisala2 = KoodiDto.of("osaamisalat", "12345");

        PerusteDto perusteDto = ppTestUtils.initPeruste(projekti.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            Set<KoodiDto> osaamisalat = Stream.of(osaamisala1, osaamisala2).collect(Collectors.toSet());
            peruste.setOsaamisalat(osaamisalat);
        });
    }

    @Test
    public void testRakenneMapping() {
        RakenneModuuliDto moduuli = new RakenneModuuliDto();
        UUID uuid = UUID.randomUUID();
        moduuli.setTunniste(uuid);

        RakenneModuuli rm = mapper.map(moduuli, RakenneModuuli.class);
        assertThat(rm.getTunniste()).isEqualTo(uuid);
        assertThat(moduuli.getTunniste()).isEqualTo(uuid);

        moduuli = mapper.map(rm, RakenneModuuliDto.class);
        assertThat(rm.getTunniste()).isEqualTo(uuid);
        assertThat(moduuli.getTunniste()).isEqualTo(uuid);
    }

    @Test
    public void testOsaamisalaToKoodiMapping() {
        OsaamisalaDto oa = new OsaamisalaDto();
        oa.setOsaamisalakoodiArvo("1234");
        oa.setOsaamisalakoodiUri("osaamisala_1234");
        Koodi koodi = mapper.map(oa, Koodi.class);
        assertThat(koodi)
                .extracting(Koodi::getUri, Koodi::getKoodisto)
                .contains("osaamisala_1234", "osaamisala");
        OsaamisalaDto oaMapped = mapper.map(koodi, OsaamisalaDto.class);
        assertThat(oaMapped.getOsaamisalakoodiUri()).isEqualTo("osaamisala_1234");
    }

    @Test
    public void testMuodostumisenVertailu() {
        MuodostumisSaanto a = new MuodostumisSaanto(new MuodostumisSaanto.Laajuus(60, 60, null));
        MuodostumisSaanto b = new MuodostumisSaanto(new MuodostumisSaanto.Laajuus(60, 60, null));
        assertThat(a).isEqualTo(b);
    }

    @Test
    public void testVaiheTunnisteMapping() {
        AIPEVaihe vaihe = new AIPEVaihe();
        vaihe.setTunniste(UUID.randomUUID());
        AIPEVaiheDto vaiheDto = mapper.map(vaihe, AIPEVaiheDto.class);
        AIPEVaihe uusivaihe = mapper.map(vaiheDto, AIPEVaihe.class);
        assertThat(vaihe.getTunniste()).isEqualTo(vaiheDto.getTunniste());
        assertThat(vaihe.getTunniste()).isEqualTo(uusivaihe.getTunniste());
    }

}
