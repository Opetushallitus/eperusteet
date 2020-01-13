package fi.vm.sade.eperusteet.service.impl;

import com.google.common.base.Functions;
import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019ModuuliRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019OppiaineRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019SisaltoRepository;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.PerusteIdentifiable;
import fi.vm.sade.eperusteet.service.yl.Lops2019Service;
import fi.vm.sade.eperusteet.utils.dto.peruste.lops2019.tutkinnonrakenne.KoodiDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional
public class NavigationBuilderLops2019 implements NavigationBuilder {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private Lops2019SisaltoRepository lops2019SisaltoRepository;

    @Autowired
    private Lops2019OppiaineRepository lops2019OppiaineRepository;

    @Autowired
    private Lops2019ModuuliRepository lops2019ModuuliRepository;

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
                .map(lo -> NavigationNodeDto.of(
                        NavigationType.laajaalainen,
                        mapper.map(lo.getNimi(), LokalisoituTekstiDto.class),
                        lo.getId())
                    .meta("koodi", mapper.map(lo.getKoodi(), KoodiDto.class))));
    }

    private NavigationNodeDto mapOppiaine(
            Lops2019Oppiaine oa,
            Map<Lops2019Oppiaine, List<Lops2019Oppiaine>> oppimaaratMap,
            Map<Lops2019Oppiaine, List<Lops2019Moduuli>> moduulitMap) {
        NavigationNodeDto result = NavigationNodeDto
                .of(NavigationType.oppiaine, mapper.map(oa.getNimi(), LokalisoituTekstiDto.class), oa.getId())
                .meta("koodi", mapper.map(oa.getKoodi(), KoodiDto.class));

        Optional.ofNullable(oppimaaratMap.get(oa))
            .ifPresent(oppimaarat -> result.add(NavigationNodeDto.of(NavigationType.oppimaarat)
                .addAll(oppimaarat.stream().map(om -> mapOppiaine(om, oppimaaratMap, moduulitMap)))));

        Optional.ofNullable(moduulitMap.get(oa))
            .ifPresent(moduulit -> result.add(NavigationNodeDto.of(NavigationType.moduulit)
                .addAll(moduulit.stream()
                    .map(m -> NavigationNodeDto.of(
                        NavigationType.moduuli,
                        mapper.map(m.getNimi(), LokalisoituTekstiDto.class),
                        m.getId())
                    .meta("oppiaine", m.getOppiaine() != null ? m.getOppiaine().getId() : null)
                    .meta("koodi", mapper.map(m.getKoodi(), KoodiDto.class))
                    .meta("pakollinen", m.getPakollinen())))));

        return result;
    }

    private NavigationNodeDto oppiaineet(Long perusteId) {
        final Lops2019Sisalto sisalto = lops2019SisaltoRepository.findByPerusteId(perusteId);
        List<Lops2019Oppiaine> oppiaineet = sisalto.getOppiaineet();
        List<Lops2019Oppiaine> oppimaarat = new ArrayList<>();
        if (!ObjectUtils.isEmpty(oppiaineet)) {
            oppimaarat = lops2019OppiaineRepository.getOppimaaratByParents(oppiaineet);
        }

        List<Lops2019Oppiaine> kaikki = new ArrayList<>();
        kaikki.addAll(oppiaineet);
        kaikki.addAll(oppimaarat);

        Map<Lops2019Oppiaine, List<Lops2019Oppiaine>> oppimaaratMap = new HashMap<>();
        oppimaarat.forEach(om -> {
            if (!oppimaaratMap.containsKey(om.getOppiaine())) {
                oppimaaratMap.put(om.getOppiaine(), new ArrayList<>());
            }
            oppimaaratMap.get(om.getOppiaine()).add(om);
        });

        Map<Lops2019Oppiaine, List<Lops2019Moduuli>> moduulitMap = new HashMap<>();
        List<Lops2019Moduuli> moduulit = new ArrayList<>();
        if (!ObjectUtils.isEmpty(kaikki)) {
            moduulit = lops2019ModuuliRepository.getModuulitByParents(kaikki);
        }
        moduulit.forEach(m -> {
            if (!moduulitMap.containsKey(m.getOppiaine())) {
                moduulitMap.put(m.getOppiaine(), new ArrayList<>());
            }
            moduulitMap.get(m.getOppiaine()).add(m);
        });

        return NavigationNodeDto.of(NavigationType.oppiaineet)
            .addAll(oppiaineet.stream()
                .map(oa -> mapOppiaine(oa, oppimaaratMap, moduulitMap))
                .collect(Collectors.toList()));
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId) {
        return NavigationNodeDto.of(NavigationType.root)
            .addAll(dispatcher.get(NavigationBuilder.class).buildNavigation(perusteId).getChildren())
            .add(laajaAlaiset(perusteId))
            .add(oppiaineet(perusteId));
    }
}
