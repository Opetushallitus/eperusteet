package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

public interface NavigationBuilder {
    @PreAuthorize("hasPermission(#peruste.id, 'peruste', 'LUKU')")
    default NavigationNodeDto buildNavigation(@P("peruste") Peruste peruste) {
        throw new BusinessRuleViolationException("ei-tuettu");
    }
}
