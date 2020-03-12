package fi.vm.sade.eperusteet.service.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenSisalto;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.dto.LokalisointiDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019ModuuliRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019OppiaineRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019SisaltoRepository;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.TutkinnonOsaViiteService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional
public class NavigationBuilderAmmatillinen implements NavigationBuilder {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteDispatcher dispatcher;

    @Autowired
    private PerusteRepository perusteRepository;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.AMMATILLINEN);
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId) {
        NavigationNodeDto tekstit = dispatcher.get(NavigationBuilder.class).buildNavigation(perusteId);
        return NavigationNodeDto.of(NavigationType.root)
                .addAll(tekstit.getChildren())
//                .add(tutkinnonMuodostuminen(perusteId))
                .add(tutkinnonOsat(perusteId));
    }

    private NavigationNodeDto tutkinnonOsat(Long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        return NavigationNodeDto.of(NavigationType.tutkinnonosat, null, perusteId)
                .addAll(peruste.getSuoritustavat().stream()
                        .map(Suoritustapa::getTutkinnonOsat)
                        .flatMap(Collection::stream)
                        .map(tosa -> NavigationNodeDto.of(
                                NavigationType.tutkinnonosaviite,
                                mapper.map(tosa.getTutkinnonOsa().getNimi(), LokalisoituTekstiDto.class),
                                tosa.getId())
                                .meta("koodi", mapper.map(tosa.getTutkinnonOsa().getKoodi(), KoodiDto.class))
                                .meta("laajuus", tosa.getLaajuus()))
                        .collect(Collectors.toList()));
    }

    private NavigationNodeDto tutkinnonMuodostuminen(Long perusteId) {
        return NavigationNodeDto.of(NavigationType.muodostuminen, null, perusteId);
    }

}
