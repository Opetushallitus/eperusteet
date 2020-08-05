package fi.vm.sade.eperusteet.service.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.yl.TaiteenalaDto;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
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

    @Autowired
    private PerusteenOsaService service;

    private final List<String> alaOsat = Arrays.asList("aikuistenOpetus", "kasvatus", "oppimisenArviointiOpetuksessa", "teemaopinnot", "tyotavatOpetuksessa", "yhteisetOpinnot") ;

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli) {
        NavigationBuilder basicBuilder = dispatcher.get(NavigationBuilder.class);
        NavigationNodeDto basicNavigation = basicBuilder.buildNavigation(perusteId, kieli);

        basicNavigation.getChildren().forEach(navigationNodeDto -> {
            TaiteenalaDto.Laaja taiteenaladto = service.getByViite(navigationNodeDto.getId());
            navigationNodeDto.addAll(alaOsat.stream().map(alaosa -> {
                try {
                    Field field =  taiteenaladto.getClass().getDeclaredField(alaosa);
                    field.setAccessible(true);
                    KevytTekstiKappaleDto tekstikappale = (KevytTekstiKappaleDto) field.get(taiteenaladto);
                    return NavigationNodeDto.of(NavigationType.taiteenosa, tekstikappale.getNimi()).meta("alaosa", alaosa);
                }catch(Exception e) {
                    return null;
                }
            }).filter(x -> x!=null));

        });

        return basicNavigation;
    }
}
