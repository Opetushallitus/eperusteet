package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.service.Validator;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.ProjektiValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class ProjektiValidatorImpl implements ProjektiValidator {

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private List<Validator> validators;

    @Override
    public TilaUpdateStatus run(Long perusteprojektiId, ProjektiTila tila) {
        TilaUpdateStatus result = new TilaUpdateStatus();
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

            TilaUpdateStatus status = validator.validate(perusteprojektiId, tila);
            result.merge(status);
        }

        // Huomautukset viimeiseksi
        result.setInfot(result.getInfot().stream()
                .sorted(Comparator.comparing(TilaUpdateStatus.Status::getValidointiStatusType)
                        .reversed())
                .collect(Collectors.toList()));

        return result;
    }
}
