package fi.vm.sade.eperusteet.service;

import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.digi.DigitaalinenOsaaminenTaso;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlueTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tuva.KoulutusOsanKoulutustyyppi;
import fi.vm.sade.eperusteet.domain.vst.TavoiteAlueTyyppi;
import fi.vm.sade.eperusteet.dto.digi.OsaamiskokonaisuusDto;
import fi.vm.sade.eperusteet.dto.digi.OsaamiskokonaisuusKasitteistoDto;
import fi.vm.sade.eperusteet.dto.digi.OsaamiskokonaisuusOsaAlueDto;
import fi.vm.sade.eperusteet.dto.digi.OsaamiskokonaisuusOsaAlueTasoKuvausDto;
import fi.vm.sade.eperusteet.dto.digi.OsaamiskokonaisuusPaaAlueDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKevytDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.Ammattitaitovaatimukset2019Dto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.AmmattitaitovaatimustenKohdealue2019Dto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueLaajaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.Osaamistavoite2020Dto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tuva.KoulutuksenOsaDto;
import fi.vm.sade.eperusteet.dto.tuva.TuvaLaajaAlainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.vst.KotoKielitaitotasoDto;
import fi.vm.sade.eperusteet.dto.vst.KotoLaajaAlainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.vst.KotoLaajaAlaisenOsaamisenAlueDto;
import fi.vm.sade.eperusteet.dto.vst.KotoOpintoDto;
import fi.vm.sade.eperusteet.dto.vst.KotoTaitotasoDto;
import fi.vm.sade.eperusteet.dto.vst.OpintokokonaisuusDto;
import fi.vm.sade.eperusteet.dto.vst.TavoiteAlueDto;
import fi.vm.sade.eperusteet.dto.vst.TavoitesisaltoalueDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import org.assertj.core.util.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@DirtiesContext
public class PerusteenOsaServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;

    @Autowired
    private TutkinnonOsaRepository tutkinnonOsaRepository;

    @PersistenceContext
    private EntityManager em;

    private ArviointiAsteikko arviointiasteikko;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    public PerusteprojektiTestUtils ppTestUtils;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private OsaAlueService osaAlueService;

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;

    @Autowired
    private PerusteenOsaViiteRepository perusteenOsaViiteRepository;

    @Before
    public void setUp() {
        TekstiPalanen osaamistasoOtsikko = TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "otsikko"));
        em.persist(osaamistasoOtsikko);

        Osaamistaso osaamistaso = new Osaamistaso();
        osaamistaso.setOtsikko(osaamistasoOtsikko);

        em.persist(osaamistaso);

        ArviointiAsteikko arviointiasteikko = new ArviointiAsteikko();
        arviointiasteikko.setOsaamistasot(Lists.newArrayList(osaamistaso));

        em.persist(arviointiasteikko);
        this.arviointiasteikko = arviointiasteikko;
        em.flush();
    }

    @Test
    @Rollback(true)
    public void testSaveWithArviointi() {
        TutkinnonOsa tutkinnonOsa = new TutkinnonOsa();
        tutkinnonOsa.setNimi(TestUtils.tekstiPalanenOf(Kieli.FI, "Nimi"));
        tutkinnonOsa.setArviointi(TestUtils.teeArviointi(arviointiasteikko));
        tutkinnonOsa = (TutkinnonOsa) perusteenOsaRepository.saveAndFlush(tutkinnonOsa);
        PerusteenOsaDto.Laaja dto = perusteenOsaService.get(tutkinnonOsa.getId());

        Assert.assertNotNull(dto);

        Assert.assertTrue(TutkinnonOsaDto.class.isInstance(dto));
        TutkinnonOsaDto to = (TutkinnonOsaDto) dto;
        to.getArviointi();
        Assert.assertNotNull(tutkinnonOsa.getArviointi());
    }

    @Test
    @Rollback(true)
    public void testFindTutkinnonOsaByName() {
        TutkinnonOsa tutkinnonOsa = new TutkinnonOsa();
        tutkinnonOsa.setNimi(TestUtils.tekstiPalanenOf(Kieli.FI, "Nimi"));
        tutkinnonOsa = tutkinnonOsaRepository.saveAndFlush(tutkinnonOsa);

        tutkinnonOsa = new TutkinnonOsa();
        tutkinnonOsa.setNimi(TestUtils.tekstiPalanenOf(Kieli.SV, "Namnet"));
        tutkinnonOsa = tutkinnonOsaRepository.saveAndFlush(tutkinnonOsa);

        List<TutkinnonOsa> tutkinnonOsat = tutkinnonOsaRepository.findByNimiTekstiTekstiContainingIgnoreCase("nim");

        Assert.assertNotNull(tutkinnonOsat);
        Assert.assertEquals(1, tutkinnonOsat.size());

        tutkinnonOsat = tutkinnonOsaRepository.findByNimiTekstiTekstiContainingIgnoreCase("nAm");

        Assert.assertNotNull(tutkinnonOsat);
        Assert.assertEquals(1, tutkinnonOsat.size());
    }

    @Test(expected = ConstraintViolationException.class)
    @Rollback(true)
    public void testWithInvalidHtml() {
        TekstiKappale tk = new TekstiKappale();
        tk.setNimi(TekstiPalanen.of(Kieli.FI, "<i>otsikko</i>"));
        perusteenOsaRepository.saveAndFlush(tk);
    }

    @Test
    public void testPerusteenOsaTilaNotUpdatable() {
        TekstiKappale tk = new TekstiKappale();
        tk.asetaTila(PerusteTila.LUONNOS);
        TekstiKappaleDto tkDto = new TekstiKappaleDto();
        tkDto.setTila(PerusteTila.VALMIS);
        tk = mapper.map(tkDto, tk);
        Assert.assertEquals(tk.getTila(), PerusteTila.LUONNOS);
    }

    @Test
    public void test_getTutkinnonOsaKaikkiDtoByKoodi() {
        {
            PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> {
            });
            PerusteDto perusteDto = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());
            luoKoodillinenTutkinnonOsa(perusteDto.getId(), Suoritustapakoodi.REFORMI, "tutkinnonosat_111");
            luoKoodillinenTutkinnonOsa(perusteDto.getId(), Suoritustapakoodi.REFORMI, "tutkinnonosat_222");
            ppTestUtils.julkaise(pp.getId(), true);
        }
        {
            PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> {
            });
            PerusteDto perusteDto = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());
            luoKoodillinenTutkinnonOsa(perusteDto.getId(), Suoritustapakoodi.REFORMI, "tutkinnonosat_111");
            ppTestUtils.julkaise(pp.getId(), true);
        }
        {
            PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> {
            });
            PerusteDto perusteDto = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());
            luoKoodillinenTutkinnonOsa(perusteDto.getId(), Suoritustapakoodi.REFORMI, "tutkinnonosat_111");
            luoKoodillinenTutkinnonOsa(perusteDto.getId(), Suoritustapakoodi.REFORMI, "tutkinnonosat_222");
            luoKoodillinenTutkinnonOsa(perusteDto.getId(), Suoritustapakoodi.REFORMI, "tutkinnonosat_333");
        }

        assertThat(perusteenOsaService.getTutkinnonOsaKaikkiDtoByKoodi("tutkinnonosat_111")).hasSize(2);
        assertThat(perusteenOsaService.getTutkinnonOsaKaikkiDtoByKoodi("tutkinnonosat_222")).hasSize(1);
        assertThat(perusteenOsaService.getTutkinnonOsaKaikkiDtoByKoodi("tutkinnonosat_333")).hasSize(0);
    }

    @Test
    public void test_tutkinnonOsanOsaalue_muokkauseiSallittu() {
        PerusteprojektiDto pp1 = ppTestUtils.createPerusteprojekti(ppl -> {
        });
        PerusteDto perusteDto1 = ppTestUtils.initPeruste(pp1.getPeruste().getIdLong());
        TutkinnonOsaViiteDto viiteDto = luoTutkinnonOsaOsaAlueella(perusteDto1.getId(), Suoritustapakoodi.REFORMI,  osaalue -> osaalue);
        viiteDto = perusteService.getTutkinnonOsaViite(perusteDto1.getId(),  Suoritustapakoodi.REFORMI, viiteDto.getId());

        TutkinnonOsaViiteDto copyViite = perusteService.getTutkinnonOsaViite(perusteDto1.getId(),  Suoritustapakoodi.REFORMI, viiteDto.getId());
        copyViite.setId(null);

        PerusteprojektiDto pp2 = ppTestUtils.createPerusteprojekti(ppl -> {});
        PerusteDto perusteDto2 = ppTestUtils.initPeruste(pp2.getPeruste().getIdLong());
        TutkinnonOsaViiteDto viiteDto2 = perusteService.attachTutkinnonOsa(perusteDto2.getId(), Suoritustapakoodi.REFORMI, copyViite, mapper.map(mapper.map(perusteDto2, Peruste.class), PerusteKevytDto.class));

        assertThatThrownBy(() -> perusteService.updateTutkinnonOsa(perusteDto2.getId(), Suoritustapakoodi.REFORMI, viiteDto2)).hasMessage("tutkinnon-osan-muokkaus-ei-sallittu");
        assertThatThrownBy(() -> osaAlueService.updateOsaAlue(perusteDto2.getId(), viiteDto2.getId(), viiteDto2.getTutkinnonOsaDto().getOsaAlueet().get(0).getId(), new OsaAlueLaajaDto())).hasMessage("osa-alueen-muokkaus-ei-sallittu");
    }

    @Test
    public void test_tutkinnonOsanOsaalue_lisaa() {
        TestTransaction.end();
        TestTransaction.start();
        TestTransaction.flagForCommit();

        PerusteprojektiDto pp1 = ppTestUtils.createPerusteprojekti(ppl -> {
        });
        PerusteDto perusteDto1 = ppTestUtils.initPeruste(pp1.getPeruste().getIdLong());
        TutkinnonOsaViiteDto viiteDto = luoTutkinnonOsaOsaAlueella(perusteDto1.getId(), Suoritustapakoodi.REFORMI, osaalue -> {
            osaalue.setPakollisetOsaamistavoitteet(Osaamistavoite2020Dto.builder()
                            .tavoitteet(Ammattitaitovaatimukset2019Dto.builder()
                                    .kohde(LokalisoituTekstiDto.of("kohde"))
                                    .kohdealueet(List.of(AmmattitaitovaatimustenKohdealue2019Dto.builder()
                                                    .kuvaus(LokalisoituTekstiDto.of("kuvaus"))
                                            .build()))
                                    .vaatimukset(List.of())
                                    .build())
                    .build());
            return osaalue;
        });
        TestTransaction.end();
        TestTransaction.start();

        viiteDto = perusteService.getTutkinnonOsaViite(perusteDto1.getId(), Suoritustapakoodi.REFORMI, viiteDto.getId());
        OsaAlueLaajaDto osaAlueLaajaDto = osaAlueService.getOsaAlue(viiteDto.getId(), viiteDto.getTutkinnonOsaDto().getOsaAlueet().get(0).getId());
        assertThat(osaAlueLaajaDto.getPakollisetOsaamistavoitteet()).isNotNull();
        assertThat(osaAlueLaajaDto.getPakollisetOsaamistavoitteet().getTavoitteet()).isNotNull();
        assertThat(osaAlueLaajaDto.getPakollisetOsaamistavoitteet().getTavoitteet().getKohde().getTekstit()).isEqualTo(LokalisoituTekstiDto.of("kohde").getTekstit());
        assertThat(osaAlueLaajaDto.getPakollisetOsaamistavoitteet().getTavoitteet().getKohdealueet()).hasSize(1);
        assertThat(osaAlueLaajaDto.getPakollisetOsaamistavoitteet().getTavoitteet().getKohdealueet().get(0).getKuvaus().getTekstit()).isEqualTo(LokalisoituTekstiDto.of("kuvaus").getTekstit());
        TestTransaction.end();

        TestTransaction.start();
        TestTransaction.flagForCommit();
        osaAlueLaajaDto.getPakollisetOsaamistavoitteet().getTavoitteet().getKohde().setTekstit(LokalisoituTekstiDto.of("kohde2").getTekstit());
        osaAlueService.lockOsaAlue(viiteDto.getId(), osaAlueLaajaDto.getId());
        osaAlueService.updateOsaAlue(perusteDto1.getId(), viiteDto.getId(), osaAlueLaajaDto.getId(), osaAlueLaajaDto);
        TestTransaction.end();

        TestTransaction.start();
        osaAlueLaajaDto = osaAlueService.getOsaAlue(viiteDto.getId(), viiteDto.getTutkinnonOsaDto().getOsaAlueet().get(0).getId());
        assertThat(osaAlueLaajaDto.getPakollisetOsaamistavoitteet().getTavoitteet().getKohde().getTekstit()).isEqualTo(LokalisoituTekstiDto.of("kohde2").getTekstit());
        TestTransaction.end();
    }

    @Test
    public void testOpintokokonaisuus() {
        PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> {
            ppl.setKoulutustyyppi(KoulutusTyyppi.VAPAASIVISTYSTYO.toString());
        });
        PerusteDto perusteDto = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());

        PerusteenOsaViiteDto.Matala viiteDto = new PerusteenOsaViiteDto.Matala(new OpintokokonaisuusDto());

        PerusteenOsaViiteDto.Matala perusteViite = perusteService.addSisaltoUUSI(perusteDto.getId(), null, viiteDto);
        OpintokokonaisuusDto opintokokonaisuusDto = (OpintokokonaisuusDto) perusteenOsaService.get(perusteViite.getPerusteenOsa().getId());
        perusteenOsaService.lock(opintokokonaisuusDto.getId());

        assertThat(opintokokonaisuusDto.getId()).isNotNull();

        opintokokonaisuusDto.setKuvaus(LokalisoituTekstiDto.of("kuvaus"));
        opintokokonaisuusDto.setMinimilaajuus(1);
        opintokokonaisuusDto.setNimiKoodi(KoodiDto.of("opintokokonaisuusnimikoodi", "arvi1"));
        opintokokonaisuusDto.setNimi(LokalisoituTekstiDto.of("nimi"));
        opintokokonaisuusDto.setOpetuksenTavoiteOtsikko(LokalisoituTekstiDto.of("opetuksentavoiteotsikko"));
        opintokokonaisuusDto.setOpetuksenTavoitteet(Arrays.asList(
                KoodiDto.builder().nimi(LokalisoituTekstiDto.of("nimi1")).uri("temporary_opintokokonaisuustavoitteet_1111").build(),
                KoodiDto.builder().nimi(LokalisoituTekstiDto.of("nimi2")).uri("temporary_opintokokonaisuustavoitteet_2222").build()));
        opintokokonaisuusDto.setArvioinnit(Arrays.asList(LokalisoituTekstiDto.of("arviointi1"), LokalisoituTekstiDto.of("arviointi2")));

        opintokokonaisuusDto = perusteenOsaService.update(opintokokonaisuusDto);

        assertThat(opintokokonaisuusDto.getNimi().get(Kieli.FI)).isEqualTo("nimi");
        assertThat(opintokokonaisuusDto.getKuvaus().get(Kieli.FI)).isEqualTo("kuvaus");
        assertThat(opintokokonaisuusDto.getMinimilaajuus()).isEqualTo(1);
        assertThat(opintokokonaisuusDto.getNimiKoodi()).isEqualTo(KoodiDto.of("opintokokonaisuusnimikoodi", "arvi1"));
        assertThat(opintokokonaisuusDto.getOpetuksenTavoitteet()).hasSize(2);
        assertThat(opintokokonaisuusDto.getOpetuksenTavoitteet()).extracting("koodisto")
                .containsExactlyInAnyOrder("opintokokonaisuustavoitteet", "opintokokonaisuustavoitteet");
        assertThat(opintokokonaisuusDto.getOpetuksenTavoitteet()).extracting("uri")
                .containsExactlyInAnyOrder("temporary_opintokokonaisuustavoitteet_1111", "temporary_opintokokonaisuustavoitteet_2222");
        assertThat(opintokokonaisuusDto.getArvioinnit()).hasSize(2);
        assertThat(opintokokonaisuusDto.getArvioinnit()).extracting("tekstit").containsExactlyInAnyOrder(Maps.newHashMap(Kieli.FI, "arviointi1"), Maps.newHashMap(Kieli.FI, "arviointi2"));
    }

    @Test
    public void testKoulutuksenosa() {
        PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> {
            ppl.setKoulutustyyppi(KoulutusTyyppi.TUTKINTOONVALMENTAVA.toString());
        });
        PerusteDto perusteDto = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());

        PerusteenOsaViiteDto.Matala viiteDto = new PerusteenOsaViiteDto.Matala(new KoulutuksenOsaDto());

        PerusteenOsaViiteDto.Matala perusteViite = perusteService.addSisaltoUUSI(perusteDto.getId(), null, viiteDto);
        KoulutuksenOsaDto koulutuksenOsaDto = (KoulutuksenOsaDto) perusteenOsaService.get(perusteViite.getPerusteenOsa().getId());
        perusteenOsaService.lock(koulutuksenOsaDto.getId());

        assertThat(koulutuksenOsaDto.getId()).isNotNull();

        koulutuksenOsaDto.setKoulutusOsanKoulutustyyppi(KoulutusOsanKoulutustyyppi.AMMATILLINENKOULUTUS);
        koulutuksenOsaDto.setKuvaus(LokalisoituTekstiDto.of("kuvaus"));
        koulutuksenOsaDto.setLaajuusMinimi(1);
        koulutuksenOsaDto.setLaajuusMaksimi(2);
        koulutuksenOsaDto.setNimiKoodi(KoodiDto.of("opintokokonaisuusnimikoodi", "arvi1"));
        koulutuksenOsaDto.setNimi(LokalisoituTekstiDto.of("nimi"));
        koulutuksenOsaDto.setKeskeinenSisalto(LokalisoituTekstiDto.of("keskeinensisalto"));
        koulutuksenOsaDto.setTavoitteet(Arrays.asList(LokalisoituTekstiDto.of("tavoite1"), LokalisoituTekstiDto.of("tavoite2")));
        koulutuksenOsaDto.setLaajaAlaisenOsaamisenKuvaus(LokalisoituTekstiDto.of("laajaalainenosaaminen"));
        koulutuksenOsaDto.setArvioinninKuvaus(LokalisoituTekstiDto.of("arvioinninkuvaus"));

        koulutuksenOsaDto = perusteenOsaService.update(koulutuksenOsaDto);

        assertThat(koulutuksenOsaDto.getKoulutusOsanKoulutustyyppi()).isEqualTo(KoulutusOsanKoulutustyyppi.AMMATILLINENKOULUTUS);
        assertThat(koulutuksenOsaDto.getNimi().get(Kieli.FI)).isEqualTo("nimi");
        assertThat(koulutuksenOsaDto.getNimiKoodi()).isEqualTo(KoodiDto.of("opintokokonaisuusnimikoodi", "arvi1"));
        assertThat(koulutuksenOsaDto.getLaajuusMinimi()).isEqualTo(1);
        assertThat(koulutuksenOsaDto.getLaajuusMaksimi()).isEqualTo(2);
        assertThat(koulutuksenOsaDto.getKuvaus().get(Kieli.FI)).isEqualTo("kuvaus");
        assertThat(koulutuksenOsaDto.getKeskeinenSisalto().get(Kieli.FI)).isEqualTo("keskeinensisalto");
        assertThat(koulutuksenOsaDto.getLaajaAlaisenOsaamisenKuvaus().get(Kieli.FI)).isEqualTo("laajaalainenosaaminen");
        assertThat(koulutuksenOsaDto.getArvioinninKuvaus().get(Kieli.FI)).isEqualTo("arvioinninkuvaus");
        assertThat(koulutuksenOsaDto.getTavoitteet()).hasSize(2);
        assertThat(koulutuksenOsaDto.getTavoitteet()).extracting("tekstit").containsExactly(Maps.newHashMap(Kieli.FI, "tavoite1"), Maps.newHashMap(Kieli.FI, "tavoite2"));
    }

    @Test
    public void testTuvaLaajaAlainenOsaaminen() {
        PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> {
            ppl.setKoulutustyyppi(KoulutusTyyppi.TUTKINTOONVALMENTAVA.toString());
        });
        PerusteDto perusteDto = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());

        PerusteenOsaViiteDto.Matala viiteDto = new PerusteenOsaViiteDto.Matala(new TuvaLaajaAlainenOsaaminenDto());

        PerusteenOsaViiteDto.Matala perusteViite = perusteService.addSisaltoUUSI(perusteDto.getId(), null, viiteDto);
        TuvaLaajaAlainenOsaaminenDto tuvaLaajaAlainenOsaaminenDto = (TuvaLaajaAlainenOsaaminenDto) perusteenOsaService.get(perusteViite.getPerusteenOsa().getId());
        perusteenOsaService.lock(tuvaLaajaAlainenOsaaminenDto.getId());

        assertThat(tuvaLaajaAlainenOsaaminenDto.getId()).isNotNull();

        tuvaLaajaAlainenOsaaminenDto.setTeksti(LokalisoituTekstiDto.of("teksti"));
        tuvaLaajaAlainenOsaaminenDto.setNimiKoodi(KoodiDto.of(KoodistoUriArvo.TUTKINTOKOULUTUKSEEN_VALMENTAVAKOULUTUS_LAAJAALAINENOSAAMINEN, "arvi1"));
        tuvaLaajaAlainenOsaaminenDto.setLiite(true);

        tuvaLaajaAlainenOsaaminenDto = perusteenOsaService.update(tuvaLaajaAlainenOsaaminenDto);

        assertThat(tuvaLaajaAlainenOsaaminenDto.getNimiKoodi()).isEqualTo(KoodiDto.of(KoodistoUriArvo.TUTKINTOKOULUTUKSEEN_VALMENTAVAKOULUTUS_LAAJAALAINENOSAAMINEN, "arvi1"));
        assertThat(tuvaLaajaAlainenOsaaminenDto.getTeksti().get(Kieli.FI)).isEqualTo("teksti");
        assertThat(tuvaLaajaAlainenOsaaminenDto.getLiite()).isTrue();
    }

    @Test
    public void testVstLukutaito_Tavoitesisaltoalue() {
        PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> {
            ppl.setKoulutustyyppi(KoulutusTyyppi.VAPAASIVISTYSTYOLUKUTAITO.toString());
        });
        TavoitesisaltoalueDto tavoitesisaltoalueDto = createPerusteWithTavoitesisaltoalue(pp);

        tavoitesisaltoalueDto = perusteenOsaService.update(tavoitesisaltoalueDto);

        assertTavoitesisaltoalueData(tavoitesisaltoalueDto);
    }

    @Test
    public void testKotoKielitaitotaso() {
        PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> ppl.setKoulutustyyppi(KoulutusTyyppi.MAAHANMUUTTAJIENKOTOUTUMISKOULUTUS.toString()));

        PerusteDto perusteDto = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());

        PerusteenOsaViiteDto.Matala perusteViite = perusteService.addSisaltoUUSI(perusteDto.getId(), null, new PerusteenOsaViiteDto.Matala(new KotoKielitaitotasoDto()));
        KotoKielitaitotasoDto kotoKielitaitotaso = (KotoKielitaitotasoDto) perusteenOsaService.get(perusteViite.getPerusteenOsa().getId());
        perusteenOsaService.lock(kotoKielitaitotaso.getId());

        assertThat(kotoKielitaitotaso.getId()).isNotNull();

        kotoKielitaitotaso.setKuvaus(LokalisoituTekstiDto.of("teksti2"));
        kotoKielitaitotaso.setNimiKoodi(KoodiDto.of(KoodistoUriArvo.TAVOITESISALTOALUEENOTSIKKO, "nimi2"));
        kotoKielitaitotaso.setNimi(LokalisoituTekstiDto.of("nimi2"));
        kotoKielitaitotaso.setTaitotasot(Collections.singletonList(
                KotoTaitotasoDto.builder()
                        .nimi(KoodiDto.of(KoodistoUriArvo.KOTOUTUMISKOULUTUSTAVOITTEET, "taitotasonimi2"))
                        .aihealueet(LokalisoituTekstiDto.of("aihealueet2"))
                        .kielenkayttotarkoitus(LokalisoituTekstiDto.of("kielenkayttotarkoitus2"))
                        .opiskelijantaidot(LokalisoituTekstiDto.of("opiskelijantaidot2"))
                        .tavoitteet(LokalisoituTekstiDto.of("tavoitteet2"))
                        .viestintataidot(LokalisoituTekstiDto.of("viestintataidot2"))
                        .suullinenVastaanottaminen(LokalisoituTekstiDto.of("testiSuullinenVastaanottaminen"))
                        .suullinenTuottaminen(LokalisoituTekstiDto.of("testiSuullinenTuottaminen"))
                        .vuorovaikutusJaMediaatio(LokalisoituTekstiDto.of("testiVuorovaikutusJaMediaatio"))
                        .build()
        ));
        kotoKielitaitotaso = perusteenOsaService.update(kotoKielitaitotaso);

        assertThat(kotoKielitaitotaso.getNimi().get(Kieli.FI)).isEqualTo("nimi2");
        assertThat(kotoKielitaitotaso.getKuvaus().get(Kieli.FI)).isEqualTo("teksti2");
        assertThat(kotoKielitaitotaso.getNimiKoodi()).isEqualTo(KoodiDto.of(KoodistoUriArvo.TAVOITESISALTOALUEENOTSIKKO, "nimi2"));
        assertThat(kotoKielitaitotaso.getTaitotasot()).hasSize(1);
        assertThat(kotoKielitaitotaso.getTaitotasot().get(0)).extracting("nimi").isEqualTo(KoodiDto.of(KoodistoUriArvo.KOTOUTUMISKOULUTUSTAVOITTEET, "taitotasonimi2"));
        assertThat(kotoKielitaitotaso.getTaitotasot())
                .flatExtracting("aihealueet", "kielenkayttotarkoitus", "opiskelijantaidot", "tavoitteet", "viestintataidot", "suullinenVastaanottaminen", "suullinenTuottaminen", "vuorovaikutusJaMediaatio")
                .extracting("tekstit")
                .containsExactlyInAnyOrder(
                        Maps.newHashMap(Kieli.FI, "aihealueet2"),
                        Maps.newHashMap(Kieli.FI, "kielenkayttotarkoitus2"),
                        Maps.newHashMap(Kieli.FI, "opiskelijantaidot2"),
                        Maps.newHashMap(Kieli.FI, "tavoitteet2"),
                        Maps.newHashMap(Kieli.FI, "viestintataidot2"),
                        Maps.newHashMap(Kieli.FI, "testiSuullinenVastaanottaminen"),
                        Maps.newHashMap(Kieli.FI, "testiSuullinenTuottaminen"),
                        Maps.newHashMap(Kieli.FI, "testiVuorovaikutusJaMediaatio"));
    }

    @Test
    public void testKotoOpinto() {
        PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> ppl.setKoulutustyyppi(KoulutusTyyppi.MAAHANMUUTTAJIENKOTOUTUMISKOULUTUS.toString()));

        PerusteDto perusteDto = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());

        PerusteenOsaViiteDto.Matala perusteViite = perusteService.addSisaltoUUSI(perusteDto.getId(), null, new PerusteenOsaViiteDto.Matala(new KotoOpintoDto()));
        KotoOpintoDto kotoOpinto = (KotoOpintoDto) perusteenOsaService.get(perusteViite.getPerusteenOsa().getId());
        perusteenOsaService.lock(kotoOpinto.getId());

        assertThat(kotoOpinto.getId()).isNotNull();

        kotoOpinto.setKuvaus(LokalisoituTekstiDto.of("teksti1"));
        kotoOpinto.setNimiKoodi(KoodiDto.of(KoodistoUriArvo.TAVOITESISALTOALUEENOTSIKKO, "nimi1"));
        kotoOpinto.setNimi(LokalisoituTekstiDto.of("nimi1"));
        kotoOpinto.setTaitotasot(Collections.singletonList(
                KotoTaitotasoDto.builder()
                        .nimi(KoodiDto.of(KoodistoUriArvo.KOTOUTUMISKOULUTUSTAVOITTEET, "taitotasonimi1"))
                        .aihealueet(LokalisoituTekstiDto.of("aihealueet1"))
                        .opiskelijantaidot(LokalisoituTekstiDto.of("opiskelijantaidot1"))
                        .tavoitteet(LokalisoituTekstiDto.of("tavoitteet1"))
                        .opiskelijanTyoelamataidot(LokalisoituTekstiDto.of("testiTyoelamataidot"))
                        .tyoelamaOpintoMinimiLaajuus(2)
                        .tyoelamaOpintoMaksimiLaajuus(4)
                        .build()
        ));
        kotoOpinto = perusteenOsaService.update(kotoOpinto);

        assertThat(kotoOpinto.getNimi().get(Kieli.FI)).isEqualTo("nimi1");
        assertThat(kotoOpinto.getKuvaus().get(Kieli.FI)).isEqualTo("teksti1");
        assertThat(kotoOpinto.getNimiKoodi()).isEqualTo(KoodiDto.of(KoodistoUriArvo.TAVOITESISALTOALUEENOTSIKKO, "nimi1"));
        assertThat(kotoOpinto.getTaitotasot()).hasSize(1);
        assertThat(kotoOpinto.getTaitotasot().get(0)).extracting("nimi").isEqualTo(KoodiDto.of(KoodistoUriArvo.KOTOUTUMISKOULUTUSTAVOITTEET, "taitotasonimi1"));
        assertThat(kotoOpinto.getTaitotasot().get(0)).extracting("tyoelamaOpintoMinimiLaajuus").isEqualTo(2);
        assertThat(kotoOpinto.getTaitotasot().get(0)).extracting("tyoelamaOpintoMaksimiLaajuus").isEqualTo(4);
        assertThat(kotoOpinto.getTaitotasot())
                .flatExtracting("aihealueet", "opiskelijantaidot", "tavoitteet", "opiskelijanTyoelamataidot")
                .extracting("tekstit")
                .containsExactlyInAnyOrder(
                        Maps.newHashMap(Kieli.FI, "aihealueet1"),
                        Maps.newHashMap(Kieli.FI, "opiskelijantaidot1"),
                        Maps.newHashMap(Kieli.FI, "tavoitteet1"),
                        Maps.newHashMap(Kieli.FI, "testiTyoelamataidot"));
    }

    @Test
    public void testKotoLaajaAlainenOsaaminen() {
        PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> ppl.setKoulutustyyppi(KoulutusTyyppi.MAAHANMUUTTAJIENKOTOUTUMISKOULUTUS.toString()));

        PerusteDto perusteDto = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());

        PerusteenOsaViiteDto.Matala perusteViite = perusteService.addSisaltoUUSI(
                perusteDto.getId(),
                null,
                new PerusteenOsaViiteDto.Matala(new KotoLaajaAlainenOsaaminenDto()));

        KotoLaajaAlainenOsaaminenDto koto = (KotoLaajaAlainenOsaaminenDto) perusteenOsaService.get(perusteViite.getPerusteenOsa().getId());
        perusteenOsaService.lock(koto.getId());

        assertThat(koto.getId()).isNotNull();

        koto.setYleiskuvaus(LokalisoituTekstiDto.of("joku yleiskuvaus"));
        koto.setNimi(LokalisoituTekstiDto.of("Laaja-alainen osaaminen kotoutumiskoulutuksekssa"));
        koto.setOsaamisAlueet(Collections.singletonList(
                KotoLaajaAlaisenOsaamisenAlueDto
                        .builder()
                        .koodi(KoodiDto.of(KoodistoUriArvo.LAAJAALAINENOSAAMINENKOTO2022, "Digiosaaminen"))
                        .kuvaus(LokalisoituTekstiDto.of("joku osaamisalue"))
                        .build()));

        KotoLaajaAlainenOsaaminenDto updatedKoto = perusteenOsaService.update(koto);

        assertThat(updatedKoto.getNimi().get(Kieli.FI)).isEqualTo("Laaja-alainen osaaminen kotoutumiskoulutuksekssa");
        assertThat(updatedKoto.getYleiskuvaus().get(Kieli.FI)).isEqualTo("joku yleiskuvaus");
        assertThat(updatedKoto.getOsaamisAlueet().get(0).getKoodi()).isEqualTo(KoodiDto.of(KoodistoUriArvo.LAAJAALAINENOSAAMINENKOTO2022, "Digiosaaminen"));
        Map<Kieli, String> tekstit = updatedKoto.getOsaamisAlueet().get(0).getKuvaus().getTekstit();
        assertThat(tekstit).isEqualTo(Maps.newHashMap(Kieli.FI, "joku osaamisalue"));
    }

    @Test
    public void testOsaamiskokonaisuus() {
        PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> {
            ppl.setKoulutustyyppi(null);
            ppl.setTyyppi(PerusteTyyppi.DIGITAALINEN_OSAAMINEN);
        });

        PerusteDto perusteDto = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());

        PerusteenOsaViiteDto.Matala perusteViite = perusteService.addSisaltoUUSI(
                perusteDto.getId(),
                null,
                new PerusteenOsaViiteDto.Matala(new OsaamiskokonaisuusDto()));

        OsaamiskokonaisuusDto osaamiskokonaisuus = (OsaamiskokonaisuusDto) perusteenOsaService.get(perusteViite.getPerusteenOsa().getId());
        perusteenOsaService.lock(osaamiskokonaisuus.getId());
        assertThat(osaamiskokonaisuus.getId()).isNotNull();
        assertThat(osaamiskokonaisuus.getKasitteistot()).hasSize(5);
        assertThat(osaamiskokonaisuus.getKasitteistot()).extracting("taso")
                .containsExactly(DigitaalinenOsaaminenTaso.VARHAISKASVATUS,
                        DigitaalinenOsaaminenTaso.ESIOPETUS,
                        DigitaalinenOsaaminenTaso.VUOSILUOKKA_12,
                        DigitaalinenOsaaminenTaso.VUOSILUOKKA_3456,
                        DigitaalinenOsaaminenTaso.VUOSILUOKKA_789);

        osaamiskokonaisuus.setNimi(LokalisoituTekstiDto.of("digiosaaminen"));
        osaamiskokonaisuus.setKuvaus(LokalisoituTekstiDto.of("kuvaus"));
        osaamiskokonaisuus.setKeskeinenKasitteisto(LokalisoituTekstiDto.of("keskeinensisalto"));
        osaamiskokonaisuus.getKasitteistot().stream()
                .filter(kasitteisto -> kasitteisto.getTaso().equals(DigitaalinenOsaaminenTaso.ESIOPETUS))
                .forEach(kasitteisto -> {
                    kasitteisto.setKuvaus(LokalisoituTekstiDto.of("kuvaus"));
                });

        OsaamiskokonaisuusDto updatetOsamiskokonaisuusDto = perusteenOsaService.update(osaamiskokonaisuus);

        assertThat(updatetOsamiskokonaisuusDto.getNimi().get(Kieli.FI)).isEqualTo("digiosaaminen");
        assertThat(updatetOsamiskokonaisuusDto.getKuvaus().get(Kieli.FI)).isEqualTo("kuvaus");
        assertThat(updatetOsamiskokonaisuusDto.getKeskeinenKasitteisto().get(Kieli.FI)).isEqualTo("keskeinensisalto");
        OsaamiskokonaisuusKasitteistoDto kasitteisto = updatetOsamiskokonaisuusDto.getKasitteistot().stream()
                .filter(k -> k.getTaso().equals(DigitaalinenOsaaminenTaso.ESIOPETUS))
                .findFirst().get();
        assertThat(kasitteisto.getKuvaus().get(Kieli.FI)).isEqualTo("kuvaus");

        PerusteenOsaViiteDto.Matala osaamiskokonaisuusPaaAlueViite = perusteService.addSisaltoLapsi(
                perusteDto.getId(),
                perusteViite.getId(),
                new PerusteenOsaViiteDto.Matala(new OsaamiskokonaisuusPaaAlueDto()));

        OsaamiskokonaisuusPaaAlueDto osaamiskokonaisuusPaaAlueDto = (OsaamiskokonaisuusPaaAlueDto) perusteenOsaService.get(osaamiskokonaisuusPaaAlueViite.getPerusteenOsa().getId());
        perusteenOsaService.lock(osaamiskokonaisuusPaaAlueDto.getId());

        osaamiskokonaisuusPaaAlueDto.setKuvaus(LokalisoituTekstiDto.of("paaaluekuvaus"));
        osaamiskokonaisuusPaaAlueDto.setNimi(LokalisoituTekstiDto.of("paaaluenimi"));
        osaamiskokonaisuusPaaAlueDto.setOsaAlueet(new ArrayList<>(Arrays.asList(
                OsaamiskokonaisuusOsaAlueDto.builder()
                        .nimi(LokalisoituTekstiDto.of("osaaluenimi"))
                        .tasokuvaukset(new ArrayList<>(Arrays.asList(
                                OsaamiskokonaisuusOsaAlueTasoKuvausDto.builder()
                                        .taso(DigitaalinenOsaaminenTaso.VARHAISKASVATUS)
                                        .edelleenKehittyvatOsaamiset(Arrays.asList(LokalisoituTekstiDto.of("edelleenkeh")))
                                        .osaamiset(Arrays.asList(LokalisoituTekstiDto.of("tasokuvausV")))
                                        .edistynytOsaaminenKuvaukset(Arrays.asList(LokalisoituTekstiDto.of("edistynytkuvausV")))
                                        .build(),
                                OsaamiskokonaisuusOsaAlueTasoKuvausDto.builder()
                                        .taso(DigitaalinenOsaaminenTaso.ESIOPETUS)
                                        .osaamiset(Arrays.asList(LokalisoituTekstiDto.of("tasokuvausE")))
                                        .edistynytOsaaminenKuvaukset(Arrays.asList(LokalisoituTekstiDto.of("edistynytkuvausE")))
                                        .build())))
                        .build(),
                OsaamiskokonaisuusOsaAlueDto.builder()
                        .nimi(LokalisoituTekstiDto.of("osaaluenimi"))
                        .tasokuvaukset(new ArrayList<>(Arrays.asList(
                                OsaamiskokonaisuusOsaAlueTasoKuvausDto.builder()
                                        .taso(DigitaalinenOsaaminenTaso.ESIOPETUS)
                                        .osaamiset(Arrays.asList(LokalisoituTekstiDto.of("tasokuvaus")))
                                        .edistynytOsaaminenKuvaukset(Arrays.asList(LokalisoituTekstiDto.of("edistynytkuvaus")))
                                        .build())))
                        .build()
        )));

        perusteenOsaService.update(osaamiskokonaisuusPaaAlueDto);

        PerusteenOsaViiteDto.Matala osaamiskokonaisuusViite = perusteenOsaViiteService.getSisalto(perusteDto.getId(), perusteViite.getId(), PerusteenOsaViiteDto.Matala.class);
        assertThat(osaamiskokonaisuusViite.getLapset()).hasSize(1);

        osaamiskokonaisuusPaaAlueDto = (OsaamiskokonaisuusPaaAlueDto) perusteenOsaService.getByViite(osaamiskokonaisuusViite.getLapset().get(0).getIdLong());
        assertThat(osaamiskokonaisuusPaaAlueDto.getKuvaus().get(Kieli.FI)).isEqualTo("paaaluekuvaus");
        assertThat(osaamiskokonaisuusPaaAlueDto.getNimi().get(Kieli.FI)).isEqualTo("paaaluenimi");
        assertThat(osaamiskokonaisuusPaaAlueDto.getOsaAlueet()).hasSize(2);
        assertThat(osaamiskokonaisuusPaaAlueDto.getOsaAlueet().get(0).getNimi().get(Kieli.FI)).isEqualTo("osaaluenimi");
        assertThat(osaamiskokonaisuusPaaAlueDto.getOsaAlueet().get(0).getTasokuvaukset()).hasSize(2);
        assertThat(osaamiskokonaisuusPaaAlueDto.getOsaAlueet().get(0).getTasokuvaukset().get(0).getTaso()).isEqualTo(DigitaalinenOsaaminenTaso.VARHAISKASVATUS);
        assertThat(osaamiskokonaisuusPaaAlueDto.getOsaAlueet().get(0).getTasokuvaukset().get(0).getEdelleenKehittyvatOsaamiset().get(0).get(Kieli.FI)).isEqualTo("edelleenkeh");
        assertThat(osaamiskokonaisuusPaaAlueDto.getOsaAlueet().get(0).getTasokuvaukset().get(0).getOsaamiset().get(0).get(Kieli.FI)).isEqualTo("tasokuvausV");
        assertThat(osaamiskokonaisuusPaaAlueDto.getOsaAlueet().get(0).getTasokuvaukset().get(0).getEdistynytOsaaminenKuvaukset().get(0).get(Kieli.FI)).isEqualTo("edistynytkuvausV");
        assertThat(osaamiskokonaisuusPaaAlueDto.getOsaAlueet().get(0).getTasokuvaukset().get(1).getTaso()).isEqualTo(DigitaalinenOsaaminenTaso.ESIOPETUS);
        assertThat(osaamiskokonaisuusPaaAlueDto.getOsaAlueet().get(0).getTasokuvaukset().get(1).getOsaamiset().get(0).get(Kieli.FI)).isEqualTo("tasokuvausE");
        assertThat(osaamiskokonaisuusPaaAlueDto.getOsaAlueet().get(0).getTasokuvaukset().get(1).getEdistynytOsaaminenKuvaukset().get(0).get(Kieli.FI)).isEqualTo("edistynytkuvausE");
    }

    private void assertTavoitesisaltoalueData(TavoitesisaltoalueDto tavoitesisaltoalueDto) {
        assertThat(tavoitesisaltoalueDto.getNimi().get(Kieli.FI)).isEqualTo("nimi");
        assertThat(tavoitesisaltoalueDto.getTeksti().get(Kieli.FI)).isEqualTo("teksti1");
        assertThat(tavoitesisaltoalueDto.getNimiKoodi()).isEqualTo(KoodiDto.of(KoodistoUriArvo.TAVOITESISALTOALUEENOTSIKKO, "arvi1"));
        assertThat(tavoitesisaltoalueDto.getTavoitealueet()).hasSize(2);
        assertThat(tavoitesisaltoalueDto.getTavoitealueet()).extracting("tavoiteAlueTyyppi").containsExactly(TavoiteAlueTyyppi.OTSIKKO, TavoiteAlueTyyppi.TAVOITESISALTOALUE);
        assertThat(tavoitesisaltoalueDto.getTavoitealueet().get(0)).extracting("otsikko").isEqualTo(KoodiDto.of(KoodistoUriArvo.TAVOITEALUEET, "otsikko1"));
        assertThat(tavoitesisaltoalueDto.getTavoitealueet())
                .flatExtracting("tavoitteet").containsExactlyInAnyOrder(KoodiDto.of(KoodistoUriArvo.TAVOITTEETLUKUTAIDOT, "tavoite1"));
        assertThat(tavoitesisaltoalueDto.getTavoitealueet())
                .flatExtracting("keskeisetSisaltoalueet").extracting("tekstit").containsExactlyInAnyOrder(Maps.newHashMap(Kieli.FI, "keskeinen1"));
    }

    private TavoitesisaltoalueDto createPerusteWithTavoitesisaltoalue(PerusteprojektiDto pp) {
        PerusteDto perusteDto = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());

        PerusteenOsaViiteDto.Matala viiteDto = new PerusteenOsaViiteDto.Matala(new TavoitesisaltoalueDto());

        PerusteenOsaViiteDto.Matala perusteViite = perusteService.addSisaltoUUSI(perusteDto.getId(), null, viiteDto);
        TavoitesisaltoalueDto tavoitesisaltoalueDto = (TavoitesisaltoalueDto) perusteenOsaService.get(perusteViite.getPerusteenOsa().getId());
        perusteenOsaService.lock(tavoitesisaltoalueDto.getId());

        assertThat(tavoitesisaltoalueDto.getId()).isNotNull();

        tavoitesisaltoalueDto.setTeksti(LokalisoituTekstiDto.of("teksti1"));
        tavoitesisaltoalueDto.setNimiKoodi(KoodiDto.of(KoodistoUriArvo.TAVOITESISALTOALUEENOTSIKKO, "arvi1"));
        tavoitesisaltoalueDto.setNimi(LokalisoituTekstiDto.of("nimi"));
        tavoitesisaltoalueDto.setTavoitealueet(Arrays
                .asList(
                        TavoiteAlueDto.builder()
                                .tavoiteAlueTyyppi(TavoiteAlueTyyppi.OTSIKKO)
                                .otsikko(KoodiDto.of(KoodistoUriArvo.TAVOITEALUEET, "otsikko1"))
                                .build(),
                        TavoiteAlueDto.builder()
                                .tavoiteAlueTyyppi(TavoiteAlueTyyppi.TAVOITESISALTOALUE)
                                .tavoitteet(Arrays.asList(KoodiDto.of(KoodistoUriArvo.TAVOITTEETLUKUTAIDOT, "tavoite1")))
                                .keskeisetSisaltoalueet(Arrays.asList(LokalisoituTekstiDto.of("keskeinen1")))
                                .build()
                )
        );
        return tavoitesisaltoalueDto;
    }

    private KoodiDto koodiDto(String nimi) {
        KoodiDto koodiDto = new KoodiDto();
        koodiDto.setNimi(LokalisoituTekstiDto.of(Kieli.FI, nimi));

        return koodiDto;
    }

    private TutkinnonOsaViiteDto luoKoodillinenTutkinnonOsa(
            Long id,
            Suoritustapakoodi suoritustapakoodi,
            String koodiUri
    ) {
        TutkinnonOsaViiteDto dto = new TutkinnonOsaViiteDto(
                BigDecimal.ONE, 1, TestUtils.lt(TestUtils.uniikkiString()), TutkinnonOsaTyyppi.NORMAALI);
        TutkinnonOsaDto tosa = new TutkinnonOsaDto();
        tosa.setNimi(dto.getNimi());
        KoodiDto koodiDto = new KoodiDto();
        koodiDto.setUri(koodiUri);
        koodiDto.setKoodisto("tutkinnonosat");
        tosa.setKoodi(koodiDto);

        dto.setTutkinnonOsaDto(tosa);
        TutkinnonOsaViiteDto lisatty = perusteService.addTutkinnonOsa(id, suoritustapakoodi, dto);
        return lisatty;
    }

    private TutkinnonOsaViiteDto luoTutkinnonOsaOsaAlueella(
            Long id,
            Suoritustapakoodi suoritustapakoodi,
            Function<OsaAlueLaajaDto, OsaAlueLaajaDto> osaAlueDatas
    ) {
        TutkinnonOsaViiteDto dto = new TutkinnonOsaViiteDto(
                BigDecimal.ONE, 1, TestUtils.lt(TestUtils.uniikkiString()), TutkinnonOsaTyyppi.REFORMI_TUTKE2);
        TutkinnonOsaDto tosa = new TutkinnonOsaDto();
        tosa.setNimi(dto.getNimi());
        tosa.setOsaAlueet(new ArrayList<>());
        tosa.setTyyppi(TutkinnonOsaTyyppi.REFORMI_TUTKE2);

        dto.setTutkinnonOsaDto(tosa);
        TutkinnonOsaViiteDto lisatty = perusteService.addTutkinnonOsa(id, suoritustapakoodi, dto);

        OsaAlueLaajaDto osaalue = new OsaAlueLaajaDto();
        osaalue.setTyyppi(OsaAlueTyyppi.OSAALUE2020);
        osaAlueDatas.apply(osaalue);

        osaAlueService.addOsaAlue(id, lisatty.getId(), osaalue);
        return lisatty;
    }

}
