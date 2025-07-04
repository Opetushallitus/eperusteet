package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.MuodostumisSaanto;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuliRooli;
import fi.vm.sade.eperusteet.domain.yl.AIPEVaihe;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.opas.OpasDto;
import fi.vm.sade.eperusteet.dto.opas.OpasLuontiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuInternalDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteInfoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKoosteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.MuodostumisSaantoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.OsaamisalaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEVaiheDto;
import fi.vm.sade.eperusteet.repository.KoulutusRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne;
import fi.vm.sade.eperusteet.service.util.Validointi;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private PerusteRepository perusteRepository;

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
        TilaUpdateStatus status = perusteprojektiService.updateTila(projekti.getId(), ProjektiTila.JULKAISTU, null);
        assertThat(status.isVaihtoOk()).isFalse();
        assertThat(status.getVirheet()).hasSize(2);
        assertThat(status.getVirheet())
                .extracting("kuvaus")
                .contains("rakenteen-validointi-virhe-tutkinnolle-ei-maaritetty-kokonaislaajuutta");
        RakenneModuuliDto rakenne = perusteService.getTutkinnonRakenne(perusteDto.getId(), Suoritustapakoodi.REFORMI, 0);
        rakenne.setMuodostumisSaanto(new MuodostumisSaantoDto(new MuodostumisSaantoDto.Laajuus(0, 180, LaajuusYksikko.OSAAMISPISTE)));
        lockService.lock(TutkinnonRakenneLockContext.of(perusteDto.getId(), Suoritustapakoodi.REFORMI));
        rakenne = perusteService.updateTutkinnonRakenne(perusteDto.getId(), Suoritustapakoodi.REFORMI, rakenne);
        status = perusteprojektiService.updateTila(projekti.getId(), ProjektiTila.JULKAISTU, null);
        assertThat(status.isVaihtoOk()).isFalse();
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
        Page<PerusteHakuDto> haku = perusteService.findJulkinenBy(PageRequest.of(0, 10), pquery);
        assertThat(haku.getTotalElements()).isEqualTo(2);

        pquery.setNimi("x");
        haku = perusteService.findJulkinenBy(PageRequest.of(0, 10), pquery);
        assertThat(haku.getTotalElements()).isEqualTo(1);

        pquery.setNimi("äää");
        haku = perusteService.findJulkinenBy(PageRequest.of(0, 10), pquery);
        assertThat(haku.getTotalElements()).isEqualTo(1);
        assertThat(haku.getContent().iterator().next().getId()).isEqualTo(perusteDto.getId());

        pquery.setNimi("ö");
        haku = perusteService.findJulkinenBy(PageRequest.of(0, 10), pquery);
        assertThat(haku.getTotalElements()).isEqualTo(1);
        assertThat(haku.getContent().iterator().next().getId()).isEqualTo(perusteDto2.getId());

        pquery.setNimi("å");
        haku = perusteService.findJulkinenBy(PageRequest.of(0, 10), pquery);
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
        Page<PerusteHakuDto> perusteet = perusteService.findJulkinenBy(PageRequest.of(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);
        ppTestUtils.asetaTila(perusteprojekti.getId(), ProjektiTila.VIIMEISTELY);
        perusteet = perusteService.findJulkinenBy(PageRequest.of(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);
        ppTestUtils.asetaTila(perusteprojekti.getId(), ProjektiTila.VALMIS);
        perusteet = perusteService.findJulkinenBy(PageRequest.of(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);
    }

    @Test
    @Rollback
    public void testKoosteenLuominen() {
        OpasLuontiDto luontiDto = new OpasLuontiDto();
        luontiDto.setNimi("Opas");
        luontiDto.setRyhmaOid("abc");
        OpasDto opas = opasService.save(luontiDto);

        Peruste perustePohja = perusteRepository.findOne(opas.getPeruste().getIdLong());
        perustePohja.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 2, Calendar.MARCH, 12).getTime());
        perusteRepository.save(perustePohja);

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
    public void testAmosaaJaettujaPohjiaSaaOllaVainYksi() {
        assertThatThrownBy(() -> perusteService.getAmosaaYhteinenPohja()).isInstanceOf(BusinessRuleViolationException.class);

        PerusteprojektiDto amosaaPohja1 = ppTestUtils.createPerusteprojekti((PerusteprojektiLuontiDto ppl) -> {
            ppl.setTyyppi(PerusteTyyppi.AMOSAA_YHTEINEN);
        });

        Peruste p1 = perusteRepository.findOne(amosaaPohja1.getPeruste().getIdLong());
        p1.asetaTila(PerusteTila.VALMIS);
        perusteRepository.save(p1);
        assertThat(perusteService.getAmosaaYhteinenPohja()).isNotNull();

        PerusteprojektiDto amosaaPohja2 = ppTestUtils.createPerusteprojekti((PerusteprojektiLuontiDto ppl) -> {
            ppl.setTyyppi(PerusteTyyppi.AMOSAA_YHTEINEN);
        });
        assertThat(perusteService.getAmosaaYhteinenPohja()).isNotNull();

        Peruste p2 = perusteRepository.findOne(amosaaPohja2.getPeruste().getIdLong());
        p2.asetaTila(PerusteTila.VALMIS);
        perusteRepository.save(p2);
        assertThatThrownBy(() -> perusteService.getAmosaaYhteinenPohja()).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @Rollback
    public void testAmosaaJaetutEivatTuleJulkiseenHakuun() {
        PerusteprojektiDto amosaaPohja1 = ppTestUtils.createPerusteprojekti((PerusteprojektiLuontiDto ppl) -> {
            ppl.setTyyppi(PerusteTyyppi.AMOSAA_YHTEINEN);
        });
        PerusteDto perusteDto = ppTestUtils.initPeruste(amosaaPohja1.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 2, Calendar.MARCH, 12).getTime());
        });
        ppTestUtils.luoValidiKVLiite(perusteDto.getId());
        Peruste p1 = perusteRepository.findOne(amosaaPohja1.getPeruste().getIdLong());
        perusteRepository.save(p1);
        p1.asetaTila(PerusteTila.VALMIS);
        PerusteQuery pquery = new PerusteQuery();
        Page<PerusteHakuDto> perusteet = perusteService.findJulkinenBy(PageRequest.of(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);
    }

    @Test
    @Rollback
    public void testAmosaaJaettuPohja() {
        assertThatThrownBy(() -> perusteService.getAmosaaYhteinenPohja()).isInstanceOf(BusinessRuleViolationException.class);

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
        Page<PerusteHakuDto> perusteet = perusteService.findJulkinenBy(PageRequest.of(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);

        PerusteKaikkiDto pohja = perusteService.getAmosaaYhteinenPohja();
        assertThat(pohja).isNotNull().hasFieldOrPropertyWithValue("id", perusteDto.getId());

        PerusteprojektiDto amosaaPohja2 = ppTestUtils.createPerusteprojekti((PerusteprojektiLuontiDto ppl) -> {
            ppl.setTyyppi(PerusteTyyppi.AMOSAA_YHTEINEN);
        });
        PerusteDto perusteDto2 = ppTestUtils.initPeruste(amosaaPohja2.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 1, Calendar.MARCH, 12).getTime());
        });

        perusteet = perusteService.findJulkinenBy(PageRequest.of(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);
        PerusteKaikkiDto amosaaYhteinen = perusteService.getAmosaaYhteinenPohja();
        assertThat(amosaaYhteinen)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", perusteDto.getId());

        // Kaikki tulevat sisäiseen hakuun
        pquery = new PerusteQuery();
        Page<PerusteHakuInternalDto> internalperusteet = perusteService.findByInternal(PageRequest.of(0, 10), pquery);
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
        oak1.setUri("urikoodi_123");
        Koodi oak2 = new Koodi();
        oak2.setUri("urikoodi_234");

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
                .contains("tutkintonimikkeelta-tai-osaamisalalta-puuttuu-sisalto");
    }

    @Test
    @Rollback(true)
    public void testOsaamisaloillaTaytyyOllaKuvaukset() {
        PerusteprojektiDto projekti = ppTestUtils.createPerusteprojekti();
        KoodiDto osaamisala1 = KoodiDto.of("osaamisala", "1234");
        KoodiDto osaamisala2 = KoodiDto.of("osaamisala", "12345");

        PerusteDto perusteDto = ppTestUtils.initPeruste(projekti.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            Set<KoodiDto> osaamisalat = Stream.of(osaamisala1, osaamisala2).collect(Collectors.toSet());
            peruste.setOsaamisalat(osaamisalat);
        });

        { // Ilman kuvauksia
            TilaUpdateStatus status = new TilaUpdateStatus(perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.JULKAISTU));
            assertThat(status.getVirheet())
                    .extracting(Validointi.Virhe::getKuvaus)
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
            TilaUpdateStatus status = new TilaUpdateStatus(perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.JULKAISTU));
            assertThat(status.getVirheet())
                    .extracting(Validointi.Virhe::getKuvaus)
                    .contains("rakenteen-validointi-virhe-tutkinnolle-ei-maaritetty-kokonaislaajuutta");
        }
    }

    @Test
    @Rollback(true)
    public void testOsaamisalaTutkintonimikeTutkinnonOsaYhdistelmat() {
        PerusteprojektiDto projekti = ppTestUtils.createPerusteprojekti();
        KoodiDto osaamisala1 = KoodiDto.of("osaamisala", "1234");
        KoodiDto osaamisala2 = KoodiDto.of("osaamisala", "12345");

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
