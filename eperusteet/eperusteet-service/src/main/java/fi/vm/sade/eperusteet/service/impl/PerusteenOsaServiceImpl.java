/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.dto.PerusteenOsaDto;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.LockManager;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.List;
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
    private TutkinnonOsaRepository tutkinnonOsaRepo;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private LockManager lockManager;

    @Override
    public List<PerusteenOsaDto> getAll() {
        return mapper.mapAsList(perusteenOsaRepo.findAll(), PerusteenOsaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteenOsaDto get(final Long id) {
        return mapper.map(perusteenOsaRepo.findOne(id), PerusteenOsaDto.class);
    }

    @Override
    public List<PerusteenOsaDto> getAllByKoodiUri(final String koodiUri) {
        return mapper.mapAsList(tutkinnonOsaRepo.findByKoodiUri(koodiUri), PerusteenOsaDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public <T extends PerusteenOsaDto, D extends PerusteenOsa> T update(T perusteenOsaDto, Class<T> dtoClass, Class<D> entityClass) {
        PerusteenOsa current = perusteenOsaRepo.findOne(perusteenOsaDto.getId());
        PerusteenOsa updated = mapper.map(perusteenOsaDto, current.getClass());
        current.mergeState(updated);
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
	public List<PerusteenOsaDto> getAllWithName(String name) {
		return mapper.mapAsList(tutkinnonOsaRepo.findByNimiTekstiTekstiContainingIgnoreCase(name), PerusteenOsaDto.class);
	}

    @Override
    public List<Revision> getRevisions(Long id) {
        PerusteenOsa perusteenOsa = perusteenOsaRepo.findOne(id);
        return perusteenOsaRepo.getRevisions(id);
    }

    @Override
    public PerusteenOsaDto getRevision(Long id, Integer revisionId) {
        return mapper.map(perusteenOsaRepo.findRevision(id, revisionId), PerusteenOsaDto.class);
    }
}
