package fi.vm.sade.eperusteet.service.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.dto.peruste.KoodillinenDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019ModuuliRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019OppiaineRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019SisaltoRepository;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional
public class NavigationBuilderLops2019 implements NavigationBuilder {

    private final DtoMapper mapper;
    private final Lops2019SisaltoRepository lops2019SisaltoRepository;
    private final Lops2019OppiaineRepository lops2019OppiaineRepository;
    private final Lops2019ModuuliRepository lops2019ModuuliRepository;
    private final PerusteDispatcher dispatcher;

    @Autowired
    public NavigationBuilderLops2019(@Dto DtoMapper mapper,
                                     Lops2019SisaltoRepository lops2019SisaltoRepository,
                                     Lops2019OppiaineRepository lops2019OppiaineRepository,
                                     Lops2019ModuuliRepository lops2019ModuuliRepository,
                                     PerusteDispatcher dispatcher) {
        this.mapper = mapper;
        this.lops2019SisaltoRepository = lops2019SisaltoRepository;
        this.lops2019OppiaineRepository = lops2019OppiaineRepository;
        this.lops2019ModuuliRepository = lops2019ModuuliRepository;
        this.dispatcher = dispatcher;
    }

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.LOPS2019);
    }

    public Pair<String, Integer> getIdentity(String arvo) {
        if (!StringUtils.isEmpty(arvo)) {
            int idx = arvo.length();
            while (idx > 1 && arvo.charAt(idx - 1) >= '0' && arvo.charAt(idx - 1) <= '9') {
                --idx;
            }

            if (idx != arvo.length()) {
                String a = idx > 0 ? arvo.substring(0, idx) : "";
                Integer v = Integer.valueOf(arvo.substring(idx));
                return Pair.of(a, v);
            }
            else {
                return Pair.of(arvo, 0);
            }
        }
        return Pair.of("", Integer.MAX_VALUE);
    }

    private int koodiComparator(KoodillinenDto a, KoodillinenDto b) {
        if (a == null || a.getKoodi() == null || a.getKoodi().getArvo() == null) {
            return -1;
        }
        else if (b == null || b.getKoodi() == null || b.getKoodi().getArvo() == null) {
            return 1;
        }
        else {
            Pair<String, Integer> ia = getIdentity(a.getKoodi().getArvo());
            Pair<String, Integer> ib = getIdentity(b.getKoodi().getArvo());
            int nimiCmp = ia.getFirst().compareTo(ib.getFirst());
            if (nimiCmp != 0) {
                return nimiCmp;
            }
            return ia.getSecond().compareTo(ib.getSecond());
        }
    }

    private NavigationNodeDto laajaAlaiset(Long perusteId) {
        final Lops2019Sisalto sisalto = lops2019SisaltoRepository.findByPerusteId(perusteId);
        return NavigationNodeDto.of(NavigationType.laajaalaiset)
                .addAll(sisalto.getLaajaAlainenOsaaminen().getLaajaAlaisetOsaamiset().stream()
                        .map(lo -> NavigationNodeDto.of(
                                NavigationType.laajaalainen,
                                mapper.map(lo.getNimi(), LokalisoituTekstiDto.class),
                                lo.getId())
                                .koodi(mapper.map(lo.getKoodi(), KoodiDto.class))
                                .meta("koodi", mapper.map(lo.getKoodi(), KoodiDto.class)))
                        .sorted(this::koodiComparator));
    }

    private NavigationNodeDto mapOppiaine(
            Lops2019Oppiaine oa,
            Map<Long, List<Lops2019Oppiaine>> oppimaaratMap,
            Map<Long, List<Lops2019Moduuli>> moduulitMap) {
        NavigationNodeDto result = NavigationNodeDto
                .of(NavigationType.oppiaine, mapper.map(oa.getNimi(), LokalisoituTekstiDto.class), oa.getId())
                .koodi(mapper.map(oa.getKoodi(), KoodiDto.class))
                .meta("koodi", mapper.map(oa.getKoodi(), KoodiDto.class));

        Optional.ofNullable(oppimaaratMap.get(oa.getId()))
            .ifPresent(oppimaarat -> result.add(NavigationNodeDto.of(NavigationType.oppimaarat)
                .addAll(oppimaarat.stream().map(om -> mapOppiaine(om, oppimaaratMap, moduulitMap)))));

        Optional.ofNullable(moduulitMap.get(oa.getId()))
            .ifPresent(moduulit -> result.add(NavigationNodeDto.of(NavigationType.moduulit)
                .addAll(moduulit.stream()
                    .map(m -> NavigationNodeDto.of(
                        NavigationType.moduuli,
                        mapper.map(m.getNimi(), LokalisoituTekstiDto.class),
                        m.getId())
                        .koodi(mapper.map(m.getKoodi(), fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto.class))
                        .meta("oppiaine", m.getOppiaine() != null ? m.getOppiaine().getId() : null)
                        .meta("koodi", mapper.map(m.getKoodi(), KoodiDto.class))
                        .meta("pakollinen", m.getPakollinen()))
                    .sorted(this::koodiComparator))));

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

        Map<Long, List<Lops2019Oppiaine>> oppimaaratMap = new HashMap<>();
        oppimaarat.forEach(om -> {
            if (!oppimaaratMap.containsKey(om.getOppiaine().getId())) {
                oppimaaratMap.put(om.getOppiaine().getId(), new ArrayList<>());
            }
            oppimaaratMap.get(om.getOppiaine().getId()).add(om);
        });

        Map<Long, List<Lops2019Moduuli>> moduulitMap = new HashMap<>();
        List<Lops2019Moduuli> moduulit = new ArrayList<>();
        if (!ObjectUtils.isEmpty(kaikki)) {
            moduulit = lops2019ModuuliRepository.getModuulitByParents(kaikki);
        }
        moduulit.forEach(m -> {
            if (!moduulitMap.containsKey(m.getOppiaine().getId())) {
                moduulitMap.put(m.getOppiaine().getId(), new ArrayList<>());
            }
            moduulitMap.get(m.getOppiaine().getId()).add(m);
        });

        return NavigationNodeDto.of(NavigationType.oppiaineet)
            .addAll(oppiaineet.stream()
                .map(oa -> mapOppiaine(oa, oppimaaratMap, moduulitMap))
                .sorted(this::koodiComparator)
                .collect(Collectors.toList()));
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli) {
        NavigationBuilder basicBuilder = dispatcher.get(NavigationBuilder.class);
        NavigationNodeDto basicNavigation = basicBuilder.buildNavigation(perusteId, kieli);
        return NavigationNodeDto.of(NavigationType.root)
            .addAll(basicNavigation.getChildren())
            .add(laajaAlaiset(perusteId))
            .add(oppiaineet(perusteId));
    }
}
