package fi.vm.sade.eperusteet.service.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019SisaltoRepository;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.PerusteIdentifiable;
import fi.vm.sade.eperusteet.service.yl.Lops2019Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@Transactional
public class NavigationBuilderLops2019 implements NavigationBuilder {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private Lops2019SisaltoRepository lops2019SisaltoRepository;

    @Autowired
    private PerusteDispatcher dispatcher;

    @Autowired
    private PerusteRepository perusteRepository;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.LOPS2019);
    }

    private NavigationNodeDto laajaAlaiset(Long perusteId) {
        final Lops2019Sisalto sisalto = lops2019SisaltoRepository.findByPerusteId(perusteId);
        return NavigationNodeDto.of(NavigationType.laajaalaiset)
                .addAll(sisalto.getLaajaAlainenOsaaminen().getLaajaAlaisetOsaamiset().stream()
                        .map(oa -> oa.constructNavigation(mapper)));
    }

    private NavigationNodeDto oppiaineet(Long perusteId) {
        final Lops2019Sisalto sisalto = lops2019SisaltoRepository.findByPerusteId(perusteId);
        return NavigationNodeDto.of(NavigationType.oppiaineet)
                .addAll(sisalto.getOppiaineet().stream()
                    .map(oa -> oa.constructNavigation(mapper)));
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId) {
        NavigationNodeDto root = NavigationNodeDto.of(NavigationType.root)
            .addAll(dispatcher.get(NavigationBuilder.class).buildNavigation(perusteId).getChildren())
            .add(laajaAlaiset(perusteId))
            .add(oppiaineet(perusteId));
        return root;
    }
}
