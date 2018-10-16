package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.service.Validator;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ValidatorPerusteHasKoulutuskoodi implements Validator {

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Override
    public TilaUpdateStatus validate(Long perusteprojektiId) {
        Perusteprojekti projekti = perusteprojektiRepository.findOne(perusteprojektiId);
        Set<Koulutus> koulutukset = projekti.getPeruste().getKoulutukset();
        if (koulutukset == null || koulutukset.isEmpty()) {
            TilaUpdateStatus status = new TilaUpdateStatus();
            status.setVaihtoOk(false);
            status.addStatus("koulutuskoodi-puuttuu");
            return status;
        }
        return null;
    }

    @Override
    public boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi) {
        return tyyppi != null && tyyppi.isAmmatillinen();
    }

    @Override
    public boolean applicableTila(ProjektiTila tila) {
        return tila.isOneOf(ProjektiTila.JULKAISTU, ProjektiTila.VIIMEISTELY);
    }

    @Override
    public boolean applicablePerustetyyppi(PerusteTyyppi tyyppi) {
        return tyyppi.isOneOf(PerusteTyyppi.NORMAALI);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
