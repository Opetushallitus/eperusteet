package fi.vm.sade.eperusteet.service.impl.navigation;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class NavigationBuilderLops implements NavigationBuilder {

    @Autowired
    private PerusteDispatcher dispatcher;

    @Autowired
    private NavigationBuilderLukio navigationBuilderLukio;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.LOPS);
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli) {
        NavigationBuilder basicBuilder = dispatcher.get(NavigationBuilder.class);
        NavigationNodeDto basicNavigation = basicBuilder.buildNavigation(perusteId, kieli);
        return NavigationNodeDto.of(NavigationType.root)
                .addAll(navigationBuilderLukio.buildNavigation(perusteId, basicNavigation));
    }

}
