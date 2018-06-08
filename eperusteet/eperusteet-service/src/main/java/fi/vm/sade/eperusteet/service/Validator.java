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
    TilaUpdateStatus validate(Long perusteprojektiId);

    @PreAuthorize("isAuthenticated()")
    String toString();

    @PreAuthorize("isAuthenticated()")
    boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi);

    @PreAuthorize("isAuthenticated()")
    boolean applicableTila(ProjektiTila tila);

    @PreAuthorize("isAuthenticated()")
    boolean applicablePerustetyyppi(PerusteTyyppi tyyppi);
}
