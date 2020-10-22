package fi.vm.sade.eperusteet.service;


import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimukset2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019Kohdealue;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.AmmattitaitovaatimusQueryDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.Ammattitaitovaatimukset2019Dto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteKontekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@Transactional
@DirtiesContext
@ActiveProfiles(profiles = {"test", "realPermissions"})
public class AmmattitaitovaatimusTestIT extends AbstractPerusteprojektiTest {

    @Autowired
    private PerusteprojektiTestUtils ppTestUtils;

    @Autowired
    private AmmattitaitovaatimusService ammattitaitovaatimusService;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private TutkinnonOsaViiteRepository tovRepository;

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private OphClientHelper ophClientHelper;

    @Test
    @Rollback
    public void testAmmattitaitovaatimuskoodinAvullaPeruste() {

        {
            AmmattitaitovaatimusQueryDto pquery = new AmmattitaitovaatimusQueryDto();
            pquery.setUri("ammattitaitovaatimukset_1000");
            Page<PerusteBaseDto> perusteet = ammattitaitovaatimusService.findPerusteet(new PageRequest(0, 10), pquery);
            Page<TutkinnonOsaViiteKontekstiDto> tosat = ammattitaitovaatimusService.findTutkinnonOsat(new PageRequest(0, 10), pquery);
            assertThat(perusteet.getTotalElements()).isEqualTo(0);
            assertThat(tosat.getTotalElements()).isEqualTo(0);
        }

        PerusteprojektiDto perusteprojekti1 = lisaaPerusteKoodistolla(asList("ammattitaitovaatimukset_1000"), ProjektiTila.JULKAISTU);
        PerusteprojektiDto perusteprojekti2 = lisaaPerusteKoodistolla(asList("ammattitaitovaatimukset_1000", "ammattitaitovaatimukset_2000"), ProjektiTila.LAADINTA);
        PerusteprojektiDto perusteprojekti3 = lisaaPerusteKoodistolla(asList("ammattitaitovaatimukset_2000", "ammattitaitovaatimukset_3000"), ProjektiTila.VALMIS);

        PerusteprojektiDto perusteProjektiPoistettu = lisaaPerusteKoodistolla(
                asList("ammattitaitovaatimukset_1000", "ammattitaitovaatimukset_2000", "ammattitaitovaatimukset_3000"), ProjektiTila.POISTETTU);

        {
            AmmattitaitovaatimusQueryDto pquery = new AmmattitaitovaatimusQueryDto();
            pquery.setKaikki(true);
            pquery.setUri("ammattitaitovaatimukset_1000");
            Page<PerusteBaseDto> perusteet = ammattitaitovaatimusService.findPerusteet(new PageRequest(0, 10), pquery);
            Page<TutkinnonOsaViiteKontekstiDto> tosat = ammattitaitovaatimusService.findTutkinnonOsat(new PageRequest(0, 10), pquery);
            assertThat(tosat.getTotalElements()).isEqualTo(2);
            assertThat(perusteet.getContent())
                    .extracting(PerusteBaseDto::getId)
                    .containsExactlyInAnyOrder(perusteprojekti1.getPeruste().getIdLong(), perusteprojekti2.getPeruste().getIdLong());
        }

        {
            AmmattitaitovaatimusQueryDto pquery = new AmmattitaitovaatimusQueryDto();
            pquery.setKaikki(true);
            pquery.setUri("ammattitaitovaatimukset_2000");
            Page<PerusteBaseDto> perusteet = ammattitaitovaatimusService.findPerusteet(new PageRequest(0, 10), pquery);
            Page<TutkinnonOsaViiteKontekstiDto> tosat = ammattitaitovaatimusService.findTutkinnonOsat(new PageRequest(0, 10), pquery);
            assertThat(tosat.getTotalElements()).isEqualTo(2);
            assertThat(perusteet.getContent())
                    .extracting(PerusteBaseDto::getId)
                    .containsExactlyInAnyOrder(perusteprojekti2.getPeruste().getIdLong(), perusteprojekti3.getPeruste().getIdLong());
        }

        {
            AmmattitaitovaatimusQueryDto pquery = new AmmattitaitovaatimusQueryDto();
            pquery.setKaikki(false);
            pquery.setUri("ammattitaitovaatimukset_1000");
            Page<PerusteBaseDto> perusteet = ammattitaitovaatimusService.findPerusteet(new PageRequest(0, 10), pquery);
            Page<TutkinnonOsaViiteKontekstiDto> tosat = ammattitaitovaatimusService.findTutkinnonOsat(new PageRequest(0, 10), pquery);
            assertThat(tosat.getTotalElements()).isEqualTo(1);
            assertThat(perusteet.getContent())
                    .extracting(PerusteBaseDto::getId)
                    .containsExactlyInAnyOrder(perusteprojekti1.getPeruste().getIdLong());
        }

    }

