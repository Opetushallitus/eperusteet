package fi.vm.sade.eperusteet.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TutkinnonOsa;
import fi.vm.sade.eperusteet.dto.PerusteenOsaDto;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;

/**
 *
 * @author jhyoty
 */
@Service
@Transactional(readOnly = true)
public class PerusteenOsaServiceImpl implements PerusteenOsaService {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteenOsaServiceImpl.class);

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepo;

    @Autowired
    private TutkinnonOsaRepository tutkinnonOsaRepo;

    @Autowired
    @Dto
    private DtoMapper mapper;
    
    @Override
    public List<PerusteenOsaDto> getAll() {
        return mapper.mapAsList(perusteenOsaRepo.findAll(), PerusteenOsaDto.class);
    }

    @Override
    public PerusteenOsaDto get(final Long id) {
        return mapper.map(perusteenOsaRepo.findOne(id), PerusteenOsaDto.class);
    }

    @Override
    public PerusteenOsaDto getByKoodiUri(final String koodiUri) {
        return mapper.map(tutkinnonOsaRepo.findOneByKoodiUri(koodiUri), PerusteenOsaDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public <T extends PerusteenOsaDto, D extends PerusteenOsa> T update(T perusteenOsaDto, Class<T> dtoClass, Class<D> entityClass) {
    	PerusteenOsa current = perusteenOsaRepo.findOne(perusteenOsaDto.getId());
        mapper.map(perusteenOsaDto, current);
        current = perusteenOsaRepo.save(current);
        return mapper.map(current, dtoClass);
    }
    
    @Override
    @Transactional(readOnly = false)
    public <T extends PerusteenOsaDto, D extends PerusteenOsa> T save(T perusteenOsaDto, Class<T> dtoClass, Class<D> entityClass) {
        D perusteenOsa = mapper.map(perusteenOsaDto, entityClass);
        perusteenOsa = perusteenOsaRepo.save(perusteenOsa);
        return mapper.map(perusteenOsa, dtoClass);
    }
  
    @Override
    @Transactional(readOnly = false)
    public void delete(final Long id) {
        LOG.info("delete" + id);
        perusteenOsaRepo.delete(id);
    }

	@Override
	public List<Revision> getRevisions(Long id) {
		PerusteenOsa perusteenOsa = perusteenOsaRepo.findOne(id);
		if(perusteenOsa instanceof TutkinnonOsa) {
			return tutkinnonOsaRepo.getRevisions(id, "arviointi");
		}
		return perusteenOsaRepo.getRevisions(id);
	}

	@Override
	public PerusteenOsaDto getRevision(Long id, Integer revisionId) {
		return mapper.map(perusteenOsaRepo.findRevision(id, revisionId), PerusteenOsaDto.class);
	}
}
