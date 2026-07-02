package fi.vm.sade.eperusteet.service.impl.navigation;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.yl.TaiteenalaDto;
import fi.vm.sade.eperusteet.dto.yl.TaiteenosaDto;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Component
@Transactional
public class NavigationBuilderTaiteenPerusopetus implements NavigationBuilder {

    @Autowired
    private PerusteDispatcher dispatcher;

    @Autowired
    private PerusteenOsaService service;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.TPO);
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli) {
        NavigationBuilder basicBuilder = dispatcher.get(NavigationBuilder.class);
        NavigationNodeDto basicNavigation = basicBuilder.buildNavigation(perusteId, kieli);

        basicNavigation.getChildren().forEach(navigationNodeDto -> {
            PerusteenOsaDto.Laaja viite = service.getByViite(navigationNodeDto.getId());

            if (viite instanceof TaiteenalaDto taiteenaladto && !CollectionUtils.isEmpty(taiteenaladto.getTaiteenOsat())) {
                navigationNodeDto.addAll(taiteenaladto.getTaiteenOsat().stream()
                        .map(taiteenosa -> taiteenosaNode(taiteenosa, navigationNodeDto.getId()))
                        .filter(Objects::nonNull)
                        .toList());
            }
        });

        return basicNavigation;
    }

    private NavigationNodeDto taiteenosaNode(TaiteenosaDto taiteenosa, Long taiteenalaViiteId) {
        if (taiteenosa.getNimi() == null) {
            return null;
        }
        return NavigationNodeDto.of(NavigationType.taiteenosa, taiteenosa.getNimi(), taiteenosa.getId())
                .meta("viiteId", taiteenalaViiteId);
    }

}
