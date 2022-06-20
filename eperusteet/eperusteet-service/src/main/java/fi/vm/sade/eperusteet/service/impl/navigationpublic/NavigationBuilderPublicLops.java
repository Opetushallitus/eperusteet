package fi.vm.sade.eperusteet.service.impl.navigationpublic;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.NavigationBuilderPublic;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.impl.navigation.NavigationBuilderLukio;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class NavigationBuilderPublicLops implements NavigationBuilderPublic {

    @Autowired
    private PerusteDispatcher dispatcher;

    @Autowired
    private NavigationBuilderPublicLukio navigationBuilderLukio;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.LOPS);
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli, boolean esikatselu) {
        NavigationBuilderPublic basicBuilder = dispatcher.get(NavigationBuilderPublic.class);
        NavigationNodeDto basicNavigation = basicBuilder.buildNavigation(perusteId, kieli, esikatselu);
        return NavigationNodeDto.of(NavigationType.root)
                .addAll(navigationBuilderLukio.buildNavigation(perusteId, basicNavigation, esikatselu));
    }

}
