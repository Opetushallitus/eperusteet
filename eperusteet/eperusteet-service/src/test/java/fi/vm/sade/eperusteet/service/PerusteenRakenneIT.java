package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuliRooli;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKevytDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.*;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import jakarta.validation.ConstraintViolationException;

import fi.vm.sade.eperusteet.service.util.Validointi;
import net.bytebuddy.implementation.bytecode.Throw;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

@Transactional
@DirtiesContext
public class PerusteenRakenneIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private PerusteprojektiTestUtils testUtils;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    @LockCtx(TutkinnonRakenneLockContext.class)
    private LockService<TutkinnonRakenneLockContext> lockService;

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;

    @Autowired
    private TutkinnonOsaRepository tutkinnonOsaRepository;

    private Perusteprojekti projekti;
    private Peruste peruste;
    private Suoritustapa suoritustapa;
    private RakenneModuuli rakenne;

    @Before
    public void setup() {
        PerusteprojektiDto projektiDto = testUtils.createPerusteprojekti();
        projekti = perusteprojektiRepository.findById(projektiDto.getId()).orElseThrow();
        peruste = projekti.getPeruste();
        suoritustapa = peruste.getSuoritustapa(Suoritustapakoodi.REFORMI);
        rakenne = suoritustapa.getRakenne();
    }

    private RakenneModuuliDto getRakenneDto() {
        return perusteService.getTutkinnonRakenne(peruste.getId(), suoritustapa.getSuoritustapakoodi(), null);
    }

    private PerusteDto update(PerusteDto perusteDto) {
        return perusteService.update(perusteDto.getId(), perusteDto);
    }

    private RakenneModuuliDto update(RakenneModuuliDto rakenne) {
        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(peruste.getId(), Suoritustapakoodi.REFORMI);
        lockService.lock(ctx);
        RakenneModuuliDto updated = perusteService.updateTutkinnonRakenne(peruste.getId(), suoritustapa.getSuoritustapakoodi(), rakenne);
        return updated;
    }

    private TutkinnonOsaViiteDto uusiTutkinnonOsa(TutkinnonOsaDto tosaDto) {
        TutkinnonOsaViiteDto result = perusteService.addTutkinnonOsa(peruste.getId(), suoritustapa.getSuoritustapakoodi(), TutkinnonOsaViiteDto.builder()
                .tyyppi(TutkinnonOsaTyyppi.NORMAALI)
                .tutkinnonOsaDto(tosaDto)
                .build());
        return result;
    }

    private TutkinnonOsaViiteDto uusiTutkinnonOsa() {
        return uusiTutkinnonOsa(TutkinnonOsaDto.builder()
                        .build());
    }

    @Test
    @Rollback
    public void testUusiRakenne() {
        assertThat(rakenne).isNotNull();
        assertThat(rakenne)
                .extracting(
                        "muodostumisSaanto.laajuus.minimi",
                        "muodostumisSaanto.laajuus.maksimi",
                        "muodostumisSaanto.koko.minimi",
                        "muodostumisSaanto.koko.maksimi")
                .contains(null, null, null, null);
    }

    @Test
    @Rollback
    public void testTestDeprekoitunutRakenteenKoko() {
        RakenneModuuliDto rakenneDto = getRakenneDto();
        rakenneDto.setMuodostumisSaanto(MuodostumisSaantoDto.builder()
                .koko(MuodostumisSaantoDto.Koko.builder()
                        .maksimi(180)
                        .minimi(180)
                        .build())
                .build());
        RakenneModuuliDto updated = update(rakenneDto);
        assertThat(updated.getMuodostumisSaanto().getKoko()).isNull();
    }

    @Test
    @Rollback
    public void testRakenteenTunnisteidenSailyminen() {
        assertThat(rakenne).isNotNull();
        RakenneModuuliDto rakenneDto = getRakenneDto();

        final UUID rootTunniste = rakenneDto.getTunniste();
        rakenneDto.getOsat()
                .addAll(Arrays.asList(
                        RakenneOsaDto.of(uusiTutkinnonOsa()),
                        RakenneModuuliDto.builder()
                                .osat(Arrays.asList(
                                        RakenneOsaDto.of(uusiTutkinnonOsa())))
                                .build()));

        rakenneDto = update(rakenneDto);

        assertThat(rakenneDto.getTunniste()).isEqualTo(rootTunniste);

        { // Tunnisteet säilyvät muokkauksilla
            final UUID a = rakenneDto.getOsat().get(0).getTunniste();
            final UUID b = rakenneDto.getOsat().get(1).getTunniste();
            final UUID c = ((RakenneModuuliDto)rakenneDto.getOsat().get(1)).getOsat().get(0).getTunniste();
            assertThat(Arrays.asList(a, b, c)).doesNotContainNull();

            ((RakenneModuuliDto)rakenneDto.getOsat().get(1)).getOsat().add(RakenneModuuliDto.builder().build());
            ((RakenneModuuliDto)rakenneDto.getOsat().get(1)).getOsat().add(0, RakenneModuuliDto.builder().build());
            rakenneDto.getOsat().add(0, RakenneOsaDto.of(uusiTutkinnonOsa()));
            rakenneDto.getOsat().add(RakenneOsaDto.of(uusiTutkinnonOsa()));
            rakenneDto = update(rakenneDto);

            assertThat(Stream.of(
                        rakenneDto,
                        rakenneDto.getOsat().get(1),
                        rakenneDto.getOsat().get(2),
                        ((RakenneModuuliDto)rakenneDto.getOsat().get(2)).getOsat().get(1))
                    .map(AbstractRakenneOsaDto::getTunniste))
                    .containsExactly(rootTunniste, a, b, c);
        }
    }

    @Test(expected = Exception.class)
    @Rollback
    public void testEiSallitaSamaaTunnistetta() {
        startNewTransaction();
        setup();
        startNewTransaction();
        //h2 ei aseta constrainttia
        em.createNativeQuery("ALTER TABLE tutkinnon_rakenne ADD CONSTRAINT tunnisteuniikki UNIQUE (tunniste)").executeUpdate();
        RakenneModuuliDto rakenneDto = getRakenneDto();
        rakenneDto.getOsat().add(RakenneOsaDto.of(uusiTutkinnonOsa()));
        rakenneDto.getOsat().add(RakenneOsaDto.of(uusiTutkinnonOsa()));
        final RakenneModuuliDto updated = update(rakenneDto);

        updated.getOsat().get(0).setTunniste(updated.getOsat().get(1).getTunniste());

        startNewTransaction();
        update(updated);
        endTransaction();
    }

    @Test
    @Rollback
    public void testRegressionRakenneMuuttunutJarjestys() {
        RakenneModuuliDto rakenneDto = getRakenneDto();
        rakenneDto.setOsat(Arrays.asList(
                RakenneModuuliDto.builder().build(),
                RakenneModuuliDto.builder().build()));

        rakenneDto = update(rakenneDto);

        List<UUID> tunnisteet = rakenneDto.getOsat().stream().map(AbstractRakenneOsaDto::getTunniste)
                .collect(Collectors.toList());
        assertThat(tunnisteet).doesNotContainNull().hasSize(2);
        UUID at = tunnisteet.get(0);
        UUID bt = tunnisteet.get(1);

        Collections.swap(rakenneDto.getOsat(), 0, 1);

        rakenneDto = update(rakenneDto);
        assertThat(rakenneDto.getOsat().stream().map(AbstractRakenneOsaDto::getTunniste))
                .containsExactly(bt, at);
    }

    @Test
    @Rollback
    public void testTutkintonimikekoodeihinPerusteAutomaattisesti() {
        TutkintonimikeKoodiDto koodi = perusteService.addTutkintonimikeKoodi(peruste.getId(), TutkintonimikeKoodiDto.builder()
                .tutkintonimikeArvo("1001")
                .tutkintonimikeUri("tutkintonimike_1001").build());

        assertThat(koodi.getPeruste().getIdLong())
                .isEqualTo(peruste.getId());
    }

    @Test
    @Rollback
    public void testRegressionRakenteessaUsampiTutkintonimikeIlmanOsaamisaloja() {
        perusteService.addTutkintonimikeKoodi(peruste.getId(), TutkintonimikeKoodiDto.builder()
                .tutkintonimikeArvo("1001")
                .tutkintonimikeUri("tutkintonimikkeet_1001").build());

        perusteService.addTutkintonimikeKoodi(peruste.getId(), TutkintonimikeKoodiDto.builder()
                .tutkintonimikeArvo("1002")
                .tutkintonimikeUri("tutkintonimikkeet_1002").build());

        RakenneModuuliDto rakenneDto = getRakenneDto();
        rakenneDto.setOsat(Arrays.asList(
                RakenneModuuliDto.builder()
                        .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                        .tutkintonimike(KoodiDto.of("tutkintonimikkeet", "1001"))
                        .build(),
                RakenneModuuliDto.builder()
                        .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                        .tutkintonimike(KoodiDto.of("tutkintonimikkeet", "1002"))
                        .build()));
        RakenneModuuliDto rakenne = update(rakenneDto);
        List<Validointi> validoinnit = perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.JULKAISTU);
        assertThat(validoinnit.stream().map(Validointi::getVirheet).flatMap(Collection::stream).map(Validointi.Virhe::getKuvaus))
            .doesNotContain("tutkintonimikkeen-osaamisala-puuttuu-perusteesta");
    }

    // Relates:
    // - EP-1533
    @Test
    @Rollback
    public void testDuplikaatitTutkintonimikkeetEriTutkinnonOsalla() {
        perusteService.addTutkintonimikeKoodi(peruste.getId(), TutkintonimikeKoodiDto.builder()
                .tutkinnonOsaArvo("1")
                .tutkinnonOsaUri("tutkinnonosa_1")
                .tutkintonimikeArvo("1")
                .tutkintonimikeUri("tutkintonimikkeet_1").build());

        perusteService.addTutkintonimikeKoodi(peruste.getId(), TutkintonimikeKoodiDto.builder()
                .tutkinnonOsaArvo("2")
                .tutkinnonOsaUri("tutkinnonosa_2")
                .tutkintonimikeArvo("1")
                .tutkintonimikeUri("tutkintonimikkeet_1").build());

        TutkinnonOsaViiteDto a = uusiTutkinnonOsa(TutkinnonOsaDto.builder()
                .koodi(KoodiDto.of("tutkinnonosa", "1"))
            .build());

        TutkinnonOsaViiteDto b = uusiTutkinnonOsa(TutkinnonOsaDto.builder()
                .koodi(KoodiDto.of("tutkinnonosa", "2"))
                .build());

        RakenneModuuliDto rakenneDto = getRakenneDto();
        rakenneDto.setOsat(new ArrayList<>(Arrays.asList(
                RakenneModuuliDto.builder()
                        .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                        .tutkintonimike(KoodiDto.of("tutkintonimikkeet", "1"))
                        .osat(Collections.singletonList(RakenneOsaDto.of(a)))
                        .build(),
                RakenneModuuliDto.builder()
                        .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                        .tutkintonimike(KoodiDto.of("tutkintonimikkeet", "1"))
                        .osat(Collections.singletonList(RakenneOsaDto.of(b)))
                        .build())));

        assertThatCode(() -> update(rakenneDto))
                .doesNotThrowAnyException();

        TutkinnonOsaViiteDto c = uusiTutkinnonOsa(TutkinnonOsaDto.builder()
                .koodi(KoodiDto.of("tutkinnonosa", "2"))
                .build());

        rakenneDto.getOsat()
                .add(RakenneModuuliDto.builder()
                        .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                        .tutkintonimike(KoodiDto.of("tutkintonimikkeet", "1"))
                        .osat(Collections.singletonList(RakenneOsaDto.of(c)))
                        .build());

        assertThatCode(() -> update(rakenneDto))
                .hasMessage("tutkintonimikeryhmalle-maaritetty-tutkinnon-osa-useaan-kertaan");
    }

    // Relates:
    // - EP-1533
    @Test
    @Rollback
    public void testDuplikaatitOsaamisalat() {
        perusteService.addTutkintonimikeKoodi(peruste.getId(), TutkintonimikeKoodiDto.builder()
                .osaamisalaArvo("1")
                .osaamisalaUri("osaamisala_1")
                .tutkintonimikeArvo("1")
                .tutkintonimikeUri("tutkintonimikkeet_1").build());

        perusteService.addTutkintonimikeKoodi(peruste.getId(), TutkintonimikeKoodiDto.builder()
                .osaamisalaArvo("1")
                .osaamisalaUri("osaamisala_1")
                .tutkintonimikeArvo("1")
                .tutkintonimikeUri("tutkintonimikkeet_2").build());

        TutkinnonOsaViiteDto a = uusiTutkinnonOsa(TutkinnonOsaDto.builder()
                .koodi(KoodiDto.of("tutkinnonosa", "1"))
                .build());

        TutkinnonOsaViiteDto b = uusiTutkinnonOsa(TutkinnonOsaDto.builder()
                .koodi(KoodiDto.of("tutkinnonosa", "2"))
                .build());

        RakenneModuuliDto rakenneDto = getRakenneDto();
        rakenneDto.setOsat(new ArrayList<>(Arrays.asList(
                RakenneModuuliDto.builder()
                        .rooli(RakenneModuuliRooli.OSAAMISALA)
                        .osaamisala(OsaamisalaDto.of("2"))
                        .build(),
                RakenneModuuliDto.builder()
                        .rooli(RakenneModuuliRooli.OSAAMISALA)
                        .osaamisala(OsaamisalaDto.of("1"))
                        .osat(Collections.singletonList(RakenneModuuliDto.builder()
                                .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                                .tutkintonimike(KoodiDto.of("tutkintonimikkeet", "1"))
                                .osat(Collections.singletonList(RakenneOsaDto.of(a)))
                            .build()))
                        .build(),
                RakenneModuuliDto.builder()
                        .rooli(RakenneModuuliRooli.OSAAMISALA)
                        .osaamisala(OsaamisalaDto.of("1"))
                        .osat(Collections.singletonList(RakenneModuuliDto.builder()
                                .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                                .tutkintonimike(KoodiDto.of("tutkintonimikkeet", "2"))
                                .osat(Collections.singletonList(RakenneOsaDto.of(a)))
                                .build()))
                        .build())));

        assertThatCode(() -> update(rakenneDto))
                .doesNotThrowAnyException();

        TutkinnonOsaViiteDto c = uusiTutkinnonOsa(TutkinnonOsaDto.builder()
                .koodi(KoodiDto.of("tutkinnonosa", "2"))
                .build());

        rakenneDto.getOsat()
                .add(RakenneModuuliDto.builder()
                    .rooli(RakenneModuuliRooli.OSAAMISALA)
                    .osaamisala(OsaamisalaDto.of("1"))
                    .osat(Collections.singletonList(RakenneModuuliDto.builder()
                            .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                            .tutkintonimike(KoodiDto.of("tutkintonimikkeet", "1"))
                            .osat(Collections.singletonList(RakenneOsaDto.of(a)))
                            .build()))
                    .build());

        assertThatCode(() -> update(rakenneDto))
                .hasMessage("osaamisala-liitetty-virheelliseti-tutkinnon-osiin");
    }

    @Test
    @Rollback
    public void testValidoiTutkinnossaMaariteltavatRyhmat() {
        PerusteprojektiLuontiDto ppldto = new PerusteprojektiLuontiDto(KoulutusTyyppi.PERUSTUTKINTO.toString(),
                LaajuusYksikko.OSAAMISPISTE, null, null, PerusteTyyppi.NORMAALI, "1.2.246.562.28.11287634288");
        ppldto.setReforminMukainen(false);
        ppldto.setNimi(TestUtils.uniikkiString());
        ppldto.setDiaarinumero(TestUtils.uniikkiDiaari());
        ppldto.setReforminMukainen(true);
        PerusteprojektiDto perusteprojektiDto = perusteprojektiService.save(ppldto);

        Perusteprojekti pp = perusteprojektiRepository.findById(perusteprojektiDto.getId()).orElseThrow();
        Peruste peruste = pp.getPeruste();

        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(peruste.getId(), Suoritustapakoodi.REFORMI);
        lockService.lock(ctx);

        perusteService.addSisalto(pp.getPeruste().getId(), Suoritustapakoodi.REFORMI, null);


        peruste.getSuoritustavat().forEach(suoritustapa -> {
            assertThatThrownBy(() -> {
                RakenneModuuliDto rakenne = mapper.map(suoritustapa.getRakenne(), RakenneModuuliDto.class);
                RakenneModuuli virtuaaliryhma = TestUtils.rakenneModuuli()
                        .laajuus(5)
                        .rooli(RakenneModuuliRooli.VIRTUAALINEN)
                        .tayta()
                        .build();

                RakenneModuuliDto moduuli = mapper.map(virtuaaliryhma, RakenneModuuliDto.class);
                rakenne.getOsat().add(moduuli);

                perusteService.updateTutkinnonRakenne(peruste.getId(), suoritustapa.getSuoritustapakoodi(), rakenne);
            }).isInstanceOf(BusinessRuleViolationException.class)
            .hasMessage("ryhman-rooli-ei-salli-sisaltoa");
        });

        lockService.unlock(ctx);
    }

    @Test
    public void testLisaaRuotsinnosJulkaistuunPerusteeseen() {
        TutkinnonOsaViiteDto dto = testUtils.addTutkinnonOsa(peruste.getId());
        RakenneModuuliDto rakenne = perusteService.getTutkinnonRakenne(peruste.getId(),
                Suoritustapakoodi.REFORMI, 0);
        rakenne.getOsat().add(RakenneOsaDto.of(dto));

        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(peruste.getId(),
                Suoritustapakoodi.REFORMI);

        lockService.lock(ctx);
        rakenne = perusteService.updateTutkinnonRakenne(peruste.getId(), Suoritustapakoodi.REFORMI, rakenne);
        lockService.unlock(ctx);

        perusteprojektiService.updateTila(projekti.getId(), ProjektiTila.VIIMEISTELY, null);
        perusteprojektiService.updateTila(projekti.getId(), ProjektiTila.VALMIS, null);
        perusteprojektiService.updateTila(projekti.getId(), ProjektiTila.JULKAISTU, new TiedoteDto());

        // Muutetaan muodostumisen kuvaus
        rakenne.setKuvaus(LokalisoituTekstiDto.of("kuvaus"));

        lockService.lock(ctx);
        perusteService.updateTutkinnonRakenne(peruste.getId(), Suoritustapakoodi.REFORMI, rakenne);
        lockService.unlock(ctx);

    }

    @Test
    public void testTutkinnonOsaTuonti() {
        Peruste peruste1 = testUtils.createPeruste();
        Peruste peruste2 = testUtils.createPeruste();
        Peruste peruste3 = testUtils.createPeruste();

        TutkinnonOsaViiteDto tutkinnonOsaViiteDto1 = testUtils.addTutkinnonOsa(peruste1.getId());
        TutkinnonOsaViiteDto tutkinnonOsaViiteDto2 = testUtils.addTutkinnonOsa(peruste1.getId());

        List<TutkinnonOsaViiteDto> peruste1viitteet = perusteService.getTutkinnonOsat(peruste1.getId(), Suoritustapakoodi.REFORMI);
        assertThat(peruste1viitteet).hasSize(2);
        assertThat(peruste1viitteet).extracting("tutkinnonOsaDto.alkuperainenPeruste.id").containsExactly(peruste1.getId(), peruste1.getId());
        assertThat(peruste1viitteet).extracting("tutkinnonOsaDto.koodi").doesNotContainNull();

        perusteService.attachTutkinnonOsa(peruste2.getId(), Suoritustapakoodi.REFORMI, tutkinnonOsaViiteDto1);
        List<TutkinnonOsaViiteDto> peruste2viitteet = perusteService.getTutkinnonOsat(peruste2.getId(), Suoritustapakoodi.REFORMI);
        assertThat(peruste2viitteet).hasSize(1);
        assertThat(peruste2viitteet).extracting("tutkinnonOsaDto.alkuperainenPeruste.id").containsExactly(peruste1.getId());
        assertThat(peruste2viitteet).extracting("tutkinnonOsaDto.id").containsExactly(tutkinnonOsaViiteDto1.getTutkinnonOsa().getIdLong());

        TutkinnonOsaViiteLuontiDto luontiDto = new TutkinnonOsaViiteLuontiDto();
        luontiDto.setNimi(tutkinnonOsaViiteDto2.getNimi());
        luontiDto.setTyyppi(TutkinnonOsaTyyppi.NORMAALI);
        luontiDto.setTutkinnonOsa(tutkinnonOsaViiteDto2.getTutkinnonOsa());
        TutkinnonOsaViiteDto kopio = perusteService.attachTutkinnonOsa(peruste3.getId(), Suoritustapakoodi.REFORMI, luontiDto, mapper.map(peruste1, PerusteKevytDto.class));
        perusteenOsaViiteService.kloonaaTutkinnonOsa(peruste3.getId(), Suoritustapakoodi.REFORMI, kopio.getId());
        List<TutkinnonOsaViiteDto> peruste3viitteet = perusteService.getTutkinnonOsat(peruste3.getId(), Suoritustapakoodi.REFORMI);
        assertThat(peruste3viitteet).hasSize(1);
        assertThat(peruste3viitteet).extracting("tutkinnonOsaDto.alkuperainenPeruste.id").containsExactly(peruste3.getId());
        assertThat(peruste3viitteet).extracting("tutkinnonOsaDto.id").isNotIn(tutkinnonOsaViiteDto1.getTutkinnonOsa().getIdLong(), tutkinnonOsaViiteDto2.getTutkinnonOsa().getIdLong());
        assertThat(peruste3viitteet).extracting("tutkinnonOsaDto.koodi").containsOnlyNulls();

        peruste1viitteet = perusteService.getTutkinnonOsat(peruste1.getId(), Suoritustapakoodi.REFORMI);
        assertThat(peruste1viitteet).extracting("tutkinnonOsaDto.koodi").doesNotContainNull();
    }

    @Test
    public void testTutkinnonOsaTuonti_ei_perustetta() {
        Peruste peruste1 = testUtils.createPeruste();
        Peruste peruste2 = testUtils.createPeruste();
        Peruste peruste3 = testUtils.createPeruste();

        TutkinnonOsaViiteDto tutkinnonOsaViiteDto1 = testUtils.addTutkinnonOsa(peruste1.getId());
        List<TutkinnonOsaViiteDto> peruste1viitteet = perusteService.getTutkinnonOsat(peruste1.getId(), Suoritustapakoodi.REFORMI);
        assertThat(peruste1viitteet).extracting("tutkinnonOsaDto.alkuperainenPeruste.id").containsExactly(peruste1.getId());

        TutkinnonOsa tutkinnonOsa = tutkinnonOsaRepository.findOne(tutkinnonOsaViiteDto1.getTutkinnonOsa().getIdLong());
        tutkinnonOsa.asetaAlkuperainenPeruste(null);
        tutkinnonOsaRepository.save(tutkinnonOsa);

        perusteService.attachTutkinnonOsa(peruste2.getId(), Suoritustapakoodi.REFORMI, tutkinnonOsaViiteDto1);

        List<TutkinnonOsaViiteDto> peruste2viitteet = perusteService.getTutkinnonOsat(peruste2.getId(), Suoritustapakoodi.REFORMI);
        assertThat(peruste2viitteet).hasSize(1);
        assertThat(peruste2viitteet.get(0)).extracting("tutkinnonOsaDto.alkuperainenPeruste").isNull();
        assertThat(peruste2viitteet).extracting("tutkinnonOsaDto.id").containsExactly(tutkinnonOsaViiteDto1.getTutkinnonOsa().getIdLong());

        perusteService.attachTutkinnonOsa(peruste3.getId(), Suoritustapakoodi.REFORMI, tutkinnonOsaViiteDto1, mapper.map(peruste1, PerusteKevytDto.class));
        List<TutkinnonOsaViiteDto> peruste3viitteet = perusteService.getTutkinnonOsat(peruste2.getId(), Suoritustapakoodi.REFORMI);
        assertThat(peruste3viitteet).hasSize(1);
        assertThat(peruste3viitteet).extracting("tutkinnonOsaDto.alkuperainenPeruste.id").containsExactly(peruste1.getId());

    }

    @Test
    @Rollback
    public void testTemporaryTutkintonimike() {

        perusteService.updateTutkintonimikkeet(peruste.getId(), Arrays.asList(
                TutkintonimikeKoodiDto.builder()
                        .tutkintonimikeUri("temporary_tutkintonimikkeet_1111-1111")
                        .nimi(LokalisoituTekstiDto.of("tutkintonimike1")).build(),
                TutkintonimikeKoodiDto.builder()
                        .tutkintonimikeUri("temporary_tutkintonimikkeet_2222-2222")
                        .nimi(LokalisoituTekstiDto.of("tutkintonimike2")).build()
        ));

        RakenneModuuliDto rakenneDto = getRakenneDto();
        rakenneDto.setOsat(Arrays.asList(
                RakenneModuuliDto.builder()
                        .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                        .tutkintonimike(KoodiDto.builder().uri("temporary_tutkintonimikkeet_1111-1111").build())
                        .build(),
                RakenneModuuliDto.builder()
                        .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                        .tutkintonimike(KoodiDto.builder().uri("temporary_tutkintonimikkeet_2222-2222").build())
                        .build()));
        RakenneModuuliDto rakenne = update(rakenneDto);

        assertThat(((RakenneModuuliDto) rakenne.getOsat().get(0)).getNimi().getTekstit()).isEqualTo(LokalisoituTekstiDto.of("tutkintonimike1").getTekstit());
        assertThat(((RakenneModuuliDto) rakenne.getOsat().get(0)).getTutkintonimike().getUri()).isEqualTo("temporary_tutkintonimikkeet_1111-1111");
        assertThat(((RakenneModuuliDto) rakenne.getOsat().get(1)).getNimi().getTekstit()).isEqualTo(LokalisoituTekstiDto.of("tutkintonimike2").getTekstit());
        assertThat(((RakenneModuuliDto) rakenne.getOsat().get(1)).getTutkintonimike().getUri()).isEqualTo("temporary_tutkintonimikkeet_2222-2222");

    }

    @Test
    @Rollback
    public void testTemporaryTutkintonimike_invalid_koodisto() {
        startNewTransaction();
        setup();
        startNewTransaction();

        perusteService.updateTutkintonimikkeet(peruste.getId(), Arrays.asList(
                TutkintonimikeKoodiDto.builder()
                        .tutkintonimikeUri("temporary_tutkintonimikkeetxxx_1111-1111")
                        .nimi(LokalisoituTekstiDto.of("tutkintonimike1")).build()
        ));

        RakenneModuuliDto rakenneDto = getRakenneDto();
        rakenneDto.setOsat(Arrays.asList(
                RakenneModuuliDto.builder()
                        .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                        .tutkintonimike(KoodiDto.builder().uri("temporary_tutkintonimikkeetxxx_1111-1111").build())
                        .build())
        );

        assertThatThrownBy(() -> {
            startNewTransaction();
            update(rakenneDto);
            endTransaction();
        }).hasRootCauseInstanceOf(ConstraintViolationException.class)
        .hasStackTraceContaining("koodilla-vaara-koodisto");
    }

    @Test
    @Rollback
    public void testTemporaryOsaamisala() {

        perusteService.updateOsaamisalat(peruste.getId(), Arrays.asList(
                KoodiDto.builder().uri("temporary_osaamisala_1111-1111").nimi(LokalisoituTekstiDto.of("osaamisala1")).build(),
                KoodiDto.builder().uri("temporary_osaamisala_2222-2222").nimi(LokalisoituTekstiDto.of("osaamisala2")).build()
        ).stream().collect(Collectors.toSet()));

        RakenneModuuliDto rakenneDto = getRakenneDto();
        rakenneDto.setOsat(Arrays.asList(
                RakenneModuuliDto.builder()
                        .rooli(RakenneModuuliRooli.OSAAMISALA)
                        .osaamisala(OsaamisalaDto.builder().osaamisalakoodiUri("temporary_osaamisala_1111-1111").build())
                        .build(),
                RakenneModuuliDto.builder()
                        .rooli(RakenneModuuliRooli.OSAAMISALA)
                        .osaamisala(OsaamisalaDto.builder().osaamisalakoodiUri("temporary_osaamisala_2222-2222").build())
                        .build()));
        RakenneModuuliDto rakenne = update(rakenneDto);

        assertThat(((RakenneModuuliDto) rakenne.getOsat().get(0)).getNimi().getTekstit()).isEqualTo(LokalisoituTekstiDto.of("osaamisala1").getTekstit());
        assertThat(((RakenneModuuliDto) rakenne.getOsat().get(0)).getOsaamisala().getOsaamisalakoodiUri()).isEqualTo("temporary_osaamisala_1111-1111");
        assertThat(((RakenneModuuliDto) rakenne.getOsat().get(1)).getNimi().getTekstit()).isEqualTo(LokalisoituTekstiDto.of("osaamisala2").getTekstit());
        assertThat(((RakenneModuuliDto) rakenne.getOsat().get(1)).getOsaamisala().getOsaamisalakoodiUri()).isEqualTo("temporary_osaamisala_2222-2222");

    }

}
