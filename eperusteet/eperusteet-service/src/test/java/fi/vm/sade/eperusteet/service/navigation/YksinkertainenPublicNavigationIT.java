package fi.vm.sade.eperusteet.service.navigation;

import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.service.NavigationBuilderPublic;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.utils.CollectionUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DirtiesContext
public class YksinkertainenPublicNavigationIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteprojektiService service;

    @Autowired
    private PerusteDispatcher dispatcher;
    private Long perusteId;

    @Before
    public void setup() {
        TestTransaction.end();
        TestTransaction.start();
        TestTransaction.flagForCommit();

        PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> {
            ppl.setEsikatseltavissa(true);
            ppl.setKoulutustyyppi(KoulutusTyyppi.ESIOPETUS.toString());
        });
        PerusteDto peruste = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());
        perusteId = peruste.getId();

        PerusteenOsaViiteDto.Matala tekstiKappale1 = perusteService.addSisaltoUUSI(peruste.getId(), Suoritustapakoodi.REFORMI, new PerusteenOsaViiteDto.Matala(new TekstiKappaleDto()));
        PerusteenOsaViiteDto.Matala tekstiKappale11 = perusteService.addSisaltoLapsi(peruste.getId(), tekstiKappale1.getId(), new PerusteenOsaViiteDto.Matala(new TekstiKappaleDto()));
        PerusteenOsaViiteDto.Matala tekstiKappale111 = perusteService.addSisaltoLapsi(peruste.getId(), tekstiKappale11.getId(), new PerusteenOsaViiteDto.Matala(new TekstiKappaleDto()));
        PerusteenOsaViiteDto.Matala tekstiKappale12 = perusteService.addSisaltoLapsi(peruste.getId(), tekstiKappale1.getId(), new PerusteenOsaViiteDto.Matala(new TekstiKappaleDto()));

        PerusteenOsaViiteDto.Matala tekstiKappale2 = perusteService.addSisaltoUUSI(peruste.getId(), Suoritustapakoodi.REFORMI, new PerusteenOsaViiteDto.Matala(new TekstiKappaleDto()));

        PerusteenOsaViiteDto.Matala tekstiKappale3 = perusteService.addSisaltoUUSI(peruste.getId(), Suoritustapakoodi.REFORMI, new PerusteenOsaViiteDto.Matala(new TekstiKappaleDto()));

        TestTransaction.end();
    }

    @Test
    public void testPerusopetusnavigation() {
        TestTransaction.start();
        NavigationNodeDto navigationNodeDto = perusteService.buildNavigationPublic(perusteId, "fi", true, 0);
        assertThat(navigationNodeDto).isNotNull();
        assertThat(navigationNodeDto.getChildren()).hasSize(3);
        assertThat(navigationNodeDto.getChildren().get(0).getChildren()).hasSize(2);
        assertThat(navigationNodeDto.getChildren().get(0).getChildren().get(0).getChildren()).hasSize(1);

        assertThat(CollectionUtil.treeToStream(navigationNodeDto, NavigationNodeDto::getChildren)
                .filter(node -> node.getMeta().containsKey("numerointi"))
                .map(node -> node.getMeta().get("numerointi").toString())
                .collect(Collectors.toList())).contains("1", "2", "3", "1.1", "1.1.1", "1.2");
        TestTransaction.end();
    }

}
