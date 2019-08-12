package fi.vm.sade.eperusteet.service;


import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.Peruste;
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
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
        PerusteprojektiDto aProjekti = ppTestUtils.createPerusteprojekti(config -> {
            config.setReforminMukainen(true);
        });
        PerusteDto aPeruste = ppTestUtils.initPeruste(aProjekti.getPeruste().getIdLong());

        PerusteprojektiDto bProjekti = ppTestUtils.createPerusteprojekti(config -> {
            config.setReforminMukainen(true);
        });
        PerusteDto bPeruste = ppTestUtils.initPeruste(bProjekti.getPeruste().getIdLong());

        AmmattitaitovaatimusQueryDto pquery = new AmmattitaitovaatimusQueryDto();
        pquery.setUri("ammattitaitovaatimukset_1000");
        Page<PerusteBaseDto> perusteet = ammattitaitovaatimusService.findPerusteet(new PageRequest(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);

        { // Vaatimukset
            Ammattitaitovaatimukset2019 vaatimukset = new Ammattitaitovaatimukset2019();
            Ammattitaitovaatimus2019 vaatimus = new Ammattitaitovaatimus2019();
            Ammattitaitovaatimus2019Kohdealue kohdealue = new Ammattitaitovaatimus2019Kohdealue();
            kohdealue.setVaatimukset(Lists.newArrayList(vaatimus));

            vaatimukset.setKohdealueet(Lists.newArrayList(kohdealue));

            Koodi koodi = new Koodi();
            koodi.setUri("ammattitaitovaatimukset_1000");
            koodi.setKoodisto("ammattitaitovaatimukset");
            vaatimus.setKoodi(koodi);

            Peruste peruste = perusteRepository.findOne(aPeruste.getId());
            TutkinnonOsaViiteDto tosa = ppTestUtils.addTutkinnonOsa(peruste.getId());
            TutkinnonOsaViite tov = tovRepository.findOne(tosa.getId());
            tov.getTutkinnonOsa().setAmmattitaitovaatimukset2019(vaatimukset);
            tovRepository.save(tov);
            em.flush();
        }

        perusteet = ammattitaitovaatimusService.findPerusteet(new PageRequest(0, 10), pquery);
        assertThat(perusteet.getContent().get(0).getId()).isEqualTo(aPeruste.getId());

        {
            Ammattitaitovaatimukset2019 vaatimukset = new Ammattitaitovaatimukset2019();
            Ammattitaitovaatimus2019 vaatimus = new Ammattitaitovaatimus2019();
            Ammattitaitovaatimus2019Kohdealue kohdealue = new Ammattitaitovaatimus2019Kohdealue();
            kohdealue.setVaatimukset(Lists.newArrayList(vaatimus));

            vaatimukset.setKohdealueet(Lists.newArrayList(kohdealue));

            Koodi koodi = new Koodi();
            koodi.setUri("ammattitaitovaatimukset_1000");
            koodi.setKoodisto("ammattitaitovaatimukset");
            vaatimus.setKoodi(koodi);

            Peruste peruste = perusteRepository.findOne(bPeruste.getId());
            TutkinnonOsaViiteDto tosa = ppTestUtils.addTutkinnonOsa(peruste.getId());
            TutkinnonOsaViite tov = tovRepository.findOne(tosa.getId());
            tov.getTutkinnonOsa().setAmmattitaitovaatimukset2019(vaatimukset);
            tovRepository.save(tov);
            em.flush();
        }

        perusteet = ammattitaitovaatimusService.findPerusteet(new PageRequest(0, 10), pquery);
        assertThat(perusteet.getContent())
                .extracting(PerusteBaseDto::getId)
                .containsExactlyInAnyOrder(aPeruste.getId(), bPeruste.getId());

    }


}
