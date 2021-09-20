package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.PerusteenSisalto;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.liite.Liitteellinen;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenSisaltoDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.NavigationBuilderPublic;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
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
            if (po instanceof TekstiKappaleDto && ((TekstiKappaleDto) po).getLiite()) {
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
                .of(type, sisalto.getPerusteenOsa() != null ? sisalto.getPerusteenOsa().getNimi() : null, sisalto.getId())
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
