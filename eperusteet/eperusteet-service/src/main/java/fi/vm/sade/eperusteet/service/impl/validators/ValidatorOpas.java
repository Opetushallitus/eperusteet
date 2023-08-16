package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.ValidointiKategoria;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.Validator;
import fi.vm.sade.eperusteet.service.util.Validointi;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Transactional
@Slf4j
public class ValidatorOpas implements Validator {

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Override
    public List<Validointi> validate(Long perusteprojektiId, ProjektiTila targetTila) {
        List<Validointi> validoinnit = new ArrayList<>();

        Perusteprojekti projekti = perusteprojektiRepository.findOne(perusteprojektiId);
        Validointi validointi = new Validointi(ValidointiKategoria.PERUSTE);

        if (projekti.getPeruste().getVoimassaoloAlkaa() == null) {
            validointi.virhe("oppaan-voimassaolon-alku-pakollinen", NavigationNodeDto.of(NavigationType.tiedot));
        }

        if (ProjektiTila.JULKAISTU.equals(projekti.getTila())) {
            if (projekti.getPeruste().getKoulutustyyppi() == null
                    && projekti.getPeruste().getOppaanKoulutustyypit().isEmpty()) {
                validointi.virhe("oppaan-koulutustyyppi-pakollinen", NavigationNodeDto.of(NavigationType.tiedot));
            }
            boolean hasNimiKaikillaKielilla = projekti.getPeruste().getKielet().stream()
                    .allMatch(kieli -> projekti.getPeruste().getNimi().getTeksti() != null
                            && projekti.getPeruste().getNimi().getTeksti().containsKey(kieli));
            if (!hasNimiKaikillaKielilla) {
                validointi.virhe("oppaan-nimea-ei-ole-kaikilla-kielilla", NavigationNodeDto.of(NavigationType.tiedot));
            }

            if (projekti.getPeruste().getVoimassaoloLoppuu() != null &&
                    projekti.getPeruste().getVoimassaoloLoppuu().before(DateTime.now().toDate())) {
                validointi.virhe("oppaan-voimassaolon-paattyminen-menneisyydessa", NavigationNodeDto.of(NavigationType.tiedot));
            }
        }
        return validoinnit;
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
