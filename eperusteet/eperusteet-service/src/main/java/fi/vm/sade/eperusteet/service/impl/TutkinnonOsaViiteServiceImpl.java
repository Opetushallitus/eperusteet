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

import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonOsa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.TutkinnonOsaViiteService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.internal.LockManager;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author harrik
 */
@Service
@Transactional(readOnly = true)
public class TutkinnonOsaViiteServiceImpl implements TutkinnonOsaViiteService {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private TutkinnonOsaViiteRepository tutkinnonOsaViiteRepository;

    @Autowired
    private LockManager lockManager;

    @Override
    public List<Revision> getVersiot(Long id) {
        return tutkinnonOsaViiteRepository.getRevisions(id);
    }

    @Override
    public TutkinnonOsaViiteDto getVersio(Long id, Integer versioId) {
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findRevision(id, versioId);

        if (viite == null) {
            throw new BusinessRuleViolationException("Virheellinen viiteId");
        }
        TutkinnonOsaViiteDto viiteDto = mapper.map(viite, TutkinnonOsaViiteDto.class);
        TutkinnonOsaDto tutkinnonOsaDto = mapper.map(viite.getTutkinnonOsa(), TutkinnonOsaDto.class);
        viiteDto.setTutkinnonOsaDto(tutkinnonOsaDto);
        return viiteDto;
    }

    @Override
    public Integer getLatestRevision(Long id) {
        return tutkinnonOsaViiteRepository.getLatestRevisionId(id);
    }

    @Override
    public LukkoDto lockPerusteenOsa(Long viiteId) {
        assertExists(viiteId);
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(viiteId);
        if (viite == null || viite.getTutkinnonOsa() == null ) {
            throw new BusinessRuleViolationException("Virheellinen viiteId");
        }
        return LukkoDto.of(lockManager.lock(viite.getTutkinnonOsa().getId()));
    }

    @Override
    public void unlockPerusteenOsa(Long viiteId) {
        assertExists(viiteId);
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(viiteId);
        if (viite == null || viite.getTutkinnonOsa() == null ) {
            throw new BusinessRuleViolationException("Virheellinen viiteId");
        }
        lockManager.unlock(viite.getTutkinnonOsa().getId());
    }

    @Override
    public LukkoDto getPerusteenOsaLock(Long viiteId) {
        assertExists(viiteId);
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(viiteId);
        if (viite == null || viite.getTutkinnonOsa() == null ) {
            throw new BusinessRuleViolationException("Virheellinen viiteId");
        }
        return LukkoDto.of(lockManager.getLock(viite.getTutkinnonOsa().getId()));
    }

    private void assertExists(Long id) {
        if (!tutkinnonOsaViiteRepository.exists(id)) {
            throw new BusinessRuleViolationException("Pyydettyä tutkinnonosaviitettä ei ole olemassa");
        }
    }
}
