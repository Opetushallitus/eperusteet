package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.service.util.Validointi;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface Validator {
    @PreAuthorize("isAuthenticated()")
    List<Validointi> validate(Long perusteprojektiId, ProjektiTila targetTila);

    @PreAuthorize("permitAll()")
    default String getName() {
        return this.getClass().getSimpleName();
    }

    @PreAuthorize("isAuthenticated()")
    boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi);

    @PreAuthorize("isAuthenticated()")
    boolean applicableToteutus(KoulutustyyppiToteutus toteutus);

    @PreAuthorize("isAuthenticated()")
    default boolean applicableTila(ProjektiTila tila) {
        return tila.isOneOf(ProjektiTila.JULKAISTU, ProjektiTila.VALMIS);
    }

    @PreAuthorize("isAuthenticated()")
    default boolean applicablePerustetyyppi(PerusteTyyppi tyyppi) {
        return PerusteTyyppi.NORMAALI.equals(tyyppi);
    }

    default boolean isDiaariValid(Diaarinumero diaarinumero) {
        return true;
    }
}
