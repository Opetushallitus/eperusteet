package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEKurssiDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOppiaineLaajaDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.AIPEOpetuksenPerusteenSisaltoService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class NavigationBuilderAipe {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private AIPEOpetuksenPerusteenSisaltoService aipeOpetuksenPerusteenSisaltoService;

    public List<NavigationNodeDto> buildNavigation(Long perusteId, String kieli) {
        return aipeOpetuksenPerusteenSisaltoService.getVaiheetKaikki(perusteId).stream()
                .map(vaihe -> NavigationNodeDto.of(NavigationType.aipevaihe, vaihe.getNimi() != null ? vaihe.getNimi().orElseGet(() -> LokalisoituTekstiDto.of("tuntematon")) : LokalisoituTekstiDto.of("tuntematon"), vaihe.getId())
                    .addAll(oppiaineet(vaihe.getOppiaineet(), kieli)))
                .collect(Collectors.toList());
    }

    private List<NavigationNodeDto> oppiaineet(List<AIPEOppiaineLaajaDto> oppiaineet, String kieli) {
        return oppiaineet.stream()
                .map(oppiaine -> NavigationNodeDto.of(NavigationType.aipeoppiaine, oppiaine.getNimi() != null ? oppiaine.getNimi().orElseGet(() -> LokalisoituTekstiDto.of("tuntematon")) : LokalisoituTekstiDto.of("tuntematon"), oppiaine.getId())
                        .addAll(oppiaineet(oppiaine.getOppimaarat(), kieli))
                        .addAll(kurssit(oppiaine.getKurssit(), kieli)))
                .collect(Collectors.toList());

    }

    private List<NavigationNodeDto> kurssit(List<AIPEKurssiDto> kurssit, String kieli) {
        return kurssit.stream()
                .map(kurssi -> NavigationNodeDto.of(NavigationType.aipekurssi, kurssi.getNimi() != null ? kurssi.getNimi().orElseGet(() -> LokalisoituTekstiDto.of("tuntematon")) : LokalisoituTekstiDto.of("tuntematon"), kurssi.getId()))
                .collect(Collectors.toList());
    }

}
