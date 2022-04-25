package fi.vm.sade.eperusteet.service.impl.navigationpublic;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenSisaltoDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.service.NavigationBuilderPublic;
import fi.vm.sade.eperusteet.service.PerusteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class NavigationBuilderPublicLinkit implements NavigationBuilderPublic {

    private final PerusteService perusteService;

    @Autowired
    public NavigationBuilderPublicLinkit(PerusteService perusteService) {
        this.perusteService = perusteService;
    }

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.VAPAASIVISTYSTYO);
    }

    private NavigationNodeDto constructNavigation(PerusteenOsaViiteDto.Laaja sisalto) {
        NavigationType type = NavigationType.viite;
        PerusteenOsaDto.Laaja po = sisalto.getPerusteenOsa();
        if (po != null) {
            if (po instanceof TekstiKappaleDto && ((TekstiKappaleDto) po).getLiite() != null && ((TekstiKappaleDto) po).getLiite()) {
                type = NavigationType.liite;
            } else if (po instanceof TekstiKappaleDto) {
                TekstiKappaleDto tk = (TekstiKappaleDto) po;
                if (PerusteenOsaTunniste.RAKENNE.equals(tk.getTunniste())) {
                    type = NavigationType.muodostuminen;
                }
            } else {
                type = po.getNavigationType();
            }
        }

        NavigationNodeDto result = NavigationNodeDto
                .of(type, getPerusteenOsaNimi(sisalto.getPerusteenOsa()), sisalto.getId())
                .addAll(sisalto.getLapset().stream()
                        .map(this::constructNavigation)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
        result.setId(sisalto.getId());
        return result;
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli) {
        PerusteKaikkiDto peruste = perusteService.getJulkaistuSisalto(perusteId);

        Set<PerusteenSisaltoDto> sisallot = peruste.getSisallot();
        NavigationNodeDto result = NavigationNodeDto.of(NavigationType.root);

        if (sisallot.isEmpty()) {
            return result;
        }

        PerusteenSisaltoDto sisalto = sisallot.iterator().next();
        if (sisalto == null) {
            return result;
        }

        PerusteenOsaViiteDto.Laaja sisaltoViite = sisalto.getSisalto();
        if (sisaltoViite != null) {
            result.addAll(constructNavigation(sisaltoViite));
        }

        return result;
    }
}
