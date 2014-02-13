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

import fi.vm.sade.eperusteet.domain.Koulutusala;
import fi.vm.sade.eperusteet.dto.KoulutusalaDto;
import fi.vm.sade.eperusteet.repository.KoulutusalaRepository;
import fi.vm.sade.eperusteet.service.KoulutusalaService;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jussini
 */
@Service
public class KoulutusalaServiceImpl implements KoulutusalaService{

    private static final Logger LOG = LoggerFactory.getLogger(KoulutusalaServiceImpl.class);
    
    @Autowired
    private KoulutusalaRepository repository;

    @Autowired
    @Koodisto
    private DtoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public KoulutusalaDto get(Long id) {
        Koulutusala k = repository.findOne(id);
        if (k == null) {
            LOG.warn("Koulutusalaa {} ei löytynyt", id);
        }        
        return mapper.map(k, KoulutusalaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KoulutusalaDto> getAll() {
        List<Koulutusala> klist = repository.findAll();
        return mapper.mapAsList(klist,KoulutusalaDto.class);
    }


          
}
