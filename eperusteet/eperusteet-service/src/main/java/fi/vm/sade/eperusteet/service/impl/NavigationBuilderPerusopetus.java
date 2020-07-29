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
                .sorted(new VuosiluokkakokonaisuusComparator())
                .map(vlk ->
                        NavigationNodeDto.of(NavigationType.vuosiluokkakokonaisuus, (vlk.getNimi() != null && vlk.getNimi().isPresent() ? vlk.getNimi().get() : null), vlk.getId()).addAll(vuosiluokanOppiaineet(perusteId, vlk.getId(), kieli)))
                .collect(Collectors.toList());
    }

    private List<NavigationNodeDto> vuosiluokanOppiaineet(Long perusteId, Long vlkId, String kieli) {
        return vuosiluokkaKokonaisuusService.getOppiaineet(perusteId, vlkId).stream()
                .sorted(new OppiaineComparator(kieli))
                .map(oppiaine ->
                NavigationNodeDto.of(NavigationType.perusopetusoppiaine, oppiaine.getNimiOrDefault(null), oppiaine.getId()).meta("vlkId", vlkId)
                        .addAll(oppiaine.getOppimaarat() != null ? oppimaarat(oppiaine.getOppimaarat(), vlkId, kieli) : null)
        ).collect(Collectors.toList());
    }

    private NavigationNodeDto oppiaineet(Long perusteId, String kieli) {
        return NavigationNodeDto.of(NavigationType.perusopetusoppiaineet)
                .addAll(sisallot.getOppiaineet(perusteId, OppiaineSuppeaDto.class).stream()
                        .sorted(new OppiaineComparator(kieli))
                        .map(oppiaine ->
                        NavigationNodeDto.of(NavigationType.perusopetusoppiaine, oppiaine.getNimiOrDefault(null), oppiaine.getId())
                                .addAll(oppiaine.getOppimaarat() != null ? oppimaarat(oppiaine.getOppimaarat(), null, kieli) : null)
                ).collect(Collectors.toList()));
    }

    private List<NavigationNodeDto> oppimaarat(Set<OppiaineSuppeaDto> oppimaarat, Long vlkId, String kieli) {
        return oppimaarat.stream()
                .sorted(new OppiaineComparator(kieli))
                .map(oppimaara -> {
            NavigationNodeDto node = NavigationNodeDto.of(NavigationType.perusopetusoppiaine, oppimaara.getNimiOrDefault(null), oppimaara.getId());
            if (vlkId != null) {
                node = node.meta("vlkId", vlkId);
            }

            return node;
        })
                .collect(Collectors.toList());
    }

    class VuosiluokkakokonaisuusComparator implements Comparator<VuosiluokkaKokonaisuusDto> {

        @Override
        public int compare(VuosiluokkaKokonaisuusDto o1, VuosiluokkaKokonaisuusDto o2) {
            return o1.getVuosiluokat().iterator().next().compareTo(o2.getVuosiluokat().iterator().next());
        }
    }

    @AllArgsConstructor
    class OppiaineComparator implements Comparator<OppiaineSuppeaDto> {

        private String kieli;

        @Override
        public int compare(OppiaineSuppeaDto o1, OppiaineSuppeaDto o2) {

            if (o1.getJnroOrDefault(99l).compareTo(o2.getJnroOrDefault(99l)) == 0) {

                LokalisoituTekstiDto o1teksti = o1.getNimiOrDefault(LokalisoituTekstiDto.of(""));
                LokalisoituTekstiDto o2teksti = o2.getNimiOrDefault(LokalisoituTekstiDto.of(""));

                String o1nimi = LokalisoituTekstiDto.getOrDefault(o1teksti, Kieli.valueOf(kieli), "");
                String o2nimi = LokalisoituTekstiDto.getOrDefault(o2teksti, Kieli.valueOf(kieli), "");

                return o1nimi.compareTo(o2nimi);
            } else {
                return o1.getJnroOrDefault(99l).compareTo(o2.getJnroOrDefault(99l));
            }
        }
    }
}
