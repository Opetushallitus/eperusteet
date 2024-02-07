package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.ValidointiKategoria;
import fi.vm.sade.eperusteet.service.Validator;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.ProjektiValidator;
import fi.vm.sade.eperusteet.service.util.Validointi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@Slf4j
public class ProjektiValidatorImpl implements ProjektiValidator {

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private List<Validator> validators;

    @Override
    public List<Validointi> run(Long perusteprojektiId, ProjektiTila tila) {
        List<Validointi> validoinnit = new ArrayList<>();
        Perusteprojekti projekti = perusteprojektiRepository.findOne(perusteprojektiId);
        Peruste peruste = projekti.getPeruste();
        KoulutustyyppiToteutus toteutus = peruste.getToteutus();
        PerusteTyyppi tyyppi = peruste.getTyyppi();
        KoulutusTyyppi kt = peruste.getKoulutustyyppi() != null
            ? KoulutusTyyppi.of(peruste.getKoulutustyyppi())
            : null;

        for (Validator validator : validators) {
            if (!validator.applicableKoulutustyyppi(kt)) {
                continue;
            }

            if (toteutus != null && !validator.applicableToteutus(toteutus)) {
                continue;
            }

            if (!validator.applicablePerustetyyppi(tyyppi)) {
                continue;
            }

            if (!validator.applicableTila(tila)) {
                continue;
            }

            validoinnit.addAll(validator.validate(perusteprojektiId, tila));
        }

        Map<ValidointiKategoria, Validointi> validointiMap = new HashMap<>();
        validoinnit.stream()
                .filter(Objects::nonNull)
                .forEach(validointi -> {
            if (!validointiMap.containsKey(validointi.getKategoria())) {
                validointiMap.put(validointi.getKategoria(), validointi);
            } else {
                validointi.getVirheet().forEach(virhe -> validointiMap.get(validointi.getKategoria()).addVirhe(virhe));
                validointi.getHuomautukset().forEach(huomautus -> validointiMap.get(validointi.getKategoria()).addHuomautus(huomautus));
            }
        });

        return new ArrayList<>(validointiMap.values());
    }
}
