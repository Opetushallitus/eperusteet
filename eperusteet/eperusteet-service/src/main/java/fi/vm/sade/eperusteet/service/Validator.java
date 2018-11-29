package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface Validator {
    @PreAuthorize("isAuthenticated()")
    TilaUpdateStatus validate(Long perusteprojektiId, ProjektiTila targetTila);

    @PreAuthorize("permitAll()")
    default String getName() {
        return this.getClass().getSimpleName();
    }

    @PreAuthorize("isAuthenticated()")
    boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi);

    @PreAuthorize("isAuthenticated()")
    default boolean applicableTila(ProjektiTila tila) {
        return tila.isOneOf(ProjektiTila.JULKAISTU, ProjektiTila.VALMIS);
    }

    @PreAuthorize("isAuthenticated()")
    default boolean applicablePerustetyyppi(PerusteTyyppi tyyppi) {
        return PerusteTyyppi.NORMAALI.equals(tyyppi);
    }
}
