package fi.vm.sade.eperusteet.service.impl.navigationpublic;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineLaajaDto;
import fi.vm.sade.eperusteet.service.NavigationBuilderPublic;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.PerusteService;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Component
@Transactional
public class NavigationBuilderPublicPerusopetus implements NavigationBuilderPublic {

    @Autowired
    private PerusteDispatcher dispatcher;

    @Autowired
    private PerusteService perusteService;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.PERUSOPETUS);
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli, boolean esikatselu, Integer julkaisuRevisio) {
        PerusteKaikkiDto peruste = perusteService.getJulkaistuSisalto(perusteId, esikatselu);
        NavigationBuilderPublic basicBuilder = dispatcher.get(NavigationBuilderPublic.class);
        NavigationNodeDto basicNavigation = basicBuilder.buildNavigation(perusteId, kieli, esikatselu, julkaisuRevisio);
        return NavigationNodeDto.of(NavigationType.root)
                .addAll(basicNavigation.getChildren())
                .addAll(vuosiluokat(peruste, kieli))
                .add(oppiaineet(peruste, kieli));
    }

    private List<NavigationNodeDto> vuosiluokat(PerusteKaikkiDto peruste, String kieli) {
        return peruste.getPerusopetuksenPerusteenSisalto().getVuosiluokkakokonaisuudet().stream()
                .sorted(Comparator.comparing(vlk -> vlk.getVuosiluokat().iterator().next()))
                .map(vlk ->
                        NavigationNodeDto.of(NavigationType.vuosiluokkakokonaisuus, (vlk.getNimi() != null && vlk.getNimi().isPresent() ? vlk.getNimi().get() : null), vlk.getId()).addAll(vuosiluokanOppiaineet(peruste, vlk.getId(), kieli)))
                .collect(Collectors.toList());
    }

    private List<NavigationNodeDto> vuosiluokanOppiaineet(PerusteKaikkiDto peruste, Long vlkId, String kieli) {
        return peruste.getPerusopetuksenPerusteenSisalto().getOppiaineet().stream()
                .filter(oppiaine -> oppiaineJaOppimaaraVuosiluokkakokonaisuusIdt(oppiaine).contains(vlkId))
                .sorted(Comparator.comparing(oppiaine -> oppiaine.getNimiOrEmpty(kieli)))
                .sorted(Comparator.comparing(oppiaine -> oppiaine.getJnroOrDefault(99l)))
                .map(oppiaine ->{
                    Set<OppiaineDto> oppimaarat = Optional.ofNullable(oppiaine.getOppimaarat())
                            .orElse(Collections.emptySet())
                            .stream().filter(oppimaara -> oppimaara.getVuosiluokkakokonaisuudet().stream()
                            .anyMatch(vlk -> vlk.getVuosiluokkaKokonaisuus().get().getIdLong().equals(vlkId)))
                            .collect(Collectors.toSet());
                    return NavigationNodeDto
                            .of(NavigationType.perusopetusoppiaine, oppiaine.getNimi(), oppiaine.getId())
                            .meta("vlkId", vlkId)
                            .add(!ObjectUtils.isEmpty(oppimaarat) ? oppimaarat(oppimaarat, vlkId, kieli) : null);

                }).collect(Collectors.toList());
    }

    private List<Long> oppiaineJaOppimaaraVuosiluokkakokonaisuusIdt(OppiaineLaajaDto oppiaine) {
        return Stream.concat(
                oppiaine.getVuosiluokkakokonaisuudet() == null ? Stream.<Long>empty() : oppiaine.getVuosiluokkakokonaisuudet().stream()
                        .map(vlk -> vlk.getVuosiluokkaKokonaisuus().get().getIdLong()),
                oppiaine.getOppimaarat() == null ? Stream.empty() : oppiaine.getOppimaarat().stream()
                        .flatMap(oppimaara -> oppimaara.getVuosiluokkakokonaisuudet().stream()
                                .map(vlk -> vlk.getVuosiluokkaKokonaisuus().get().getIdLong())))
                .collect(Collectors.toList());
    }

    private NavigationNodeDto oppiaineet(PerusteKaikkiDto peruste, String kieli) {
        return NavigationNodeDto.of(NavigationType.perusopetusoppiaineet)
                .addAll(peruste.getPerusopetuksenPerusteenSisalto().getOppiaineet().stream()
                        .sorted(Comparator.comparing(oppiaine -> oppiaine.getNimiOrEmpty(kieli)))
                        .sorted(Comparator.comparing(oppiaine -> oppiaine.getJnroOrDefault(99l)))
                        .map(oppiaine ->
                        NavigationNodeDto.of(NavigationType.perusopetusoppiaine, oppiaine.getNimi(), oppiaine.getId())
                                .add(!ObjectUtils.isEmpty(oppiaine.getOppimaarat()) ? oppimaarat(oppiaine.getOppimaarat(), null, kieli) : null)
                ).collect(Collectors.toList()));
    }

    private NavigationNodeDto oppimaarat(Set<OppiaineDto> oppimaarat, Long vlkId, String kieli) {
        return NavigationNodeDto.of(NavigationType.oppimaarat).meta("navigation-subtype", true)
                .addAll(
                        oppimaarat.stream()
                                .sorted(Comparator.comparing(oppiaine -> oppiaine.getNimiOrEmpty(kieli)))
                                .sorted(Comparator.comparing(oppiaine -> oppiaine.getJnroOrDefault(99l)))
                                .map(oppimaara -> {
                                    NavigationNodeDto node = NavigationNodeDto
                                            .of(NavigationType.perusopetusoppiaine, oppimaara.getNimi(), oppimaara.getId());
                                    if (vlkId != null) {
                                        node = node.meta("vlkId", vlkId);
                                    }

                                    return node;
                                })
                                .collect(Collectors.toList()));
    }

}
