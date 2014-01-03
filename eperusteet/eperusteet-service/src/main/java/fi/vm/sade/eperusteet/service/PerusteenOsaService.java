package fi.vm.sade.eperusteet.service;

import java.util.List;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;

/**
 *
 * @author jhyoty
 */
public interface PerusteenOsaService {

    PerusteenOsa add(PerusteenOsa perusteenOsa);

    void delete(final Long id);

    PerusteenOsa get(final Long id);

    List<PerusteenOsa> getAll();

    PerusteenOsa update(final Long id, PerusteenOsa perusteenOsa);

}
