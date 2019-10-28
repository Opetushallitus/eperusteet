package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.service.mapping.DtoMapper;

public interface Navigable {
    NavigationNodeDto constructNavigation(DtoMapper mapper);
}
