package fi.vm.sade.eperusteet.service.impl.navigation;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.PerusopetuksenPerusteenSisaltoService;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkaKokonaisuusService;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Component
@Transactional
public class NavigationBuilderPerusopetus implements NavigationBuilder {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteDispatcher dispatcher;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusopetuksenPerusteenSisaltoService sisallot;

    @Autowired
    private VuosiluokkaKokonaisuusService vuosiluokkaKokonaisuusService;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.PERUSOPETUS);
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli) {
        NavigationBuilder basicBuilder = dispatcher.get(NavigationBuilder.class);
        NavigationNodeDto basicNavigation = basicBuilder.buildNavigation(perusteId, kieli);

        return NavigationNodeDto.of(NavigationType.root)
                .addAll(basicNavigation.getChildren())
                .add(vuosiluokkakokonaisuudet(perusteId, kieli))
                .add(oppiaineet(perusteId, kieli))
                .add(NavigationNodeDto.of(NavigationType.perusopetuslaajaalaisetosaamiset)
                        .addAll(laajaAlaisetOsaamiset(perusteId)));
    }

    private List<NavigationNodeDto> laajaAlaisetOsaamiset(Long perusteId) {
        return sisallot.getLaajaalaisetOsaamiset(perusteId).stream()
                .sorted(Comparator.comparing(LaajaalainenOsaaminenDto::getId))
                .map(lao ->
                        NavigationNodeDto.of(
                                NavigationType.perusopetuslaajaalainenosaaminen,
                                (lao.getNimi() != null && lao.getNimi().isPresent() ? lao.getNimi().get() : null),
                                lao.getId()))
                .collect(Collectors.toList());
    }

    private NavigationNodeDto vuosiluokkakokonaisuudet(Long perusteId, String kieli) {
        return NavigationNodeDto.of(NavigationType.vuosiluokkakokonaisuudet)
                .addAll(sisallot.getVuosiluokkaKokonaisuudet(perusteId).stream()
                .sorted(Comparator.comparing(vlk -> vlk.getVuosiluokat().iterator().next()))
                .map(vlk ->
                        NavigationNodeDto.of(
                                NavigationType.vuosiluokkakokonaisuus,
                                (vlk.getNimi() != null ? vlk.getNimi() : null),
                                vlk.getId()))
                .collect(Collectors.toList()));
    }

    private NavigationNodeDto oppiaineet(Long perusteId, String kieli) {
        return NavigationNodeDto.of(NavigationType.perusopetusoppiaineet)
                .addAll(sisallot.getOppiaineet(perusteId, OppiaineSuppeaDto.class).stream()
                        .sorted(Comparator.comparing(oppiaine -> oppiaine.getNimiOrEmpty(kieli)))
                        .sorted(Comparator.comparing(oppiaine -> oppiaine.getJnroOrDefault(99L)))
                        .map(oppiaine ->
                        NavigationNodeDto.of(NavigationType.perusopetusoppiaine, oppiaine.getNimi(), oppiaine.getId())
                                .addAll(!ObjectUtils.isEmpty(oppiaine.getOppimaarat()) ? oppimaarat(oppiaine.getOppimaarat(), kieli) : null)
                ).collect(Collectors.toList()));
    }

    private Stream<NavigationNodeDto> oppimaarat(Set<OppiaineSuppeaDto> oppimaarat, String kieli) {
        return Stream.concat(
                    Stream.of(NavigationNodeDto.of(NavigationType.oppimaarat).meta("navigation-subtype", true)),
                    oppimaarat.stream()
                            .sorted(Comparator.comparing(oppiaine -> oppiaine.getNimiOrEmpty(kieli)))
                            .sorted(Comparator.comparing(oppiaine -> oppiaine.getJnroOrDefault(99l)))
                            .map(oppimaara -> NavigationNodeDto.of(NavigationType.perusopetusoppiaine, oppimaara.getNimi(), oppimaara.getId()).meta("oppimaara", true)));
    }

}
