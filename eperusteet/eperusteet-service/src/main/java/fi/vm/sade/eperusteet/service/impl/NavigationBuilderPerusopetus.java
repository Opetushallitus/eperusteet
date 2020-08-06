package fi.vm.sade.eperusteet.service.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.PerusopetuksenPerusteenSisaltoService;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkaKokonaisuusService;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
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
                .addAll(vuosiluokat(perusteId, kieli))
                .add(oppiaineet(perusteId, kieli));
    }

    private List<NavigationNodeDto> vuosiluokat(Long perusteId, String kieli) {
        return sisallot.getVuosiluokkaKokonaisuudet(perusteId).stream()
                .sorted(Comparator.comparing(vlk -> vlk.getVuosiluokat().iterator().next()))
                .map(vlk ->
                        NavigationNodeDto.of(NavigationType.vuosiluokkakokonaisuus, (vlk.getNimi() != null && vlk.getNimi().isPresent() ? vlk.getNimi().get() : null), vlk.getId()).addAll(vuosiluokanOppiaineet(perusteId, vlk.getId(), kieli)))
                .collect(Collectors.toList());
    }

    private List<NavigationNodeDto> vuosiluokanOppiaineet(Long perusteId, Long vlkId, String kieli) {
        return vuosiluokkaKokonaisuusService.getOppiaineet(perusteId, vlkId).stream()
                .sorted(Comparator.comparing(oppiaine -> LokalisoituTekstiDto.getOrDefault(oppiaine.getNimiOrDefault(LokalisoituTekstiDto.of("")), Kieli.of(kieli), "")))
                .sorted(Comparator.comparing(oppiaine -> oppiaine.getJnroOrDefault(99l)))
                .map(oppiaine ->
                NavigationNodeDto.of(NavigationType.perusopetusoppiaine, oppiaine.getNimiOrDefault(null), oppiaine.getId()).meta("vlkId", vlkId)
                        .add(!ObjectUtils.isEmpty(oppiaine.getOppimaarat()) ? oppimaarat(oppiaine.getOppimaarat(), vlkId, kieli) : null)
        ).collect(Collectors.toList());
    }

    private NavigationNodeDto oppiaineet(Long perusteId, String kieli) {
        return NavigationNodeDto.of(NavigationType.perusopetusoppiaineet)
                .addAll(sisallot.getOppiaineet(perusteId, OppiaineSuppeaDto.class).stream()
                        .sorted(Comparator.comparing(oppiaine -> LokalisoituTekstiDto.getOrDefault(oppiaine.getNimiOrDefault(LokalisoituTekstiDto.of("")), Kieli.of(kieli), "")))
                        .sorted(Comparator.comparing(oppiaine -> oppiaine.getJnroOrDefault(99l)))
                        .map(oppiaine ->
                        NavigationNodeDto.of(NavigationType.perusopetusoppiaine, oppiaine.getNimiOrDefault(null), oppiaine.getId())
                                .add(!ObjectUtils.isEmpty(oppiaine.getOppimaarat()) ? oppimaarat(oppiaine.getOppimaarat(), null, kieli) : null)
                ).collect(Collectors.toList()));
    }

    private NavigationNodeDto oppimaarat(Set<OppiaineSuppeaDto> oppimaarat, Long vlkId, String kieli) {
        return NavigationNodeDto.of(NavigationType.oppimaarat).meta("navigation-subtype", true)
                .addAll(
                        oppimaarat.stream()
                                .sorted(Comparator.comparing(oppiaine -> LokalisoituTekstiDto.getOrDefault(oppiaine.getNimiOrDefault(LokalisoituTekstiDto.of("")), Kieli.of(kieli), "")))
                                .sorted(Comparator.comparing(oppiaine -> oppiaine.getJnroOrDefault(99l)))
                                .map(oppimaara -> {
                                    NavigationNodeDto node = NavigationNodeDto
                                            .of(NavigationType.perusopetusoppiaine, oppimaara.getNimiOrDefault(null), oppimaara.getId());
                                    if (vlkId != null) {
                                        node = node.meta("vlkId", vlkId);
                                    }

                                    return node;
                                })
                                .collect(Collectors.toList()));
    }

}
