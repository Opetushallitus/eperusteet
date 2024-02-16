package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.TutkinnonOsaViiteService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.internal.LockManager;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TutkinnonOsaViiteServiceImpl implements TutkinnonOsaViiteService {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private TutkinnonOsaViiteRepository tutkinnonOsaViiteRepository;

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Autowired
    private LockManager lockManager;

    @Autowired
    private TutkinnonOsaRepository tutkinnonOsaRepository;

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
        return tutkinnonOsaViiteRepository.getLatestRevisionId(id).getNumero();
    }

    @Override
    public LukkoDto lockPerusteenOsa(Long viiteId) {
        assertExists(viiteId);
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(viiteId);
        if (viite == null || viite.getTutkinnonOsa() == null ) {
            throw new BusinessRuleViolationException("Virheellinen viiteId");
        }
        return LukkoDto.of(lockManager.lock(viite.getId()));
    }

    @Override
    public void unlockPerusteenOsa(Long viiteId) {
        assertExists(viiteId);
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(viiteId);
        if (viite == null || viite.getTutkinnonOsa() == null ) {
            throw new BusinessRuleViolationException("Virheellinen viiteId");
        }
        lockManager.unlock(viite.getId());
    }

    @Override
    public LukkoDto getPerusteenOsaLock(Long viiteId) {
        assertExists(viiteId);
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(viiteId);
        if (viite == null || viite.getTutkinnonOsa() == null ) {
            throw new BusinessRuleViolationException("Virheellinen viiteId");
        }
        return LukkoDto.of(lockManager.getLock(viite.getId()));
    }

    @Override
    @Transactional
    public TutkinnonOsaViiteDto revertToVersio(Long id, Integer versioId) {
        TutkinnonOsaViite revision = tutkinnonOsaViiteRepository.findRevision(id, versioId);
        TutkinnonOsaViiteDto viiteDto = mapper.map(revision, TutkinnonOsaViiteDto.class);
        TutkinnonOsaDto tutkinnonOsaDto = mapper.map(revision.getTutkinnonOsa(), TutkinnonOsaDto.class);
        viiteDto.setTutkinnonOsaDto(tutkinnonOsaDto);
        return update(viiteDto, false);
    }

    @Override
    @Transactional
    public TutkinnonOsaViiteDto update(TutkinnonOsaViiteDto viiteDto, boolean updateTutkinnonOsa) {
        assertExists(viiteDto.getId());
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(viiteDto.getId());

        if (viite == null || viite.getTutkinnonOsa() == null ) {
            throw new BusinessRuleViolationException("Virheellinen viiteId");
        }

        if (viiteDto.getLaajuus() == null) {
            viiteDto.setLaajuusMaksimi(null);
        }

        if (viiteDto.getLaajuus() != null
                && viiteDto.getLaajuusMaksimi() != null
                && viiteDto.getLaajuusMaksimi().compareTo(viiteDto.getLaajuus()) < 0) {
            throw new BusinessRuleViolationException("Laajuuden maksimin täytyy olla suurempi kuin minimiarvon");
        }

        lockManager.ensureLockedByAuthenticatedUser(viiteDto.getId());
        if (viiteDto.getTutkinnonOsaDto() != null) {
            if (updateTutkinnonOsa) {
                lockManager.lock(viiteDto.getTutkinnonOsaDto().getId());
                viiteDto.setTutkinnonOsaDto(perusteenOsaService.update(viiteDto.getTutkinnonOsaDto()));
                lockManager.unlock(viiteDto.getTutkinnonOsaDto().getId());
            }

            viite.setTutkinnonOsa(tutkinnonOsaRepository.findOne(viiteDto.getTutkinnonOsa().getIdLong()));
        }

        viite.setJarjestys(viiteDto.getJarjestys());
        viite.setLaajuus(viiteDto.getLaajuus());
        viite.setLaajuusMaksimi(viiteDto.getLaajuusMaksimi());
        viite.setMuokattu(new Date());
        viite = tutkinnonOsaViiteRepository.save(viite);
        TutkinnonOsaViiteDto uusiViiteDto = mapper.map(viite, TutkinnonOsaViiteDto.class);

        if (viiteDto.getTutkinnonOsaDto() != null) {
            uusiViiteDto.setTutkinnonOsaDto(viiteDto.getTutkinnonOsaDto());
        }

        return uusiViiteDto;
    }

    private void assertExists(Long id) {
        if (!tutkinnonOsaViiteRepository.exists(id)) {
            throw new BusinessRuleViolationException("Pyydettyä tutkinnonosaviitettä ei ole olemassa");
        }
    }
}
