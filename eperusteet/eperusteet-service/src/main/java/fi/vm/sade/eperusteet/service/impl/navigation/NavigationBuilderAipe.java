package fi.vm.sade.eperusteet.service.impl.navigation;

import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.yl.AIPEKurssiDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOppiaineLaajaDto;
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
    private AIPEOpetuksenPerusteenSisaltoService aipeOpetuksenPerusteenSisaltoService;

    public List<NavigationNodeDto> buildNavigation(Long perusteId, String kieli) {
        return aipeOpetuksenPerusteenSisaltoService.getVaiheetKaikki(perusteId).stream()
                .map(vaihe -> NavigationNodeDto.of(NavigationType.aipevaihe, vaihe.getNimi() != null ? vaihe.getNimi().orElse(null) : null, vaihe.getId())
                        .addAll(oppiaineet(vaihe.getOppiaineet(), vaihe.getId(), kieli)))
                .collect(Collectors.toList());
    }

    private List<NavigationNodeDto> oppiaineet(List<AIPEOppiaineLaajaDto> oppiaineet, Long vaiheId, String kieli) {
        return oppiaineet.stream()
                .map(oppiaine -> NavigationNodeDto
                        .of(NavigationType.aipeoppiaine, oppiaine.getNimi() != null ? oppiaine.getNimi().orElse(null) : null, oppiaine.getId())
                        .meta("vaiheId", vaiheId)
                        .addAll(oppiaineet(oppiaine.getOppimaarat(), vaiheId, kieli))
                        .addAll(kurssit(oppiaine.getKurssit(), vaiheId, oppiaine.getId(), kieli)))
                .collect(Collectors.toList());
    }

    private List<NavigationNodeDto> kurssit(List<AIPEKurssiDto> kurssit, Long vaiheId, Long oppiaineId, String kieli) {
        return kurssit.stream()
                .map(kurssi -> NavigationNodeDto
                        .of(NavigationType.aipekurssi, kurssi.getNimi() != null ? kurssi.getNimi().orElse(null) : null, kurssi.getId())
                        .meta("vaiheId", vaiheId)
                        .meta("oppiaineId", oppiaineId)
                )
                .collect(Collectors.toList());
    }

}
