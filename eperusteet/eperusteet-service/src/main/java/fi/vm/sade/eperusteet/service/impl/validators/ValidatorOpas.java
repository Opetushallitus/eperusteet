package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.Validator;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Slf4j
public class ValidatorOpas implements Validator {

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Override
    public TilaUpdateStatus validate(Long perusteprojektiId, ProjektiTila targetTila) {
        TilaUpdateStatus updateStatus = new TilaUpdateStatus();

        Perusteprojekti projekti = perusteprojektiRepository.findOne(perusteprojektiId);

        if (ProjektiTila.JULKAISTU.equals(projekti.getTila())) {
            if (projekti.getPeruste().getKoulutustyyppi() == null
                    && projekti.getPeruste().getOppaanKoulutustyypit().isEmpty()) {
                updateStatus.setVaihtoOk(false);
                updateStatus.addStatus("oppaan-koulutustyyppi-pakollinen");
            }
            if (projekti.getPeruste().getNimi() == null
                    || projekti.getPeruste().getNimi().getTeksti().isEmpty()) {
                updateStatus.setVaihtoOk(false);
                updateStatus.addStatus("oppaan-nimi-pakollinen");
            }
            if (projekti.getPeruste().getVoimassaoloAlkaa() == null) {
                updateStatus.setVaihtoOk(false);
                updateStatus.addStatus("oppaan-voimassaolon-alku-pakollinen");
            }
            if (projekti.getPeruste().getVoimassaoloLoppuu() != null &&
                    projekti.getPeruste().getVoimassaoloLoppuu().before(DateTime.now().toDate())) {
                updateStatus.setVaihtoOk(false);
                updateStatus.addStatus("oppaan-voimassaolon-paattyminen-menneisyydessa");
            }
        }
        return updateStatus;
    }

    @Override
    public boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi) {
        return true;
    }

    @Override
    public boolean applicableToteutus(KoulutustyyppiToteutus toteutus) {
        return true;
    }

    @Override
    public boolean applicablePerustetyyppi(PerusteTyyppi tyyppi) {
        return PerusteTyyppi.OPAS.equals(tyyppi);
    }
}
