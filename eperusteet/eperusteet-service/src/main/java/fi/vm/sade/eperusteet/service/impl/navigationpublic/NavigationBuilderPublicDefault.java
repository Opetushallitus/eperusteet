package fi.vm.sade.eperusteet.service.impl.navigationpublic;

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

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class NavigationBuilderPublicDefault implements NavigationBuilderPublic {

    @Autowired
    private PerusteService perusteService;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Collections.emptySet();
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

        if (sisallot.size() > 0) {
            PerusteenSisaltoDto sisalto = sisallot.iterator().next();
            if (sisalto != null) {
                PerusteenOsaViiteDto.Laaja sisaltoViite = sisalto.getSisalto();
                if (sisaltoViite != null) {
                    result.addAll(constructNavigation(sisaltoViite));
                }
            }
        }
        return result;
    }
}
