package fi.vm.sade.eperusteet.service;

import java.util.List;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.audit.Revision;
import fi.vm.sade.eperusteet.dto.PerusteenOsaDto;

/**
 *
 * @author jhyoty
 */
public interface PerusteenOsaService {
    
	public <T extends PerusteenOsaDto, D extends PerusteenOsa> T update(T perusteenOsaDto, Class<T> dtoClass, Class<D> entityClass);
	
    <T extends PerusteenOsaDto, D extends PerusteenOsa> T save(T perusteenOsaDto, Class<T> dtoClass, Class<D> destinationClass);
   
    void delete(final Long id);

    PerusteenOsaDto get(final Long id);

    PerusteenOsaDto getByKoodiUri(final String koodiUri);

    List<PerusteenOsaDto> getAll();

	public List<Revision> getRevisions(Long id);
}
