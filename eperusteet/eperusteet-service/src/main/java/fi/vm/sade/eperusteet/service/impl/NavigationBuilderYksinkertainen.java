package fi.vm.sade.eperusteet.service.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.PerusopetuksenPerusteenSisaltoService;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkaKokonaisuusService;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class NavigationBuilderYksinkertainen implements NavigationBuilder {

    @Autowired
    private PerusteDispatcher dispatcher;

    @Autowired
    private PerusteRepository perusteRepository;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.YKSINKERTAINEN);
    }

    @Autowired
    private NavigationBuilderAipe navigationBuilderAipe;

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli) {
        NavigationBuilder basicBuilder = dispatcher.get(NavigationBuilder.class);
        NavigationNodeDto basicNavigation = basicBuilder.buildNavigation(perusteId, kieli);

        Peruste peruste = perusteRepository.getOne(perusteId);

        if (peruste.getKoulutustyyppi() != null && KoulutusTyyppi.of(peruste.getKoulutustyyppi()).equals(KoulutusTyyppi.AIKUISTENPERUSOPETUS)) {
            return basicNavigation.addAll(navigationBuilderAipe.buildNavigation(perusteId, kieli));
        }

        return basicNavigation;
    }

}
