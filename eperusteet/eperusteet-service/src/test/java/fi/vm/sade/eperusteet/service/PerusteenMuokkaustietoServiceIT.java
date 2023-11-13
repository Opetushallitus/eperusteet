package fi.vm.sade.eperusteet.service;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenMuokkaustieto;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.tuva.KoulutuksenOsaDto;
import fi.vm.sade.eperusteet.repository.PerusteenMuokkaustietoRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PerusteenMuokkaustietoServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteprojektiTestUtils testUtils;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private PerusteenMuokkaustietoRepository muokkausTietoRepository;

    @Autowired
    private PerusteService perusteService;

    @Dto
    @Autowired
    private DtoMapper mapper;

    private Perusteprojekti projekti;
    private Peruste peruste;

    @Before
    public void setup() {
        PerusteprojektiDto projektiDto = testUtils.createPerusteprojekti();
        projekti = perusteprojektiRepository.findById(projektiDto.getId()).orElseThrow();
        peruste = projekti.getPeruste();
        Suoritustapa s = new Suoritustapa();
        s.setSuoritustapakoodi(Suoritustapakoodi.OPS);
        peruste.setSuoritustavat(Sets.newHashSet(s));
        peruste.setPaatospvm(new Date());
        perusteService.update(peruste.getId(), mapper.map(peruste, PerusteDto.class));
    }

    @Test
    @Rollback
    public void testPaivitettyPeruste() {
        assertThat(getMuokkaustiedot(peruste.getId()))
                .extracting(PerusteenMuokkaustieto::getKohde)
                .contains(NavigationType.peruste);
    }

    @Test
    @Rollback
    public void testLisaaTekstikappale() {
        addPerusteenOsa(new TekstiKappaleDto());
        assertThat(getMuokkaustiedot(peruste.getId()))
                .extracting(PerusteenMuokkaustieto::getKohde)
                .contains(NavigationType.tekstikappale);
    }

    @Test
    @Rollback
    public void testLisaaKoulutuksenosa() {
        addPerusteenOsa(new KoulutuksenOsaDto());
        assertThat(getMuokkaustiedot(peruste.getId()))
                .extracting(PerusteenMuokkaustieto::getKohde)
                .contains(NavigationType.koulutuksenosa);
    }

    @Test
    @Rollback
    public void testMuokkaustiedot() {
        addPerusteenOsa(new TekstiKappaleDto());
        addPerusteenOsa(new KoulutuksenOsaDto());
        assertThat(getMuokkaustiedotValilta(peruste.getId()))
                .hasSize(2)
                .extracting(PerusteenMuokkaustieto::getKohde)
                .containsExactlyInAnyOrder(NavigationType.koulutuksenosa, NavigationType.tekstikappale);
    }

    private void addPerusteenOsa(PerusteenOsaDto.Laaja perusteenOsaDto) {
        PerusteenOsaViiteDto.Matala pov = new PerusteenOsaViiteDto.Matala(new KoulutuksenOsaDto());
        pov.setPerusteenOsa(perusteenOsaDto);
        perusteService.addSisaltoUUSI(peruste.getId(), Suoritustapakoodi.OPS, pov);
    }

    private List<PerusteenMuokkaustieto> getMuokkaustiedot(Long perusteId) {
        return muokkausTietoRepository.findByPerusteIdAndLuotuBeforeOrderByLuotuDesc(perusteId, new Date(), PageRequest.of(0, 100));
    }

    private List<PerusteenMuokkaustieto> getMuokkaustiedotValilta(Long perusteId) {
        List<MuokkausTapahtuma> includeTapahtumat = Arrays.asList(MuokkausTapahtuma.LUONTI, MuokkausTapahtuma.PAIVITYS, MuokkausTapahtuma.POISTO);
        List<NavigationType> excludeTypet = List.of(NavigationType.peruste);
        return muokkausTietoRepository.findByPerusteIdAndLuotuIsBetweenAndTapahtumaIsInAndKohdeNotIn(perusteId, peruste.getLuotu(), new Date(), includeTapahtumat, excludeTypet);
    }
}
