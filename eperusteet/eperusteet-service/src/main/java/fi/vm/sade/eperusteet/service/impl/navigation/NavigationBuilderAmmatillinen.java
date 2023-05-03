package fi.vm.sade.eperusteet.service.impl.navigation;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
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
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli) {
        NavigationNodeDto tekstit = dispatcher.get(NavigationBuilder.class).buildNavigation(perusteId, kieli);
        NavigationNodeDto tutkinnonMuodostuminen = tutkinnonMuodostuminen(tekstit);
        return NavigationNodeDto.of(NavigationType.root)
                .add(tutkinnonMuodostuminen)
                .add(tutkinnonOsat(perusteId))
                .addAll(tekstit.getChildren().stream()
                        .filter(node -> !node.getId().equals(tutkinnonMuodostuminen.getId())));
    }

    private NavigationNodeDto tutkinnonMuodostuminen(NavigationNodeDto tekstit) {
        return tekstit.getChildren().stream()
                .filter(teksti -> teksti.getType().equals(NavigationType.muodostuminen))
                .findFirst()
                .orElse(new NavigationNodeDto());
    }

    private NavigationNodeDto buildTutkinnonOsa(TutkinnonOsaViite tosa) {
        NavigationNodeDto result = NavigationNodeDto.of(
                NavigationType.tutkinnonosaviite,
                mapper.map(tosa.getTutkinnonOsa(), TutkinnonOsaDto.class).getNimi(),
                tosa.getId())
                .meta("koodi", mapper.map(tosa.getTutkinnonOsa().getKoodi(), KoodiDto.class))
                .meta("laajuus", tosa.getLaajuus());
        if (tosa.getTutkinnonOsa() != null && tosa.getTutkinnonOsa().getTyyppi() != TutkinnonOsaTyyppi.NORMAALI) {
            result.add(NavigationNodeDto.of(NavigationType.osaalueet)
                    .addAll(tosa.getTutkinnonOsa().getOsaAlueet().stream()
                    .map(osaAlue -> buildOsaAlue(tosa, osaAlue))
                .collect(Collectors.toList())));
        }
        return result;
    }

    private NavigationNodeDto buildOsaAlue(TutkinnonOsaViite tosa, OsaAlue osaAlue) {
        return NavigationNodeDto.of(
                NavigationType.osaalue,
                mapper.map(osaAlue.getNimi(), LokalisoituTekstiDto.class),
                osaAlue.getId())
                .meta("koodi", mapper.map(osaAlue.getKoodi(), KoodiDto.class))
                .meta("tutkinnonOsa", tosa.getTutkinnonOsa().getId())
                .meta("tutkinnonOsaViite", tosa.getId());
    }

    private NavigationNodeDto tutkinnonOsat(Long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        return NavigationNodeDto.of(NavigationType.tutkinnonosat, null, perusteId)
                .addAll(peruste.getSuoritustavat().stream()
                        .map(Suoritustapa::getTutkinnonOsat)
                        .flatMap(Collection::stream)
                        .map(this::buildTutkinnonOsa)
                        .collect(Collectors.toList()));
    }
}