    @Test
    public void testfindTutkinnonOsat_accessDenied() {

        loginAsUser("test8");

        AmmattitaitovaatimusQueryDto pquery = new AmmattitaitovaatimusQueryDto();
        pquery.setKaikki(true);
        pquery.setUri("ammattitaitovaatimukset_1000");

        Assertions.assertThatThrownBy(() -> {
            ammattitaitovaatimusService.findTutkinnonOsat(new PageRequest(0, 10), pquery);
        }).isInstanceOf(AccessDeniedException.class);

        loginAsUser("test");
        Page<TutkinnonOsaViiteKontekstiDto> tosat = ammattitaitovaatimusService.findTutkinnonOsat(new PageRequest(0, 10), pquery);
        assertThat(tosat.getTotalElements()).isEqualTo(0);
    }

    @Test
    public void testLisaaAmmattitaitovaatimusTutkinnonosaKoodistoon() {

        rakennaAmmattitaitovaatimusLatausPohjadata();

        ammattitaitovaatimusService.lisaaAmmattitaitovaatimusTutkinnonosaKoodistoon(new GregorianCalendar(2017, 1, 1).getTime());

        //posturl mockitettu KoodistoMockissa

        verify(ophClientHelper, times(8)).post(any(), any());

        verify(ophClientHelper).post("", "koodirelaatio" + "koulutus_1000" + "tutkinnonosat_200530");
        verify(ophClientHelper).post("", "koodirelaatio" + "koulutus_1001" + "tutkinnonosat_200530");
        verify(ophClientHelper).post("", "koodirelaatio" + "tutkinnonosat_200530" + "[ammattitaitovaatimukset_1000, ammattitaitovaatimukset_1001, ammattitaitovaatimukset_1002]");
        verify(ophClientHelper).post("", "koodirelaatio" + "tutkintonimike_1000" + "tutkinnonosa_1000");
        verify(ophClientHelper).post("", "koodirelaatio" + "osaamisala_1000" + "tutkinnonosa_1000");
        verify(ophClientHelper).post("", "koodirelaatio" + "koulutus_2000" + "tutkinnonosat_200530");
        verify(ophClientHelper, Mockito.times(2)).post("", "koodirelaatio" + "tutkinnonosat_200530" + "ammattitaitovaatimukset_2000");
    }

