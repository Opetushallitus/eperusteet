package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.Koulutus;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.ValidointiKategoria;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.Validator;
import fi.vm.sade.eperusteet.service.util.Validointi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class ValidatorPerusteHasKoulutuskoodi implements Validator {

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Override
    public List<Validointi> validate(Long perusteprojektiId, ProjektiTila tila) {
        Perusteprojekti projekti = perusteprojektiRepository.findById(perusteprojektiId).orElse(null);
        Set<Koulutus> koulutukset = projekti.getPeruste().getKoulutukset();
        if (koulutukset == null || koulutukset.isEmpty()) {
            Validointi validointi = new Validointi(ValidointiKategoria.PERUSTE);
            return Collections.singletonList(
                    validointi.virhe("koulutuskoodi-puuttuu", NavigationNodeDto.of(NavigationType.tiedot)));
        }
        return Collections.emptyList();
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
