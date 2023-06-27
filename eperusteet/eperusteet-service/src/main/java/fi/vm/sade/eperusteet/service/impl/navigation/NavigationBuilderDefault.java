package fi.vm.sade.eperusteet.service.impl.navigation;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.liite.Liitteellinen;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional
public class NavigationBuilderDefault implements NavigationBuilder {

    private DtoMapper mapper;
    private PerusteRepository perusteRepository;

    @Autowired
    public NavigationBuilderDefault(@Dto DtoMapper mapper, PerusteRepository perusteRepository) {
        this.mapper = mapper;
        this.perusteRepository = perusteRepository;
    }

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Collections.emptySet();
    }

    private NavigationNodeDto constructNavigation(PerusteenOsaViite sisalto) {
        NavigationType type = NavigationType.viite;
        PerusteenOsa po = sisalto.getPerusteenOsa();
        if (po != null) {
            if (po instanceof Liitteellinen && ((Liitteellinen) po).isLiite()) {
                type = NavigationType.liite;
            } else if (po instanceof TekstiKappale) {
                TekstiKappale tk = (TekstiKappale) po;
                if (PerusteenOsaTunniste.RAKENNE.equals(tk.getTunniste())) {
                    type = NavigationType.muodostuminen;
                }
                if (sisalto.getPeruste() != null
                        && sisalto.getPeruste().getKoulutustyyppi() != null
                        && KoulutusTyyppi.of(sisalto.getPeruste().getKoulutustyyppi()).equals(KoulutusTyyppi.AIKUISTENPERUSOPETUS)
                        && PerusteenOsaTunniste.LAAJAALAINENOSAAMINEN.equals(tk.getTunniste())) {
                    type = NavigationType.aipe_laajaalaisetosaamiset;
                }
            } else {
                type = po.getNavigationType();
            }
        }

        NavigationNodeDto result = NavigationNodeDto
                .of(type, getPerusteenOsaNimi(mapper, sisalto.getPerusteenOsa()),
                        sisalto.getId())
                .addAll(sisalto.getLapset().stream()
                        .map(this::constructNavigation)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
        result.setId(sisalto.getId());
        return result;
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        Set<PerusteenSisalto> sisallot = peruste.getSisallot();
        NavigationNodeDto result = NavigationNodeDto.of(NavigationType.root);

        if (sisallot.size() > 0) {
            PerusteenSisalto sisalto = sisallot.iterator().next();
            if (sisalto != null) {
                PerusteenOsaViite sisaltoViite = sisalto.getSisalto();
                if (sisaltoViite != null) {
                    result.addAll(constructNavigation(sisaltoViite));
                }
            }
        }
        return result;
    }
}
