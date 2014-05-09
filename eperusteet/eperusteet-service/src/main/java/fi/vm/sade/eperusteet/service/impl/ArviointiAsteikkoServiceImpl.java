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

import fi.vm.sade.eperusteet.dto.ArviointiAsteikkoDto;
import fi.vm.sade.eperusteet.repository.ArviointiAsteikkoRepository;
import fi.vm.sade.eperusteet.service.ArviointiAsteikkoService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jhyoty
 */
@Service
@Transactional(readOnly = true)
public class ArviointiAsteikkoServiceImpl implements ArviointiAsteikkoService {

    @Autowired
    private ArviointiAsteikkoRepository repository;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Override
    public List<ArviointiAsteikkoDto> getAll() {
        return mapper.mapAsList(repository.findAll(), ArviointiAsteikkoDto.class);
    }

    @Override
    public ArviointiAsteikkoDto get(Long id) {
        return mapper.map(repository.findOne(id), ArviointiAsteikkoDto.class);
    }

}
