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
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.Osaamistavoite;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.TutkinnonOsa;
import fi.vm.sade.eperusteet.dto.KommenttiDto;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonOsa.OsaAlueLaajaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonOsa.OsaamistavoiteLaajaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonOsa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.repository.OsaAlueRepository;
import fi.vm.sade.eperusteet.repository.OsaamistavoiteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.KommenttiService;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.internal.LockManager;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
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
    @Transactional(readOnly = true)
    public List<fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Suppea> getAll() {
        return mapper.mapAsList(perusteenOsaRepo.findAll(), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Suppea.class);
    }

    @Override
    @Transactional(readOnly = true)
    public fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja get(final Long id) {
        return mapper.map(perusteenOsaRepo.findOne(id), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getLatestRevision(final Long id) {
        return perusteenOsaRepo.getLatestRevisionId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja> getAllByKoodiUri(final String koodiUri) {
        return mapper.mapAsList(tutkinnonOsaRepo.findByKoodiUri(koodiUri), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja.class);
    }

    @Override
    @Transactional(readOnly = false)
    public <T extends fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja> T update(T perusteenOsaDto) {
        assertExists(perusteenOsaDto.getId());
        lockManager.ensureLockedByAuthenticatedUser(perusteenOsaDto.getId());
        PerusteenOsa current = perusteenOsaRepo.findOne(perusteenOsaDto.getId());
        PerusteenOsa updated = mapper.map(perusteenOsaDto, current.getClass());
        if (perusteenOsaDto.getClass().equals(TutkinnonOsaDto.class)) {
            ((TutkinnonOsa)updated).setOsaAlueet(createOsaAlueIfNotExist(((TutkinnonOsa)updated).getOsaAlueet()));
        }
        current.mergeState(updated);
        current = perusteenOsaRepo.save(current);

        mapper.map(current, perusteenOsaDto);
        return perusteenOsaDto;
    }

    @Override
    @Transactional(readOnly = false)
    public <T extends fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja> T update(UpdateDto<T> perusteenOsaDto) {
        T updated = update(perusteenOsaDto.getDto());

        if (perusteenOsaDto.getMetadata() != null) {
            perusteenOsaRepo.setRevisioKommentti(perusteenOsaDto.getMetadata().getKommentti());
        }
        return updated;
    }

    @Override
    @Transactional(readOnly = false)
    public <T extends fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja> T add(T perusteenOsaDto) {
        PerusteenOsa perusteenOsa = mapper.map(perusteenOsaDto, PerusteenOsa.class);
        perusteenOsa = perusteenOsaRepo.save(perusteenOsa);
        mapper.map(perusteenOsa, perusteenOsaDto);
        return perusteenOsaDto;
    }

    private List<OsaAlue> createOsaAlueIfNotExist(List<OsaAlue> osaAlueet) {

        List<OsaAlue> osaAlueTemp = new ArrayList<>();
        if (osaAlueet != null) {
            for (OsaAlue osaAlue : osaAlueet) {
                if (osaAlue.getId() == null) {
                    osaAlueTemp.add(osaAlueRepository.save(osaAlue));
                } else {
                    osaAlueTemp.add(osaAlue);
                }
            }
        }
        return osaAlueTemp;
    }

    @Override
    @Transactional(readOnly = false)
    public OsaAlueLaajaDto addTutkinnonOsaOsaAlue(Long id, OsaAlueLaajaDto osaAlueDto) {
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

        return mapper.map(osaAlue, OsaAlueLaajaDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public OsaAlueLaajaDto updateTutkinnonOsaOsaAlue(Long id, Long osaAlueId, OsaAlueLaajaDto osaAlue) {
        assertExists(id);
        lockManager.ensureLockedByAuthenticatedUser(id);
        OsaAlue osaAlueEntity = osaAlueRepository.findOne(osaAlueId);
        if (osaAlueEntity == null) {
            throw new EntityNotFoundException("Osa-aluetta ei löytynyt id:llä: " + osaAlueId);
        }
        OsaAlue osaAlueTmp = mapper.map(osaAlue, OsaAlue.class);
        osaAlueTmp.setOsaamistavoitteet(createOsaamistavoiteIfNotExist(osaAlueTmp.getOsaamistavoitteet()));
        osaAlueEntity.mergeState(osaAlueTmp);
        osaAlueRepository.save(osaAlueEntity);

        return mapper.map(osaAlueEntity, OsaAlueLaajaDto.class);
    }


    private List<Osaamistavoite> createOsaamistavoiteIfNotExist(List<Osaamistavoite> osaamistavoitteet) {

        List<Osaamistavoite> osaamistavoiteTemp = new ArrayList<>();
        if (osaamistavoitteet != null) {
            for (Osaamistavoite osaamistavoite : osaamistavoitteet) {
                if (osaamistavoite.getId() == null) {
                    osaamistavoiteTemp.add(osaamistavoiteRepository.save(osaamistavoite));
                } else {
                    osaamistavoiteTemp.add(osaamistavoite);
                }
            }
        }
        return osaamistavoiteTemp;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OsaAlueLaajaDto> getTutkinnonOsaOsaAlueet(Long id) {
        TutkinnonOsa tutkinnonOsa = tutkinnonOsaRepo.findOne(id);
        if (tutkinnonOsa == null) {
            throw new EntityNotFoundException("Tutkinnon osaa ei löytynyt id:llä: " + id);
        }

        return mapper.mapAsList(tutkinnonOsa.getOsaAlueet(), OsaAlueLaajaDto.class);
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
    public OsaamistavoiteLaajaDto addOsaamistavoite(Long id, Long osaAlueId, OsaamistavoiteLaajaDto osaamistavoiteDto) {
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

        return mapper.map(osaamistavoite, OsaamistavoiteLaajaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OsaamistavoiteLaajaDto> getOsaamistavoitteet(Long id, Long osaAlueId) {
        assertExists(id);
        OsaAlue osaAlue = osaAlueRepository.findOne(osaAlueId);
        if (osaAlue == null) {
            throw new EntityNotFoundException("Osa-aluetta ei löytynyt id:llä: " + osaAlueId);
        }
        return mapper.mapAsList(osaAlue.getOsaamistavoitteet(), OsaamistavoiteLaajaDto.class);

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
    public OsaamistavoiteLaajaDto updateOsaamistavoite(Long id, Long osaAlueId, Long osaamistavoiteId, OsaamistavoiteLaajaDto osaamistavoite) {
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

        return mapper.map(osaamistavoiteEntity, OsaamistavoiteLaajaDto.class);
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
    public List<fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Suppea> getAllWithName(String name) {
        return mapper.mapAsList(tutkinnonOsaRepo.findByNimiTekstiTekstiContainingIgnoreCase(name), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Suppea.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Revision> getVersiot(Long id) {
        return perusteenOsaRepo.getRevisions(id);
    }

    @Override
    @Transactional(readOnly = true)
    public fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja getVersio(Long id, Integer versioId) {
        return mapper.map(perusteenOsaRepo.findRevision(id, versioId), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja.class);
    }

    @Override
    @Transactional
    public fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja revertToVersio(Long id, Integer versioId) {
        PerusteenOsa revision = perusteenOsaRepo.findRevision(id, versioId);
        return update(mapper.map(revision, fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja.class));
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
