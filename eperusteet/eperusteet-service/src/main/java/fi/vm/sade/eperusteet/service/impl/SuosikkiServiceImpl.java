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

import fi.vm.sade.eperusteet.dto.kayttaja.SuosikkiDto;
import fi.vm.sade.eperusteet.repository.SuosikkiRepository;
import fi.vm.sade.eperusteet.service.SuosikkiService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author harrik
 */
@Service
public class SuosikkiServiceImpl implements SuosikkiService {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    SuosikkiRepository suosikki;

    @Override
    public SuosikkiDto get(Long suosikkiId) {
        return mapper.map(suosikki.findOne(suosikkiId), SuosikkiDto.class);
    }

}
