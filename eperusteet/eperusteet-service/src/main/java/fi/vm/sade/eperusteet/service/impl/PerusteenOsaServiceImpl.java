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

import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.Osaamistavoite;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.TutkinnonOsa;
import fi.vm.sade.eperusteet.dto.KommenttiDto;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.tutkinnonOsa.OsaAlueDto;
import fi.vm.sade.eperusteet.dto.tutkinnonOsa.OsaamistavoiteDto;
import fi.vm.sade.eperusteet.repository.OsaAlueRepository;
import fi.vm.sade.eperusteet.repository.OsaamistavoiteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.KommenttiService;
import fi.vm.sade.eperusteet.service.LockManager;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
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
public class PerusteenOsaServiceImpl implements PerusteenOsaService {
    @Autowired
    private KommenttiService kommenttiService;

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepo;

    @Autowired
    private TutkinnonOsaRepository tutkinnonOsaRepo;

    @Autowired
    private OsaAlueRepository osaAlueRepository;

    @Autowired
    private OsaamistavoiteRepository osaamistavoiteRepository;

    @PersistenceContext
    private EntityManager em;

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
    @Transactional(readOnly = true)
    public Integer getLatestRevision(final Long id) {
        return perusteenOsaRepo.getLatestRevisionId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerusteenOsaDto> getAllByKoodiUri(final String koodiUri) {
        return mapper.mapAsList(tutkinnonOsaRepo.findByKoodiUri(koodiUri), PerusteenOsaDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public <T extends PerusteenOsaDto, D extends PerusteenOsa> T update(T perusteenOsaDto, Class<T> dtoClass) {
        assertExists(perusteenOsaDto.getId());
        lockManager.ensureLockedByAuthenticatedUser(perusteenOsaDto.getId());
        PerusteenOsa current = perusteenOsaRepo.findOne(perusteenOsaDto.getId());
        PerusteenOsa updated = mapper.map(perusteenOsaDto, current.getClass());
        current.mergeState(updated);
        current = perusteenOsaRepo.save(current);

        return mapper.map(current, dtoClass);
    }

    @Override
    @Transactional(readOnly = false)
    public <T extends PerusteenOsaDto, D extends PerusteenOsa> T update(UpdateDto<T> perusteenOsaDto, Class<T> dtoClass) {
        T updated = update(perusteenOsaDto.getDto(), dtoClass);

        if (perusteenOsaDto.getMetadata() != null) {
            perusteenOsaRepo.setRevisioKommentti(perusteenOsaDto.getMetadata().getKommentti());
        }
        return updated;
    }

    @Override
    @Transactional(readOnly = false)
    public <T extends PerusteenOsaDto, D extends PerusteenOsa> T add(T perusteenOsaDto, Class<T> dtoClass, Class<D> entityClass) {
        D perusteenOsa = mapper.map(perusteenOsaDto, entityClass);
        perusteenOsa = perusteenOsaRepo.save(perusteenOsa);
        return mapper.map(perusteenOsa, dtoClass);
    }

    @Override
    @Transactional(readOnly = false)
    public OsaAlueDto addTutkinnonOsaOsaAlue(Long id, OsaAlueDto osaAlueDto) {
        assertExists(id);
        lockManager.ensureLockedByAuthenticatedUser(id);
        TutkinnonOsa tutkinnonOsa = tutkinnonOsaRepo.findOne(id);
        OsaAlue osaAlue;
        if (osaAlueDto != null) {
            osaAlue = mapper.map(osaAlueDto, OsaAlue.class);
        } else {
            osaAlue = new OsaAlue();
        }
        osaAlueRepository.save(osaAlue);
        tutkinnonOsa.getOsaAlueet().add(osaAlue);
        tutkinnonOsaRepo.save(tutkinnonOsa);

        return mapper.map(osaAlue, OsaAlueDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public OsaAlueDto updateTutkinnonOsaOsaAlue(Long id, Long osaAlueId, OsaAlueDto osaAlue) {
        assertExists(id);
        lockManager.ensureLockedByAuthenticatedUser(id);
        OsaAlue osaAlueEntity = osaAlueRepository.findOne(osaAlueId);
        if (osaAlueEntity == null) {
            throw new EntityNotFoundException("Osa-aluetta ei löytynyt id:llä: " + osaAlueId);
        }
        OsaAlue osaAlueTmp = mapper.map(osaAlue, OsaAlue.class);
        osaAlueEntity.mergeState(osaAlueTmp);
        osaAlueRepository.save(osaAlueEntity);

        return mapper.map(osaAlueEntity, OsaAlueDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OsaAlueDto> getTutkinnonOsaOsaAlueet(Long id) {
        TutkinnonOsa tutkinnonOsa = tutkinnonOsaRepo.findOne(id);
        if (tutkinnonOsa == null) {
            throw new EntityNotFoundException("Tutkinnon osaa ei löytynyt id:llä: " + id);
        }

        return mapper.mapAsList(tutkinnonOsa.getOsaAlueet(), OsaAlueDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public void removeOsaAlue(Long id, Long osaAlueId) {
        assertExists(id);
        lockManager.ensureLockedByAuthenticatedUser(id);
        OsaAlue osaAlue = osaAlueRepository.findOne(osaAlueId);
        if (osaAlue == null) {
            throw new EntityNotFoundException("Osa-aluetta ei löytynyt id:llä: " + osaAlueId);
        }
        TutkinnonOsa tutkinnonOsa = tutkinnonOsaRepo.findOne(id);
        tutkinnonOsa.getOsaAlueet().remove(osaAlue);
         osaAlueRepository.delete(osaAlue);
    }


    @Override
    @Transactional(readOnly = false)
    public OsaamistavoiteDto addOsaamistavoite(Long id, Long osaAlueId, OsaamistavoiteDto osaamistavoiteDto) {
        assertExists(id);
        lockManager.ensureLockedByAuthenticatedUser(id);
        OsaAlue osaAlue = osaAlueRepository.findOne(osaAlueId);
        if (osaAlue == null) {
            throw new EntityNotFoundException("Osa-aluetta ei löytynyt id:llä: " + osaAlueId);
        }
        Osaamistavoite osaamistavoite;
        if (osaamistavoiteDto != null) {
            osaamistavoite = mapper.map(osaamistavoiteDto, Osaamistavoite.class);
        } else {
            osaamistavoite = new Osaamistavoite();
        }
        osaamistavoiteRepository.save(osaamistavoite);
        osaAlue.getOsaamistavoitteet().add(osaamistavoite);
        osaAlueRepository.save(osaAlue);

        return mapper.map(osaamistavoite, OsaamistavoiteDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OsaamistavoiteDto> getOsaamistavoitteet(Long id, Long osaAlueId) {
        assertExists(id);
        OsaAlue osaAlue = osaAlueRepository.findOne(osaAlueId);
        return mapper.mapAsList(osaAlue.getOsaamistavoitteet(), OsaamistavoiteDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public void removeOsaamistavoite(Long id, Long osaAlueId, Long osaamistavoiteId) {
        assertExists(id);
        lockManager.ensureLockedByAuthenticatedUser(id);
        OsaAlue osaAlue = osaAlueRepository.findOne(osaAlueId);
        if (osaAlue == null) {
            throw new EntityNotFoundException("Osa-aluetta ei löytynyt id:llä: " + osaAlueId);
        }
        Osaamistavoite osaamistavoiteEntity = osaamistavoiteRepository.findOne(osaamistavoiteId);
        if (osaamistavoiteEntity == null) {
            throw new EntityNotFoundException("Osaamistavoitetta ei löytynyt id:llä: " + osaamistavoiteId);
        }
        osaAlue.getOsaamistavoitteet().remove(osaamistavoiteEntity);
        osaamistavoiteRepository.delete(osaamistavoiteEntity);
    }

    @Override
    @Transactional(readOnly = false)
    public OsaamistavoiteDto updateOsaamistavoite(Long id, Long osaAlueId, Long osaamistavoiteId, OsaamistavoiteDto osaamistavoite) {
        assertExists(id);
        lockManager.ensureLockedByAuthenticatedUser(id);
        OsaAlue osaAlue = osaAlueRepository.findOne(osaAlueId);
        if (osaAlue == null) {
            throw new EntityNotFoundException("Osa-aluetta ei löytynyt id:llä: " + osaAlueId);
        }
        Osaamistavoite osaamistavoiteEntity = osaamistavoiteRepository.findOne(osaamistavoiteId);
        if (osaamistavoiteEntity == null) {
            throw new EntityNotFoundException("Osaamistavoitetta ei löytynyt id:llä: " + osaamistavoiteId);
        }
        Osaamistavoite osaamistavoiteUusi = mapper.map(osaamistavoite, Osaamistavoite.class);
        osaamistavoiteEntity.mergeState(osaamistavoiteUusi);

        return mapper.map(osaamistavoiteEntity, OsaamistavoiteDto.class);
    }


    @Override
    public void delete(final Long id) {
        assertExists(id);
        lockManager.lock(id);
        try {
            List<KommenttiDto> allByPerusteenOsa = kommenttiService.getAllByPerusteenOsa(id);
            for (KommenttiDto kommentti : allByPerusteenOsa) {
                kommenttiService.deleteReally(kommentti.getId());
            }
            perusteenOsaRepo.delete(id);
        } finally {
            lockManager.unlock(id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerusteenOsaDto> getAllWithName(String name) {
        return mapper.mapAsList(tutkinnonOsaRepo.findByNimiTekstiTekstiContainingIgnoreCase(name), PerusteenOsaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Revision> getVersiot(Long id) {
        return perusteenOsaRepo.getRevisions(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteenOsaDto getVersio(Long id, Integer versioId) {
        return mapper.map(perusteenOsaRepo.findRevision(id, versioId), PerusteenOsaDto.class);
    }

    @Override
    @Transactional
    public PerusteenOsaDto revertToVersio(Long id, Integer versioId) {
        PerusteenOsa revision = perusteenOsaRepo.findRevision(id, versioId);
        return update(mapper.map(revision, PerusteenOsaDto.class), PerusteenOsaDto.class);
    }

    @Override
    public LukkoDto lock(Long id) {
        assertExists(id);
        return LukkoDto.of(lockManager.lock(id));
    }

    @Override
    public void unlock(Long id) {
        assertExists(id);
        lockManager.unlock(id);
    }

    @Override
    public LukkoDto getLock(Long id) {
        assertExists(id);
        return LukkoDto.of(lockManager.getLock(id));
    }

    private void assertExists(Long id) {
        if (!perusteenOsaRepo.exists(id)) {
            throw new BusinessRuleViolationException("Pyydettyä perusteen osaa ei ole olemassa");
        }
    }

}
