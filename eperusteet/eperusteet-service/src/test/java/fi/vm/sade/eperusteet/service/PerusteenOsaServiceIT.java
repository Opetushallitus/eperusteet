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

import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tuva.KoulutusOsanKoulutustyyppi;
import fi.vm.sade.eperusteet.domain.vst.TavoiteAlueTyyppi;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tuva.KoulutuksenOsaDto;
import fi.vm.sade.eperusteet.dto.tuva.TuvaLaajaAlainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.vst.KotoKielitaitotasoDto;
import fi.vm.sade.eperusteet.dto.vst.KotoOpintoDto;
import fi.vm.sade.eperusteet.dto.vst.KotoTaitotasoDto;
import fi.vm.sade.eperusteet.dto.vst.OpintokokonaisuusDto;
import fi.vm.sade.eperusteet.dto.vst.TavoiteAlueDto;
import fi.vm.sade.eperusteet.dto.vst.TavoitesisaltoalueDto;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolationException;
import org.assertj.core.util.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author teele1
 */
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
    public void testVstKotoKielitaitotaso() {
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
                        .vuorovaikutusJaMeditaatio(LokalisoituTekstiDto.of("testiVuorovaikutusJaMeditaatio"))
                        .build()
        ));
        kotoKielitaitotaso = perusteenOsaService.update(kotoKielitaitotaso);

        assertThat(kotoKielitaitotaso.getNimi().get(Kieli.FI)).isEqualTo("nimi2");
        assertThat(kotoKielitaitotaso.getKuvaus().get(Kieli.FI)).isEqualTo("teksti2");
        assertThat(kotoKielitaitotaso.getNimiKoodi()).isEqualTo(KoodiDto.of(KoodistoUriArvo.TAVOITESISALTOALUEENOTSIKKO, "nimi2"));
        assertThat(kotoKielitaitotaso.getTaitotasot()).hasSize(1);
        assertThat(kotoKielitaitotaso.getTaitotasot().get(0)).extracting("nimi").isEqualTo(KoodiDto.of(KoodistoUriArvo.KOTOUTUMISKOULUTUSTAVOITTEET, "taitotasonimi2"));
        assertThat(kotoKielitaitotaso.getTaitotasot())
                .flatExtracting("aihealueet", "kielenkayttotarkoitus", "opiskelijantaidot", "tavoitteet", "viestintataidot", "suullinenVastaanottaminen", "suullinenTuottaminen", "vuorovaikutusJaMeditaatio")
                .extracting("tekstit")
                .containsExactlyInAnyOrder(
                        Maps.newHashMap(Kieli.FI, "aihealueet2"),
                        Maps.newHashMap(Kieli.FI, "kielenkayttotarkoitus2"),
                        Maps.newHashMap(Kieli.FI, "opiskelijantaidot2"),
                        Maps.newHashMap(Kieli.FI, "tavoitteet2"),
                        Maps.newHashMap(Kieli.FI, "viestintataidot2"),
                        Maps.newHashMap(Kieli.FI, "testiSuullinenVastaanottaminen"),
                        Maps.newHashMap(Kieli.FI, "testiSuullinenTuottaminen"),
                        Maps.newHashMap(Kieli.FI, "testiVuorovaikutusJaMeditaatio"));
    }

    @Test
    public void testVstKotoOpinto() {
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

}
