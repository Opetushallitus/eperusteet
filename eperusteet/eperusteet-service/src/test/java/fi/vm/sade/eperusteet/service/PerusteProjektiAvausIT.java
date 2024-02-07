package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DirtiesContext
@ActiveProfiles(profiles = {"test", "realPermissions"})
public class PerusteProjektiAvausIT extends AbstractPerusteprojektiTest {

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Test
    public void testPerusteprojektiAvaus() {
        loginAsUser("testOphAdmin");
        PerusteprojektiDto projektiDto = testUtils.createPerusteprojekti();
        Perusteprojekti projekti = mapper.map(projektiDto, Perusteprojekti.class);
        projekti.setTila(ProjektiTila.JULKAISTU);
        projekti.getPeruste().asetaTila(PerusteTila.VALMIS);
        perusteprojektiRepository.save(projekti);

        projektiDto = perusteprojektiService.get(projekti.getId());
        assertThat(projektiDto.getTila()).isEqualTo(ProjektiTila.JULKAISTU);
        PerusteDto peruste = perusteService.get(projekti.getPeruste().getId());
        assertThat(peruste.getTila()).isEqualTo(PerusteTila.VALMIS);

        perusteprojektiService.avaaPerusteProjekti(projekti.getId());
        projektiDto = perusteprojektiService.get(projekti.getId());
        assertThat(projektiDto.getTila()).isEqualTo(ProjektiTila.LAADINTA);
        peruste = perusteService.get(projekti.getPeruste().getId());
        assertThat(peruste.getTila()).isEqualTo(PerusteTila.LUONNOS);
    }

    @Test(expected = AccessDeniedException.class)
    public void testPerusteprojektiAvausNoPermissions() {
        loginAsUser("test");
        PerusteprojektiDto projektiDto = testUtils.createPerusteprojekti();
        Perusteprojekti projekti = mapper.map(projektiDto, Perusteprojekti.class);
        projekti.setTila(ProjektiTila.JULKAISTU);
        projekti.getPeruste().asetaTila(PerusteTila.VALMIS);
        perusteprojektiRepository.save(projekti);

        projektiDto = perusteprojektiService.get(projekti.getId());
        assertThat(projektiDto.getTila()).isEqualTo(ProjektiTila.JULKAISTU);
        PerusteDto peruste = perusteService.get(projekti.getPeruste().getId());
        assertThat(peruste.getTila()).isEqualTo(PerusteTila.VALMIS);

        perusteprojektiService.avaaPerusteProjekti(projekti.getId());
    }
}
