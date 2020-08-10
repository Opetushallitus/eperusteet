package fi.vm.sade.eperusteet.service;


import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

public interface NavigationBuilder extends PerusteToteutus {
    @Override
    default Class getImpl() {
        return NavigationBuilder.class;
    }

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    default NavigationNodeDto buildNavigation(@P("perusteId") Long perusteId, String kieli) {
        throw new BusinessRuleViolationException("ei-tuettu");
    }
}
