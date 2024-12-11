package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlueTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueLaajaDto;
import fi.vm.sade.eperusteet.repository.GeneerinenArviointiasteikkoRepository;
import fi.vm.sade.eperusteet.repository.OsaAlueRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.service.OsaAlueService;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.internal.LockManager;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class OsaAlueServiceImpl implements OsaAlueService {

    @Autowired
    private LockManager lockManager;

    @Autowired
    private OsaAlueRepository osaAlueRepository;

    @Autowired
    private TutkinnonOsaViiteRepository tutkinnonOsaViiteRepository;

    @Autowired
    private GeneerinenArviointiasteikkoRepository geneerinenArviointiasteikkoRepository;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteenMuokkaustietoService muokkausTietoService;

    private OsaAlue findOne(Long viiteId, Long osaAlueId, boolean readonly) {
        OsaAlue osaAlue = osaAlueRepository.getOne(osaAlueId);
        if (osaAlue == null) {
            throw new EntityNotFoundException("Osa-aluetta ei löytynyt id:llä: " + osaAlueId);
        }

        TutkinnonOsaViite tov = tutkinnonOsaViiteRepository.getOne(viiteId);
        Set<Long> osat = osaAlue.getTutkinnonOsat().stream()
                .map(PerusteenOsa::getId)
                .collect(Collectors.toSet());
        Long tosaId = tov.getTutkinnonOsa().getId();
        if (!osat.contains(tosaId)) {
            throw new EntityNotFoundException("viite-ei-omista-osa-alueetta");
        }

        if (!readonly) {
            lockManager.ensureLockedByAuthenticatedUser(osaAlueId);
        }

        return osaAlue;
    }

    @Override
    public OsaAlueLaajaDto getOsaAlue(Long viiteId, Long osaAlueId) {
        OsaAlue oa = findOne(viiteId, osaAlueId, true);
        OsaAlueLaajaDto oaDto = mapper.map(oa, OsaAlueLaajaDto.class);
        return oaDto;
    }

    @Override
    public OsaAlueLaajaDto addOsaAlue(Long viiteId, OsaAlueLaajaDto osaAlueDto) {
        if (!OsaAlueTyyppi.OSAALUE2020.equals(osaAlueDto.getTyyppi())) {
            throw new EntityNotFoundException("tuki-vain-uusille-osaalueille");
        }

        TutkinnonOsaViite tov = tutkinnonOsaViiteRepository.getOne(viiteId);
        OsaAlue osaAlue = mapper.map(osaAlueDto, OsaAlue.class);
        updateGeneerinen(osaAlue, osaAlueDto);
        osaAlue = osaAlueRepository.save(osaAlue);
        tov.getTutkinnonOsa().getOsaAlueet().add(osaAlue);
        return mapper.map(osaAlue, OsaAlueLaajaDto.class);
    }

    @Override
    public OsaAlueLaajaDto addOsaAlue(Long perusteId, Long viiteId, OsaAlueLaajaDto osaAlueDto) {
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.getOne(viiteId);

        if (viite.getTutkinnonOsa().getAlkuperainenPeruste() != null && viite.getSuoritustapa().getPerusteet().stream().noneMatch(p -> p.getId().equals(viite.getTutkinnonOsa().getAlkuperainenPeruste().getId()))) {
            throw new BusinessRuleViolationException("osa-alueen-lisays-ei-sallittu");
        }

        OsaAlueLaajaDto uusiOsaAlueDto = addOsaAlue(viiteId, osaAlueDto);
        OsaAlue osaAlue = mapper.map(uusiOsaAlueDto, OsaAlue.class);
        muokkausTietoService.addMuokkaustieto(perusteId, osaAlue, MuokkausTapahtuma.LUONTI);
        return uusiOsaAlueDto;
    }

    private void updateGeneerinen(OsaAlue osaAlue, OsaAlueLaajaDto dto) {
        if (dto == null || dto.getArviointi() == null) {
            return;
        }

        if (osaAlue.getGeneerinenArviointiasteikko() != null && Objects.equals(osaAlue.getGeneerinenArviointiasteikko().getId(), dto.getArviointi().getId())) {
            return;
        }

        GeneerinenArviointiasteikko arviointi = geneerinenArviointiasteikkoRepository.getOne(dto.getArviointi().getId());
        if (!arviointi.isJulkaistu()) {
            throw new BusinessRuleViolationException("vain-julkaistun-voi-valita");
        }
        osaAlue.setGeneerinenArviointiasteikko(arviointi);
    }

    @Override
    public OsaAlueLaajaDto updateOsaAlue(Long viiteId, Long osaAlueId, OsaAlueLaajaDto osaAlue) {
        OsaAlue oa = findOne(viiteId, osaAlueId, false);
        OsaAlue uusi = mapper.map(osaAlue, OsaAlue.class);
        oa.mergeState(uusi);
        updateGeneerinen(oa, osaAlue);
        oa = osaAlueRepository.save(oa);
        return mapper.map(oa, OsaAlueLaajaDto.class);
    }

    @Override
    public OsaAlueLaajaDto updateOsaAlue(Long perusteId, Long viiteId, Long osaAlueId, OsaAlueLaajaDto osaAlueDto) {
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(viiteId);

        if (viite.getTutkinnonOsa().getAlkuperainenPeruste() != null && viite.getSuoritustapa().getPerusteet().stream().noneMatch(p -> p.getId().equals(viite.getTutkinnonOsa().getAlkuperainenPeruste().getId()))) {
            throw new BusinessRuleViolationException("osa-alueen-muokkaus-ei-sallittu");
        }

        OsaAlueLaajaDto uusiOsaAlueDto = updateOsaAlue(viiteId, osaAlueId, osaAlueDto);
        OsaAlue osaAlue = mapper.map(uusiOsaAlueDto, OsaAlue.class);
        muokkausTietoService.addMuokkaustieto(perusteId, osaAlue, MuokkausTapahtuma.PAIVITYS);
        return uusiOsaAlueDto;
    }

    @Override
    public void removeOsaAlue(Long viiteId, Long osaAlueId) {
        OsaAlue oa = findOne(viiteId, osaAlueId, false);
        TutkinnonOsaViite tov = tutkinnonOsaViiteRepository.getOne(viiteId);
        tov.getTutkinnonOsa().getOsaAlueet().remove(oa);
        osaAlueRepository.delete(oa);
    }

    @Override
    public void removeOsaAlue(Long perusteId, Long viiteId, Long osaAlueId) {
        OsaAlue oa = findOne(viiteId, osaAlueId, false);
        TutkinnonOsaViite tov = tutkinnonOsaViiteRepository.getOne(viiteId);
        tov.getTutkinnonOsa().getOsaAlueet().remove(oa);
        muokkausTietoService.addMuokkaustieto(perusteId, oa, MuokkausTapahtuma.POISTO);
        osaAlueRepository.delete(oa);
    }

    @Override
    public LukkoDto getOsaAlueLock(Long viiteId, Long osaAlueId) {
        Lukko lock = lockManager.getLock(osaAlueId);
        return mapper.map(lock, LukkoDto.class);
    }

    @Override
    public LukkoDto lockOsaAlue(Long viiteId, Long osaAlueId) {
        Lukko lock = lockManager.lock(osaAlueId);
        return mapper.map(lock, LukkoDto.class);
    }

    @Override
    public void unlockOsaAlue(Long viiteId, Long osaAlueId) {
        lockManager.unlock(osaAlueId);
    }

}
