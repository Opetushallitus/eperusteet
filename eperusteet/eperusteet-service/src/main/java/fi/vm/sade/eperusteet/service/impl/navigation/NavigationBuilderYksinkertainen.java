package fi.vm.sade.eperusteet.service.impl.navigation;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import java.util.Set;

import fi.vm.sade.eperusteet.utils.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class NavigationBuilderYksinkertainen implements NavigationBuilder {

    @Autowired
    private PerusteDispatcher dispatcher;

    @Autowired
    private PerusteRepository perusteRepository;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.YKSINKERTAINEN);
    }

    @Autowired
    private NavigationBuilderAipe navigationBuilderAipe;

    @Autowired
    private NavigationBuilderLukio navigationBuilderLukio;

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli) {
        NavigationBuilder basicBuilder = dispatcher.get(NavigationBuilder.class);
        NavigationNodeDto basicNavigation = basicBuilder.buildNavigation(perusteId, kieli);

        Peruste peruste = perusteRepository.getOne(perusteId);

        if (peruste.getKoulutustyyppi() != null && KoulutusTyyppi.of(peruste.getKoulutustyyppi()).equals(KoulutusTyyppi.AIKUISTENPERUSOPETUS)) {
            CollectionUtil.treeToStream(basicNavigation, NavigationNodeDto::getChildren)
                    .filter(node -> node.getType().equals(NavigationType.aipe_laajaalaisetosaamiset))
                    .forEach(node -> node.addAll(peruste.getAipeOpetuksenPerusteenSisalto().getLaajaalaisetosaamiset().stream()
                                .map(lao -> NavigationNodeDto.of(NavigationType.aipe_laajaalainenosaaminen, LokalisoituTekstiDto.of(lao.getNimi().getTeksti()), lao.getId()))));

            return basicNavigation.addAll(navigationBuilderAipe.buildNavigation(perusteId, kieli));
        }

        if (peruste.getKoulutustyyppi() != null && KoulutusTyyppi.of(peruste.getKoulutustyyppi()).equals(KoulutusTyyppi.LUKIOVALMISTAVAKOULUTUS)) {
            return navigationBuilderLukio.buildNavigation(perusteId, basicNavigation);
        }

        return basicNavigation;
    }

}
