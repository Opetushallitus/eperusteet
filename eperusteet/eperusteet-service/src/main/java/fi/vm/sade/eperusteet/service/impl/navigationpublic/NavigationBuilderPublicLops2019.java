package fi.vm.sade.eperusteet.service.impl.navigationpublic;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.dto.lops2019.Lops2019OppiaineKaikkiDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto;
import fi.vm.sade.eperusteet.dto.peruste.KoodillinenDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.NavigationBuilderPublic;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Component
@Transactional
public class NavigationBuilderPublicLops2019 implements NavigationBuilderPublic {

    @Autowired
    private PerusteDispatcher dispatcher;

    @Autowired
    private PerusteService perusteService;

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

    private NavigationNodeDto laajaAlaiset(PerusteKaikkiDto peruste) {
        return NavigationNodeDto.of(NavigationType.laajaalaiset)
                .addAll(peruste.getLops2019Sisalto().getLaajaAlainenOsaaminen().getLaajaAlaisetOsaamiset().stream()
                        .map(lo -> NavigationNodeDto.of(
                                NavigationType.laajaalainen,
                                lo.getNimi(),
                                lo.getId())
                                .koodi(lo.getKoodi())
                                .meta("koodi", lo.getKoodi()))
                        .sorted(this::koodiComparator));
    }

    private NavigationNodeDto mapOppiaine(
            Lops2019OppiaineKaikkiDto oa,
            Map<Long, List<Lops2019OppiaineKaikkiDto>> oppimaaratMap,
            Map<Long, List<Lops2019ModuuliDto>> moduulitMap) {
        NavigationNodeDto result = NavigationNodeDto
                .of(NavigationType.oppiaine, oa.getNimi(), oa.getId())
                .koodi(oa.getKoodi())
                .meta("koodi", oa.getKoodi());

        Optional.ofNullable(oppimaaratMap.get(oa.getId()))
                .ifPresent(oppimaarat -> result.add(NavigationNodeDto.of(NavigationType.oppimaarat).meta("navigation-subtype", true)
                .addAll(oppimaarat.stream().map(om -> mapOppiaine(om, oppimaaratMap, moduulitMap)))));

        Optional.ofNullable(moduulitMap.get(oa.getId()))
                .ifPresent(moduulit -> result.add(NavigationNodeDto.of(NavigationType.moduulit).meta("navigation-subtype", true)
                .addAll(moduulit.stream()
                    .map(m -> NavigationNodeDto.of(
                        NavigationType.moduuli,
                            m.getNimi(),
                        m.getId())
                        .koodi(m.getKoodi())
                        .meta("oppiaine", m.getOppiaine() != null ? m.getOppiaine().getIdLong() : null)
                        .meta("koodi", m.getKoodi())
                        .meta("pakollinen", m.getPakollinen()))
                    .sorted(this::koodiComparator))));

        return result;
    }

    private NavigationNodeDto oppiaineet(PerusteKaikkiDto peruste) {
        List<Lops2019OppiaineKaikkiDto> oppiaineet = peruste.getLops2019Sisalto().getOppiaineet();
        List<Lops2019OppiaineKaikkiDto> oppimaarat = oppiaineet.stream()
                .map(oppiaine -> oppiaine.getOppimaarat())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<Lops2019OppiaineKaikkiDto> kaikki = new ArrayList<>();
        kaikki.addAll(oppiaineet);
        kaikki.addAll(oppimaarat);

        Map<Long, List<Lops2019OppiaineKaikkiDto>> oppimaaratMap = new HashMap<>();
        oppimaarat.forEach(om -> {
            if (!oppimaaratMap.containsKey(om.getOppiaine().getIdLong())) {
                oppimaaratMap.put(om.getOppiaine().getIdLong(), new ArrayList<>());
            }
            oppimaaratMap.get(om.getOppiaine().getIdLong()).add(om);
        });

        Map<Long, List<Lops2019ModuuliDto>> moduulitMap = new HashMap<>();
        List<Lops2019ModuuliDto> moduulit = new ArrayList<>();
        if (!ObjectUtils.isEmpty(kaikki)) {
            moduulit = kaikki.stream()
                    .map(oppiaine -> oppiaine.getModuulit())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        moduulit.forEach(m -> {
            if (!moduulitMap.containsKey(m.getOppiaine().getIdLong())) {
                moduulitMap.put(m.getOppiaine().getIdLong(), new ArrayList<>());
            }
            moduulitMap.get(m.getOppiaine().getIdLong()).add(m);
        });

        return NavigationNodeDto.of(NavigationType.oppiaineet)
            .addAll(oppiaineet.stream()
                .map(oa -> mapOppiaine(oa, oppimaaratMap, moduulitMap))
                .sorted(this::koodiComparator)
                .collect(Collectors.toList()));
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli) {
        PerusteKaikkiDto peruste = perusteService.getJulkaistuSisalto(perusteId);
        NavigationBuilder basicBuilder = dispatcher.get(NavigationBuilder.class);
        NavigationNodeDto basicNavigation = basicBuilder.buildNavigation(perusteId, kieli);
        return NavigationNodeDto.of(NavigationType.root)
            .addAll(basicNavigation.getChildren())
            .add(laajaAlaiset(peruste))
            .add(oppiaineet(peruste));
    }
}
