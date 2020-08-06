package fi.vm.sade.eperusteet.service.impl;

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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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

                navigationNodeDto.addAll(taiteenaladto.getOsaavainMap().keySet().stream().map(alaosa -> {
                    KevytTekstiKappaleDto tekstikappale = taiteenaladto.getOsaavainMap().get(alaosa);
                    if (tekstikappale != null) {
                        return NavigationNodeDto.of(NavigationType.taiteenosa, tekstikappale.getNimi()).meta("alaosa", alaosa);
                    } else {
                        return null;
                    }
                }).filter(Objects::nonNull));
            }
        });

        return basicNavigation;
    }

}
