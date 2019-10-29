package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.PerusteenSisalto;
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
import java.util.Set;

@Component
@Transactional
public class NavigationBuilderDefault implements NavigationBuilder {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteRepository perusteRepository;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Collections.emptySet();
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        Set<PerusteenSisalto> sisallot = peruste.getSisallot();

        NavigationNodeDto result = NavigationNodeDto.of(NavigationType.root);
        if (sisallot.size() > 0) {
            PerusteenOsaViite sisalto = sisallot.iterator().next().getSisalto();
            if (sisalto != null) {
                result.addAll(sisalto.constructNavigation(this.mapper));
            }
        }
        return result;
    }
}
