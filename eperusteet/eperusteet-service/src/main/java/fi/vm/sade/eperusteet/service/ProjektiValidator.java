package fi.vm.sade.eperusteet.service;


import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ProjektiValidator {

    @PreAuthorize("hasPermission(#perusteprojektiId, 'perusteprojekti', 'TILANVAIHTO')")
    TilaUpdateStatus run(@P("perusteprojektiId") Long perusteprojektiId, ProjektiTila tila);
}