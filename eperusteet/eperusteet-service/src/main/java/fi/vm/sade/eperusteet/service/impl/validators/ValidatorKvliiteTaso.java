package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.ValidointiKategoria;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteTasoDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.Validator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import fi.vm.sade.eperusteet.service.util.Validointi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidatorKvliiteTaso implements Validator {

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteService perusteService;

    private final List<String> koodiTarkistus = Arrays.asList("nqf_", "eqf_", "isced2011koulutusastetaso1_");

    @Override
    public List<Validointi> validate(Long perusteprojektiId, ProjektiTila tila) {
        Peruste peruste = perusteRepository.findByPerusteprojektiId(perusteprojektiId);
        List<KVLiiteTasoDto> tasot = perusteService.haeTasot(peruste.getId(), peruste);
        List<Validointi> validoinnit = new ArrayList<>();

        List<String> koodiUrit = tasot.stream().map(taso -> taso.getCodeUri()).collect(Collectors.toList());

        boolean kaikkiLoyty = new ArrayList<>(koodiTarkistus).stream()
                .reduce("", (partialString, element) -> {
                    Optional<String> loytyi = koodiUrit.stream().filter(koodiUri -> koodiUri.startsWith(element)).findFirst();
                    return partialString + (loytyi.isPresent() ? "" : element);
                }).isEmpty();

        if (!kaikkiLoyty) {
            Validointi validointi = new Validointi(ValidointiKategoria.MAARITTELEMATON);
            validointi.huomautukset("kvliite-validointi-taso-koodi-puute", NavigationNodeDto.of(NavigationType.kvliite));
            validoinnit.add(validointi);
        }

        return validoinnit;
    }

    @Override
    public boolean applicableToteutus(KoulutustyyppiToteutus toteutus) {
        return true;
    }

    @Override
    public boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi) {
        return tyyppi != null && tyyppi.isAmmatillinen();
    }

    @Override
    public boolean applicablePerustetyyppi(PerusteTyyppi tyyppi) {
        return tyyppi.isOneOf(PerusteTyyppi.NORMAALI);
    }

    @Override
    public boolean applicableTila(ProjektiTila tila) {
        return tila.isOneOf(ProjektiTila.JULKAISTU);
    }
}
