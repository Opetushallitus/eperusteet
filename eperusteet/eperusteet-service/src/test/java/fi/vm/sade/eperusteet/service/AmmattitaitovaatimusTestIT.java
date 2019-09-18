package fi.vm.sade.eperusteet.service;


import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimukset2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019Kohdealue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.KoodiTest;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.AmmattitaitovaatimusQueryDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteKontekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;


@Transactional
@DirtiesContext
public class AmmattitaitovaatimusTestIT extends AbstractPerusteprojektiTest {

    @Autowired
    private PerusteprojektiTestUtils ppTestUtils;

    @Autowired
    private AmmattitaitovaatimusService ammattitaitovaatimusService;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private TutkinnonOsaViiteRepository tovRepository;

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

        PerusteprojektiDto perusteprojekti1 = lisaaPerusteKoodistolla(Arrays.asList("ammattitaitovaatimukset_1000"), ProjektiTila.JULKAISTU);
        PerusteprojektiDto perusteprojekti2 = lisaaPerusteKoodistolla(Arrays.asList("ammattitaitovaatimukset_1000", "ammattitaitovaatimukset_2000"), ProjektiTila.LAADINTA);
        PerusteprojektiDto perusteprojekti3 = lisaaPerusteKoodistolla(Arrays.asList("ammattitaitovaatimukset_2000", "ammattitaitovaatimukset_3000"), ProjektiTila.VALMIS);

        PerusteprojektiDto perusteProjektiPoistettu = lisaaPerusteKoodistolla(
                Arrays.asList("ammattitaitovaatimukset_1000", "ammattitaitovaatimukset_2000", "ammattitaitovaatimukset_3000"), ProjektiTila.POISTETTU);

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

    private PerusteprojektiDto lisaaPerusteKoodistolla(Collection<String> koodiUrit, ProjektiTila tila) {

        PerusteprojektiDto aProjekti = ppTestUtils.createPerusteprojekti(config -> {
            config.setReforminMukainen(true);
        });
        PerusteDto aPeruste = ppTestUtils.initPeruste(aProjekti.getPeruste().getIdLong());

        Ammattitaitovaatimukset2019 vaatimukset = new Ammattitaitovaatimukset2019();
        Ammattitaitovaatimus2019Kohdealue kohdealue = new Ammattitaitovaatimus2019Kohdealue();
        kohdealue.setVaatimukset(createVaatimukset(koodiUrit));

        vaatimukset.setKohdealueet(Lists.newArrayList(kohdealue));

        Peruste peruste = perusteRepository.findOne(aPeruste.getId());
        TutkinnonOsaViiteDto tosa = ppTestUtils.addTutkinnonOsa(peruste.getId());
        TutkinnonOsaViite tov = tovRepository.findOne(tosa.getId());
        tov.getTutkinnonOsa().setAmmattitaitovaatimukset2019(vaatimukset);
        tovRepository.save(tov);
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
