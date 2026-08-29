package fi.vm.sade.eperusteet.service.impl.navigationpublic;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.yl.TaiteenalaDto;
import fi.vm.sade.eperusteet.dto.yl.TaiteenosaDto;
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

            if (viite.isPresent() && viite.get().getPerusteenOsa() instanceof TaiteenalaDto taiteenaladto) {
                navigationNodeDto.addAll(taiteenaladto.getTaiteenTekstiOsat().stream().map(tekstiosa -> {
                    if (tekstiosa.getNimi() != null) {
                        return NavigationNodeDto.of(NavigationType.taiteentekstiosa,
                                        tekstiosa.getNimi())
                                .meta("tekstiosa", tekstiosa.getTaiteenTekstiOsa())
                                .meta("viiteId", navigationNodeDto.getId());
                    } else {
                        return null;
                    }
                }).filter(Objects::nonNull));

                if (!CollectionUtils.isEmpty(taiteenaladto.getVapaatTekstit())) {
                    navigationNodeDto.addAll(taiteenaladto.getVapaatTekstit().stream().map(vapaateksti -> NavigationNodeDto.of(NavigationType.taiteentekstiosa,
                                    vapaateksti.getNimi())
                            .meta("vapaateksti_id", vapaateksti.getId())
                            .meta("viiteId", navigationNodeDto.getId())));
                }

                if (!CollectionUtils.isEmpty(taiteenaladto.getTaiteenOsat())) {
                    navigationNodeDto.addAll(taiteenaladto.getTaiteenOsat().stream()
                            .map(taiteenosa -> taiteenosaNode(taiteenosa, navigationNodeDto.getId()))
                            .filter(Objects::nonNull)
                            .toList());
                }
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
