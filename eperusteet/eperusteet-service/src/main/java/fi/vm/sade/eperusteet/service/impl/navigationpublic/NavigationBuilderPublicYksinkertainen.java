package fi.vm.sade.eperusteet.service.impl.navigationpublic;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.service.NavigationBuilderPublic;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.PerusteService;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import fi.vm.sade.eperusteet.utils.CollectionUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class NavigationBuilderPublicYksinkertainen implements NavigationBuilderPublic {

    @Autowired
    private PerusteDispatcher dispatcher;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.YKSINKERTAINEN);
    }

    @Autowired
    private NavigationBuilderPublicAipe navigationBuilderAipe;

    @Autowired
    private NavigationBuilderPublicLukio navigationBuilderLukio;

    @Autowired
    private PerusteService perusteService;

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli, boolean esikatselu, Integer julkaisuRevisio) {
        PerusteKaikkiDto peruste = perusteService.getJulkaistuSisalto(perusteId, esikatselu);
        NavigationBuilderPublic basicBuilder = dispatcher.get(NavigationBuilderPublic.class);
        NavigationNodeDto basicNavigation = basicBuilder.buildNavigation(perusteId, kieli, esikatselu, julkaisuRevisio);

        if (peruste.getKoulutustyyppi() != null && KoulutusTyyppi.of(peruste.getKoulutustyyppi()).equals(KoulutusTyyppi.AIKUISTENPERUSOPETUS)) {
            return basicNavigation.addAll(navigationBuilderAipe.buildNavigation(perusteId, kieli, esikatselu));
        }

        if (peruste.getKoulutustyyppi() != null && KoulutusTyyppi.of(peruste.getKoulutustyyppi()).equals(KoulutusTyyppi.LUKIOVALMISTAVAKOULUTUS)) {
            return navigationBuilderLukio.buildNavigation(perusteId, basicNavigation, esikatselu);
        }

        return asetaNumerointi(basicNavigation);
    }

    private NavigationNodeDto asetaNumerointi(NavigationNodeDto node) {
        asetaNumerointi(node.getChildren(), "");
        return node;
    }

    private void asetaNumerointi(List<NavigationNodeDto> nodes, String taso) {
        AtomicInteger nro = new AtomicInteger(0);
        nodes.stream()
                .filter(node -> !node.getType().equals(NavigationType.liite))
                .forEach(node -> {
            node.meta("numerointi", taso + nro.incrementAndGet());
            asetaNumerointi(node.getChildren(), taso + nro.get() + ".");
        });
    }

}
