package fi.vm.sade.eperusteet.service;


import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimukset2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019Kohdealue;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.AmmattitaitovaatimusQueryDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteKontekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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
        verify(ophClientHelper).post("", "koodirelaatio" + "koulutus_2000" + "tutkinnonosat_200530");
        verify(ophClientHelper).post("", "koodirelaatio" + "tutkintonimike_1000" + "tutkinnonosat_200530");
        verify(ophClientHelper).post("", "koodirelaatio" + "tutkinnonosat_200530" + "ammattitaitovaatimukset_1000");
        verify(ophClientHelper).post("", "koodirelaatio" + "tutkinnonosat_200530" + "ammattitaitovaatimukset_1001");
        verify(ophClientHelper).post("", "koodirelaatio" + "tutkinnonosat_200530" + "ammattitaitovaatimukset_1002");
        verify(ophClientHelper).post("", "koodirelaatio" + "tutkinnonosat_200530" + "ammattitaitovaatimukset_2000");
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
        assertThat(perusteRepository.findAmmattitaitovaatimusPerusteelliset(ProjektiTila.JULKAISTU, new DateTime(2020, 1, 1, 0, 0).toDate(),
                PerusteTyyppi.NORMAALI, KoulutusTyyppi.ammatilliset(), Suoritustapakoodi.REFORMI)).hasSize(0);

    }

    private void rakennaAmmattitaitovaatimusLatausPohjadata() {
        loginAsUser("test");

        PerusteprojektiDto perusteprojekti = lisaaPerusteKoodistolla(asList("ammattitaitovaatimukset_1000", "ammattitaitovaatimukset_1001", "ammattitaitovaatimukset_1002"), ProjektiTila.JULKAISTU);
        lisaaKoulutukset(new Long(perusteprojekti.getPeruste().getId()), asList("koulutus_1000", "koulutus_1001"));
        perusteprojekti = lisaaPerusteKoodistolla(asList("ammattitaitovaatimukset_2000"), ProjektiTila.JULKAISTU);
        lisaaKoulutukset(new Long(perusteprojekti.getPeruste().getId()), asList("koulutus_2000"));
        lisaaTutkintonimikkeet(new Long(perusteprojekti.getPeruste().getId()), asList("tutkintonimike_1000"));

        // not found perusteet
        perusteprojekti = lisaaPerusteKoodistollaJaKoulutustyypilla(asList("ammattitaitovaatimukset_X000"), ProjektiTila.JULKAISTU, KoulutusTyyppi.LUKIOVALMISTAVAKOULUTUS);

        perusteprojekti = lisaaPerusteKoodistolla(asList("ammattitaitovaatimukset_3000"), ProjektiTila.VALMIS);
        lisaaKoulutukset(new Long(perusteprojekti.getPeruste().getId()), asList("koulutus_3000", "koulutus_3001"));

        updateKaikkienPerusteenOsienTilat(PerusteTila.VALMIS);

    }

    private PerusteprojektiDto lisaaPerusteKoodistolla(Collection<String> koodiUrit, ProjektiTila tila) {

        PerusteprojektiDto aProjekti = ppTestUtils.createPerusteprojekti(config -> {
            config.setReforminMukainen(true);
        });
        PerusteDto aPeruste = ppTestUtils.initPeruste(aProjekti.getPeruste().getIdLong());

        Ammattitaitovaatimukset2019 vaatimukset = new Ammattitaitovaatimukset2019();
        Ammattitaitovaatimus2019Kohdealue kohdealue = new Ammattitaitovaatimus2019Kohdealue();
        kohdealue.setVaatimukset(createVaatimukset(koodiUrit));

        vaatimukset.setKohdealueet(Lists.newArrayList(kohdealue));
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
            vaatimus.setKoodi(koodi);
            return vaatimus;
        }).collect(Collectors.toList());
    }

}
