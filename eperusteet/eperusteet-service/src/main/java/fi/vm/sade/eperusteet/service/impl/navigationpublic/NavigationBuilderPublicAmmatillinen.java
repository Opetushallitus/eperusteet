package fi.vm.sade.eperusteet.service.impl.navigationpublic;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteSuppeaDto;
import fi.vm.sade.eperusteet.service.NavigationBuilderPublic;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.PerusteService;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class NavigationBuilderPublicAmmatillinen implements NavigationBuilderPublic {

    @Autowired
    private PerusteDispatcher dispatcher;

    @Autowired
    private PerusteService perusteService;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.AMMATILLINEN);
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli) {
        PerusteKaikkiDto peruste = perusteService.getJulkaistuSisalto(perusteId);
        NavigationNodeDto tekstit = dispatcher.get(NavigationBuilderPublic.class).buildNavigation(perusteId, kieli);
        return NavigationNodeDto.of(NavigationType.root)
                .add(tutkinnonOsat(peruste))
                .addAll(tekstit.getChildren());
    }

    private NavigationNodeDto buildTutkinnonOsa(TutkinnonOsaViiteSuppeaDto viite, TutkinnonOsaKaikkiDto tosa) {
        if (viite == null) {
            return null;
        }

        NavigationNodeDto result = NavigationNodeDto.of(
                NavigationType.tutkinnonosaviite,
                tosa.getNimi(),
                viite.getId())
                .meta("koodi", tosa.getKoodi())
                .meta("laajuus", viite.getLaajuus());
        return result;
    }

    private NavigationNodeDto tutkinnonOsat(PerusteKaikkiDto peruste) {
        return NavigationNodeDto.of(
                KoulutusTyyppi.of(peruste.getKoulutustyyppi()).isValmaTelma() ? NavigationType.koulutuksenosat : NavigationType.tutkinnonosat,
                null,
                peruste.getId())
                .addAll(peruste.getTutkinnonOsat().stream()
                        .map(tosa -> buildTutkinnonOsa(
                                peruste.getSuoritustavat().stream()
                                        .flatMap(st -> st.getTutkinnonOsat().stream())
                                        .filter(viite -> viite.getTutkinnonOsa().getIdLong().equals(tosa.getId()))
                                        .findFirst().orElse(null), tosa))
                        .filter(t -> t != null)
                        .collect(Collectors.toList()));
    }

}
