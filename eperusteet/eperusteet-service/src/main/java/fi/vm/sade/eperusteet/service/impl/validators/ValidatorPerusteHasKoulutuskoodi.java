package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.ValidointiKategoria;
import fi.vm.sade.eperusteet.service.Validator;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ValidatorPerusteHasKoulutuskoodi implements Validator {

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Override
    public TilaUpdateStatus validate(Long perusteprojektiId, ProjektiTila tila) {
        Perusteprojekti projekti = perusteprojektiRepository.findOne(perusteprojektiId);
        Set<Koulutus> koulutukset = projekti.getPeruste().getKoulutukset();
        if (koulutukset == null || koulutukset.isEmpty()) {
            TilaUpdateStatus status = new TilaUpdateStatus();
            status.setVaihtoOk(false);
            status.addStatus("koulutuskoodi-puuttuu", ValidointiKategoria.PERUSTE);
            return status;
        }
        return null;
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
    public boolean applicableTila(ProjektiTila tila) {
        return tila.isOneOf(ProjektiTila.JULKAISTU, ProjektiTila.VALMIS);
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
