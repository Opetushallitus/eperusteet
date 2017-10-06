/*
 *
 *  * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *  *
 *  * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  * soon as they will be approved by the European Commission - subsequent versions
 *  * of the EUPL (the "Licence");
 *  *
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  * European Union Public Licence for more details.
 *
 */

package fi.vm.sade.eperusteet.service.dokumentti.impl;

import fi.vm.sade.eperusteet.domain.Dokumentti;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.repository.DokumenttiRepository;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiStateService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author iSaul
 */
@Service
public class DokumenttiStateServiceImpl implements DokumenttiStateService {

    @Autowired
    private DokumenttiRepository dokumenttiRepository;

    @Dto
    @Autowired
    private DtoMapper mapper;

    @IgnorePerusteUpdateCheck
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DokumenttiDto save(DokumenttiDto dto) {
        Dokumentti dokumentti = mapper.map(dto, Dokumentti.class);
        dokumentti = dokumenttiRepository.save(dokumentti);
        return mapper.map(dokumentti, DokumenttiDto.class);
    }
}
