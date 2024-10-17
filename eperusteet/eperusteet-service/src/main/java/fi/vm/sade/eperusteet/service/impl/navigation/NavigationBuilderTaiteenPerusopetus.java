package fi.vm.sade.eperusteet.service.impl.navigation;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.yl.TaiteenalaDto;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

// toistaiseksi pois kaytosta
//@Component
//@Transactional
public class NavigationBuilderTaiteenPerusopetus implements NavigationBuilder {

    @Autowired
    private PerusteDispatcher dispatcher;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.TPO);
    }

    @Autowired
    private PerusteenOsaService service;

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli) {
        NavigationBuilder basicBuilder = dispatcher.get(NavigationBuilder.class);
        NavigationNodeDto basicNavigation = basicBuilder.buildNavigation(perusteId, kieli);

        basicNavigation.getChildren().forEach(navigationNodeDto -> {
            PerusteenOsaDto.Laaja viite = service.getByViite(navigationNodeDto.getId());

            if (viite instanceof TaiteenalaDto) {
                TaiteenalaDto taiteenaladto = (TaiteenalaDto) service.getByViite(navigationNodeDto.getId());

                navigationNodeDto.addAll(taiteenaladto.getTaiteenOsat().stream().map(alaosa -> {
                    if (alaosa.getNimi() != null) {
                        return NavigationNodeDto.of(NavigationType.taiteenosa,
                                        alaosa.getNimi())
                                .meta("alaosa", alaosa.getTaiteenOsa())
                                .meta("viiteId", navigationNodeDto.getId());
                    } else {
                        return null;
                    }
                }).filter(Objects::nonNull));
            }
        });

        return basicNavigation;
    }

}
