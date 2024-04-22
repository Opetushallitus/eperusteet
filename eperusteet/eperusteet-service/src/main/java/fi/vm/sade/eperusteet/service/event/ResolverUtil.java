package fi.vm.sade.eperusteet.service.event;

import java.util.Set;

public interface ResolverUtil {
    Set<Long> findPerusteIdsByFirstResolvable(ResolvableReferenced resolvable);
}