    @Test
    public void test_findAmmattitaitovaatimusPerusteelliset() {

        rakennaAmmattitaitovaatimusLatausPohjadata();

        // kaikki
        assertThat(perusteRepository.findAll()).hasSize(4);

        // ammattivaatimuksilla 2
        assertThat(perusteRepository.findAmmattitaitovaatimusPerusteelliset(ProjektiTila.JULKAISTU, new DateTime(1970, 1, 1, 0, 0).toDate(),
                PerusteTyyppi.NORMAALI, KoulutusTyyppi.ammatilliset(), Suoritustapakoodi.REFORMI)).hasSize(2);

        // lukiolla 0
        assertThat(perusteRepository.findAmmattitaitovaatimusPerusteelliset(ProjektiTila.JULKAISTU, new DateTime(1970, 1, 1, 0, 0).toDate(),
                PerusteTyyppi.NORMAALI, Arrays.asList(KoulutusTyyppi.LUKIOVALMISTAVAKOULUTUS.toString()), Suoritustapakoodi.LUKIOKOULUTUS)).hasSize(0);

        // aikavertailulla 0
        assertThat(perusteRepository.findAmmattitaitovaatimusPerusteelliset(ProjektiTila.JULKAISTU, new DateTime(2999, 1, 1, 0, 0).toDate(),
                PerusteTyyppi.NORMAALI, KoulutusTyyppi.ammatilliset(), Suoritustapakoodi.REFORMI)).hasSize(0);

    }

    @Test
    public void test_kaikkiRajapinta() {
        PerusteprojektiDto perusteprojektiDto = rakennaAmmattitaitovaatimusLatausPohjadata();
        Long perusteId = perusteprojektiDto.getPeruste().getIdLong();
        PerusteKaikkiDto kaikki = perusteService.getJulkaistuSisalto(perusteId);
        assertThat(kaikki).isNotNull();
        Ammattitaitovaatimukset2019Dto av = kaikki.getTutkinnonOsat().get(0).getAmmattitaitovaatimukset2019();
        assertThat(av).isNotNull()
                .returns(1, x -> x.getKohdealueet().size())
                .returns(2, x -> x.getKohdealueet().get(0).getVaatimukset().size())
                .returns(2, x -> x.getVaatimukset().size());

    }

    @Test
    public void test_addAmmattitaitovaatimuskooditToKoodisto() {

        PerusteprojektiDto aProjekti = ppTestUtils.createPerusteprojekti(config -> {
            config.setReforminMukainen(true);
        });
        PerusteDto aPeruste = ppTestUtils.initPeruste(aProjekti.getPeruste().getIdLong());

        Ammattitaitovaatimukset2019 vaatimukset = new Ammattitaitovaatimukset2019();
        Ammattitaitovaatimus2019Kohdealue kohdealue = new Ammattitaitovaatimus2019Kohdealue();
        kohdealue.setVaatimukset(Arrays.asList(
                Ammattitaitovaatimus2019.of(TekstiPalanen.of(Kieli.FI, "teksti1")),
                Ammattitaitovaatimus2019.of(TekstiPalanen.of(Kieli.FI, "teksti1")),
                Ammattitaitovaatimus2019.of(TekstiPalanen.of(Kieli.FI, "teksti2")),
                Ammattitaitovaatimus2019.of(TekstiPalanen.of(Kieli.FI, "teksti2")),
                Ammattitaitovaatimus2019.of(TekstiPalanen.of(Kieli.FI, "teksti3"))
        ));
        vaatimukset.setKohdealueet(Lists.newArrayList(kohdealue));


        Peruste peruste = perusteRepository.findOne(aPeruste.getId());
        TutkinnonOsaViiteDto tosa = ppTestUtils.addTutkinnonOsa(peruste.getId());
        TutkinnonOsaViite tov = tovRepository.findOne(tosa.getId());
        tov.getTutkinnonOsa().setAmmattitaitovaatimukset2019(vaatimukset);
        tovRepository.save(tov);
        em.flush();

        List<KoodiDto> lisatytKoodit = ammattitaitovaatimusService.addAmmattitaitovaatimuskooditToKoodisto(peruste.getId());
        assertThat(lisatytKoodit).hasSize(3);
        assertThat(lisatytKoodit).extracting("uri").containsExactlyInAnyOrder("ammattitaitovaatimukset_0", "ammattitaitovaatimukset_1", "ammattitaitovaatimukset_2");

        List<Ammattitaitovaatimus2019> tallennetutVaatimukset = tovRepository.findOne(tosa.getId()).getTutkinnonOsa().getAmmattitaitovaatimukset2019().getKohdealueet().stream()
                .map(kohdealue2 -> kohdealue.getVaatimukset())
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());

