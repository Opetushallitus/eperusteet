package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.service.mapping.DtoMapper;

public interface LiitteetNavigable {
    NavigationNodeDto constructLiitteetNavigation(DtoMapper mapper);
}
