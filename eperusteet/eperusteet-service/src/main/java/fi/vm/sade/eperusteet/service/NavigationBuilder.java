package fi.vm.sade.eperusteet.service;


import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.security.core.parameters.P;
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

    default LokalisoituTekstiDto getPerusteenOsaNimi(DtoMapper mapper, PerusteenOsa perusteenOsa) {
        return perusteenOsa != null
                ? mapper.map(perusteenOsa, PerusteenOsaDto.class).getNimi()
                : null;
    }
}
