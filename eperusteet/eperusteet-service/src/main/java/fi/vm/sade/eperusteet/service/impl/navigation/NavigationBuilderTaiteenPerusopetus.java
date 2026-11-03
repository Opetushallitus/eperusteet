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
public class NavigationBuilderTaiteenPerusopetus implements NavigationBuilder {

    @Autowired
    private PerusteDispatcher dispatcher;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.TPO);
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli) {
        NavigationBuilder basicBuilder = dispatcher.get(NavigationBuilder.class);
        NavigationNodeDto basicNavigation = basicBuilder.buildNavigation(perusteId, kieli);
        lisaaVaiheidenTaiteenala(basicNavigation);
        return basicNavigation;
    }

    private void lisaaVaiheidenTaiteenala(NavigationNodeDto node) {
        node.getChildren().forEach(lapsi -> {
            if (NavigationType.taiteenala.equals(node.getType()) && NavigationType.taiteenalavaihe.equals(lapsi.getType())) {
                lapsi.meta("taiteenalaId", node.getId());
            }
            lisaaVaiheidenTaiteenala(lapsi);
        });
    }
}