        assertThat(tallennetutVaatimukset).hasSize(5);
        assertThat(tallennetutVaatimukset).extracting("koodi.uri").containsExactlyInAnyOrder("ammattitaitovaatimukset_0", "ammattitaitovaatimukset_1", "ammattitaitovaatimukset_1", "ammattitaitovaatimukset_2", "ammattitaitovaatimukset_2");
    }

    @Test
    public void test_addArvioinninKohdealueetToKoodisto() {

        PerusteprojektiDto aProjekti = ppTestUtils.createPerusteprojekti(config -> {
            config.setReforminMukainen(true);
        });
        PerusteDto aPeruste = ppTestUtils.initPeruste(aProjekti.getPeruste().getIdLong());

        Arviointi arviointi = new Arviointi();
        arviointi.setArvioinninKohdealueet(Arrays.asList(
                arvioinninKohdealue(TekstiPalanen.of(Kieli.FI, "teksti1")),
                arvioinninKohdealue(TekstiPalanen.of(Kieli.FI, "teksti1")),
                arvioinninKohdealue(TekstiPalanen.of(Kieli.FI, "teksti2")),
                arvioinninKohdealue(TekstiPalanen.of(Kieli.FI, "teksti2")),
                arvioinninKohdealue(TekstiPalanen.of(Kieli.FI, "teksti3"))
        ));

        Peruste peruste = perusteRepository.findOne(aPeruste.getId());
        TutkinnonOsaViiteDto tosa = ppTestUtils.addTutkinnonOsa(peruste.getId());
        TutkinnonOsaViite tov = tovRepository.findOne(tosa.getId());
        tov.getTutkinnonOsa().setArviointi(arviointi);
        tovRepository.save(tov);
        em.flush();

        List<KoodiDto> lisatytKoodit = ammattitaitovaatimusService.addArvioinninKohdealueetToKoodisto(peruste.getId());
        assertThat(lisatytKoodit).hasSize(3);
        assertThat(lisatytKoodit).extracting("uri").containsExactlyInAnyOrder("ammattitaitovaatimukset_0", "ammattitaitovaatimukset_1", "ammattitaitovaatimukset_2");

        List<ArvioinninKohdealue> tallennetutVaatimukset = tovRepository.findOne(tosa.getId()).getTutkinnonOsa().getArviointi().getArvioinninKohdealueet().stream().collect(Collectors.toList());

        assertThat(tallennetutVaatimukset).hasSize(5);
        assertThat(tallennetutVaatimukset).extracting("koodi.uri").containsExactlyInAnyOrder("ammattitaitovaatimukset_0", "ammattitaitovaatimukset_1", "ammattitaitovaatimukset_1", "ammattitaitovaatimukset_2", "ammattitaitovaatimukset_2");
    }

    private ArvioinninKohdealue arvioinninKohdealue(TekstiPalanen tekstipalanen) {
        ArvioinninKohdealue kohdealue = new ArvioinninKohdealue();
        kohdealue.setOtsikko(tekstipalanen);
        return kohdealue;
    }

    private PerusteprojektiDto rakennaAmmattitaitovaatimusLatausPohjadata() {
        loginAsUser("test");

        PerusteprojektiDto perusteprojekti = lisaaPerusteKoodistolla(asList("ammattitaitovaatimukset_1000", "ammattitaitovaatimukset_1001", "ammattitaitovaatimukset_1002", null), ProjektiTila.JULKAISTU, false);
        lisaaKoulutukset(new Long(perusteprojekti.getPeruste().getId()), asList("koulutus_1000", "koulutus_1001"));
        perusteprojekti = lisaaPerusteKoodistolla(asList("ammattitaitovaatimukset_2000", null), ProjektiTila.JULKAISTU, true);
        lisaaKoulutukset(new Long(perusteprojekti.getPeruste().getId()), asList("koulutus_2000"));
        lisaaTutkintonimikkeet(new Long(perusteprojekti.getPeruste().getId()), asList("1000"));

        // not found perusteet
        perusteprojekti = lisaaPerusteKoodistollaJaKoulutustyypilla(asList("ammattitaitovaatimukset_X000"), ProjektiTila.JULKAISTU, KoulutusTyyppi.LUKIOVALMISTAVAKOULUTUS);

        perusteprojekti = lisaaPerusteKoodistolla(asList("ammattitaitovaatimukset_3000", null), ProjektiTila.VALMIS);
        lisaaKoulutukset(new Long(perusteprojekti.getPeruste().getId()), asList("koulutus_3000", "koulutus_3001"));

        updateKaikkienPerusteenOsienTilat(PerusteTila.VALMIS);
        return perusteprojekti;

    }

    private PerusteprojektiDto lisaaPerusteKoodistolla(Collection<String> koodiUrit, ProjektiTila tila) {
        return lisaaPerusteKoodistolla(koodiUrit, tila, true);
    }

    private PerusteprojektiDto lisaaPerusteKoodistolla(Collection<String> koodiUrit, ProjektiTila tila, boolean lisaaKohdealueelliset) {

        PerusteprojektiDto aProjekti = ppTestUtils.createPerusteprojekti(config -> {
            config.setReforminMukainen(true);
        });
        PerusteDto aPeruste = ppTestUtils.initPeruste(aProjekti.getPeruste().getIdLong());

        Ammattitaitovaatimukset2019 vaatimukset = new Ammattitaitovaatimukset2019();

        if (lisaaKohdealueelliset) {
            Ammattitaitovaatimus2019Kohdealue kohdealue = new Ammattitaitovaatimus2019Kohdealue();
            kohdealue.setVaatimukset(createVaatimukset(koodiUrit));
            vaatimukset.setKohdealueet(Lists.newArrayList(kohdealue));
        }

        vaatimukset.setVaatimukset(createVaatimukset(koodiUrit));

        Peruste peruste = perusteRepository.findOne(aPeruste.getId());
        TutkinnonOsaViiteDto tosa = ppTestUtils.addTutkinnonOsa(peruste.getId());
        TutkinnonOsaViite tov = tovRepository.findOne(tosa.getId());
        tov.getTutkinnonOsa().setAmmattitaitovaatimukset2019(vaatimukset);
        tovRepository.save(tov);
        em.flush();

        ppTestUtils.asetaProjektiTilaan(aProjekti.getId(), tila);

        return aProjekti;
    }

    private PerusteprojektiDto lisaaPerusteKoodistollaJaKoulutustyypilla(Collection<String> koodiUrit, ProjektiTila tila, KoulutusTyyppi koulutustyyppi) {

        PerusteprojektiDto aProjekti = ppTestUtils.createPerusteprojekti(config -> {
            config.setReforminMukainen(false);
            config.setKoulutustyyppi(koulutustyyppi.toString());
        });
        PerusteDto aPeruste = ppTestUtils.initPeruste(aProjekti.getPeruste().getIdLong());

        em.flush();

        ppTestUtils.asetaProjektiTilaan(aProjekti.getId(), tila);

        return aProjekti;
    }

    private List<Ammattitaitovaatimus2019> createVaatimukset(Collection<String> koodiUrit) {
        return koodiUrit.stream().map(koodiUri -> {
            Ammattitaitovaatimus2019 vaatimus = new Ammattitaitovaatimus2019();
            Koodi koodi = new Koodi();
            koodi.setUri(koodiUri);
            koodi.setKoodisto("ammattitaitovaatimukset");
            if (koodiUri != null) {
                vaatimus.setKoodi(koodi);
            }
            return vaatimus;
        }).collect(Collectors.toList());
    }

}
