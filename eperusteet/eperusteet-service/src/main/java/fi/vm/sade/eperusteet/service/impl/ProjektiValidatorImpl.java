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

import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
public class ProjektiValidatorImpl implements ProjektiValidator {

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private List<Validator> validators;

    public TilaUpdateStatus run(Long perusteprojektiId, ProjektiTila tila) {
        TilaUpdateStatus result = new TilaUpdateStatus();
        Perusteprojekti projekti = perusteprojektiRepository.findOne(perusteprojektiId);
        Peruste peruste = projekti.getPeruste();
        PerusteTyyppi tyyppi = peruste.getTyyppi();
        KoulutusTyyppi kt = peruste.getKoulutustyyppi() != null
            ? KoulutusTyyppi.of(peruste.getKoulutustyyppi())
            : null;

        for (Validator validator : validators) {
            if (!validator.applicableKoulutustyyppi(kt)) {
                continue;
            }

            if (!validator.applicablePerustetyyppi(tyyppi)) {
                continue;
            }

            if (validator.applicableTila(tila)) {
                continue;
            }

            TilaUpdateStatus status = validator.validate(perusteprojektiId);
            result.merge(status);
        }
        return result;
    }

}
