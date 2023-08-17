package fi.vm.sade.eperusteet.service;


import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.service.util.Validointi;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface ProjektiValidator {

    @PreAuthorize("hasPermission(#perusteprojektiId, 'perusteprojekti', 'MUOKKAUS')")
    List<Validointi> run(@P("perusteprojektiId") Long perusteprojektiId, ProjektiTila tila);
}