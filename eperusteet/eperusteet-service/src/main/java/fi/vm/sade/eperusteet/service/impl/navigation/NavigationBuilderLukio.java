package fi.vm.sade.eperusteet.service.impl.navigation;

import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukioOppiaineOppimaaraNodeDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukioOppiainePuuDto;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.LukiokoulutuksenPerusteenSisaltoService;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Component
@Transactional
public class NavigationBuilderLukio {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteDispatcher dispatcher;

    @Autowired
    private LukiokoulutuksenPerusteenSisaltoService lukiokoulutuksenPerusteenSisaltoService;

    public NavigationNodeDto buildNavigation(Long perusteId, NavigationNodeDto rootNode) {
        return NavigationNodeDto.of(NavigationType.root)
                .addAll(rootNode.getChildren().stream().filter(node -> !node.getType().equals(NavigationType.lukiorakenne)))
                .add(oppiaineet(perusteId));
    }

    private NavigationNodeDto oppiaineet(Long perusteId) {
        LukioOppiainePuuDto lukioOppiainePuuDto = lukiokoulutuksenPerusteenSisaltoService.getOppiaineTreeStructure(perusteId);
        return NavigationNodeDto.of(NavigationType.lukiooppiaineet_2015)
                .addAll(lukioOppiainePuuDto.getOppiaineet().stream()
                        .map(oa -> mapOppiaine(oa))
                        .collect(Collectors.toList()));
    }

    private NavigationNodeDto mapOppiaine(
            LukioOppiaineOppimaaraNodeDto oa) {
        NavigationNodeDto result = NavigationNodeDto
                .of(NavigationType.lukiooppiaine_2015, oa.getNimi(), oa.getId())
                .meta("koodi", KoodiDto.builder().arvo(oa.getKoodiArvo()).build());

        if (!CollectionUtils.isEmpty(oa.getOppimaarat())) {
            Optional.ofNullable(oa.getOppimaarat())
                    .ifPresent(oppimaarat -> result.add(NavigationNodeDto.of(NavigationType.lukiooppimaarat_2015).meta("navigation-subtype", true)
                            .addAll(oppimaarat.stream().map(om -> mapOppiaine(om)))));
        }

        if (!CollectionUtils.isEmpty(oa.getKurssit())) {
            Optional.ofNullable(oa.getKurssit())
                    .ifPresent(kurssit -> result.add(NavigationNodeDto.of(NavigationType.lukiokurssit).meta("navigation-subtype", true)
                            .addAll(kurssit.stream().map(kurssi -> NavigationNodeDto.
                                    of(NavigationType.lukiokurssi, kurssi.getNimi(), kurssi.getId())
                                    .meta("tyyppi", kurssi.getTyyppi())
                                    .meta("koodi", KoodiDto.builder().arvo(kurssi.getKoodiArvo()).build())
                                    .meta("oppiaine", oa.getId())
                            ))));
        }

        return result;
    }

}
