package fi.vm.sade.eperusteet.service.impl.navigationpublic;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueKokonaanDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteSuppeaDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.NavigationBuilderPublic;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.Collection;
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
        NavigationNodeDto result = NavigationNodeDto.of(
                NavigationType.tutkinnonosaviite,
                tosa.getNimi(),
                viite.getId())
                .meta("koodi", tosa.getKoodi())
                .meta("laajuus", viite.getLaajuus());
        return result;
    }

    private NavigationNodeDto tutkinnonOsat(PerusteKaikkiDto peruste) {
        return NavigationNodeDto.of(NavigationType.tutkinnonosat, null, peruste.getId())
                .addAll(peruste.getTutkinnonOsat().stream()
                        .map(tosa -> buildTutkinnonOsa(peruste.getSuoritustavat().stream()
                                .map(st -> st.getTutkinnonOsat().stream()
                                        .filter(viite -> viite.getTutkinnonOsa().getIdLong().equals(tosa.getId()))
                                        .findFirst().get()
                                ).findFirst().get(), tosa))
                        .collect(Collectors.toList()));
    }

}
