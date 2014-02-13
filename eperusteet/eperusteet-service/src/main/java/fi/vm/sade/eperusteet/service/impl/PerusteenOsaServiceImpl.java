package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import java.util.List;
import fi.vm.sade.eperusteet.dto.PerusteenOsaDto;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = false)
    public <T extends PerusteenOsaDto, D extends PerusteenOsa> T add(T perusteenOsaDto, Class<T> dtoClass, Class<D> entityClass) {
        LOG.debug("map dto to entity");
        D perusteenOsa = mapper.map(perusteenOsaDto, entityClass);
        
        LOG.debug("Save entity to db");
        perusteenOsa = perusteenOsaRepo.save(perusteenOsa);

        return mapper.map(perusteenOsa, dtoClass);
    }

    @Override
    @Transactional(readOnly = false)
    public PerusteenOsaDto update(final Long id, PerusteenOsaDto osa) {
        return null;
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(final Long id) {
        LOG.info("delete" + id);
        perusteenOsaRepo.delete(id);
    }

}
