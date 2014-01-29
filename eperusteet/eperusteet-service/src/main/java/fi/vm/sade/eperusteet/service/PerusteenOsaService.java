package fi.vm.sade.eperusteet.service;

import java.util.List;
import fi.vm.sade.eperusteet.dto.PerusteenOsaDto;

/**
 *
 * @author jhyoty
 */
public interface PerusteenOsaService {

    PerusteenOsaDto add(PerusteenOsaDto perusteenOsaDto);

    void delete(final Long id);

    PerusteenOsaDto get(final Long id);

    List<PerusteenOsaDto> getAll();

    PerusteenOsaDto update(final Long id, PerusteenOsaDto perusteenOsa);

}
