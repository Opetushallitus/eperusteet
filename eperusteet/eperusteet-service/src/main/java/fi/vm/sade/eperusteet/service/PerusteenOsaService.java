package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import java.util.List;
import fi.vm.sade.eperusteet.dto.PerusteenOsaDto;

/**
 *
 * @author jhyoty
 */
public interface PerusteenOsaService {
    
    <T extends PerusteenOsaDto, D extends PerusteenOsa> T add(T perusteenOsaDto, Class<T> dtoClass, Class<D> destinationClass);

    void delete(final Long id);

    PerusteenOsaDto get(final Long id);

    List<PerusteenOsaDto> getAll();

    PerusteenOsaDto update(final Long id, PerusteenOsaDto perusteenOsa);

}
