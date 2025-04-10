package fi.vm.sade.eperusteet.service.impl.navigationpublic;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.yl.TaiteenalaDto;
import fi.vm.sade.eperusteet.service.NavigationBuilderPublic;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.PerusteService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Component
@Transactional
public class NavigationBuilderPublicTaiteenPerusopetus implements NavigationBuilderPublic {

    @Autowired
    private PerusteDispatcher dispatcher;

    @Autowired
    private PerusteService perusteService;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.TPO);
    }


    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli, boolean esikatselu, Integer julkaisuRevisio) {
        PerusteKaikkiDto peruste = perusteService.getJulkaistuSisalto(perusteId, esikatselu);
        NavigationBuilderPublic basicBuilder = dispatcher.get(NavigationBuilderPublic.class);
        NavigationNodeDto basicNavigation = basicBuilder.buildNavigation(perusteId, kieli, esikatselu, julkaisuRevisio);

        List<PerusteenOsaViiteDto.Laaja> viitteet = getLapsiViitteet(peruste.getTpoOpetuksenSisalto().getSisalto().getLapset());

        basicNavigation.getChildren().forEach(navigationNodeDto -> {
            Optional<PerusteenOsaViiteDto.Laaja> viite = viitteet.stream().filter(filteredViite -> filteredViite.getId().equals(navigationNodeDto.getId())).findFirst();

            if (viite.isPresent() && viite.get().getPerusteenOsa() instanceof TaiteenalaDto) {
                TaiteenalaDto taiteenaladto = (TaiteenalaDto) viite.get().getPerusteenOsa();

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

                if (!CollectionUtils.isEmpty(taiteenaladto.getVapaatTekstit())) {
                    navigationNodeDto.addAll(taiteenaladto.getVapaatTekstit().stream().map(vapaateksti -> NavigationNodeDto.of(NavigationType.taiteenosa,
                                    vapaateksti.getNimi())
                            .meta("vapaateksti_id", vapaateksti.getId())
                            .meta("viiteId", navigationNodeDto.getId())));
                }
            }
        });

        return basicNavigation;
    }

    private List<PerusteenOsaViiteDto.Laaja> getLapsiViitteet(List<PerusteenOsaViiteDto.Laaja> viitteet) {
        List<PerusteenOsaViiteDto.Laaja> lapsiviitteet = new ArrayList<>();
        if (!CollectionUtils.isEmpty(viitteet)) {
            lapsiviitteet.addAll(viitteet);
            lapsiviitteet.addAll(viitteet.stream()
                    .map(lapsi -> getLapsiViitteet(lapsi.getLapset()))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
        }
        return viitteet;
    }

}
