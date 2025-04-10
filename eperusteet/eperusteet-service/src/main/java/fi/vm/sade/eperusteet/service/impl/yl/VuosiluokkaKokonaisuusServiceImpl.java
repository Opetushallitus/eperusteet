package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.domain.yl.OppiaineenVuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.VuosiluokkaKokonaisuusRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;

import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkaKokonaisuusContext;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkaKokonaisuusService;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = false)
public class VuosiluokkaKokonaisuusServiceImpl implements VuosiluokkaKokonaisuusService {

    @Autowired
    private VuosiluokkaKokonaisuusRepository kokonaisuusRepository;
    @Autowired
    @Dto
    private DtoMapper mapper;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private PerusopetuksenPerusteenSisaltoRepository sisaltoRepository;

    @Autowired
    @LockCtx(VuosiluokkaKokonaisuusContext.class)
    private LockService<VuosiluokkaKokonaisuusContext> lockService;

    @Autowired
    private PerusteenMuokkaustietoService muokkausTietoService;

    @Override
    @Transactional
    public VuosiluokkaKokonaisuusDto addVuosiluokkaKokonaisuus(Long perusteId, VuosiluokkaKokonaisuusDto dto) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        //TODO -- salli siirtymän "kytkeminen" toisesta jo tallennetusta vuosiluokkakokonaisuudesta
        VuosiluokkaKokonaisuus vk = mapper.map(dto, VuosiluokkaKokonaisuus.class);
        sisaltoRepository.lock(sisalto);
        sisalto.addVuosiluokkakokonaisuus(vk);
        vk = kokonaisuusRepository.save(vk);
        muokkausTietoService.addMuokkaustieto(perusteId, vk, MuokkausTapahtuma.LUONTI);
        return mapper.map(vk, VuosiluokkaKokonaisuusDto.class);
    }

    @Override
    public void deleteVuosiluokkaKokonaisuus(Long perusteId, Long kokonaisuusId) {
        lockService.lock(VuosiluokkaKokonaisuusContext.of(perusteId, kokonaisuusId));
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        VuosiluokkaKokonaisuus vk = kokonaisuusRepository.findOne(kokonaisuusId);
        if (vk.getOppiaineet().isEmpty()) {
            sisaltoRepository.lock(sisalto);
            sisalto.removeVuosiluokkakokonaisuus(vk);
            muokkausTietoService.addMuokkaustieto(perusteId, vk, MuokkausTapahtuma.POISTO);
            kokonaisuusRepository.delete(vk);
        } else {
            throw new BusinessRuleViolationException("Vuosiluokkakokonaisuutta ei voi poistaa koska siihen liittyy oppiaineita");
        }

    }

    @Override
    @Transactional(readOnly = true)
    public VuosiluokkaKokonaisuusDto getVuosiluokkaKokonaisuus(Long perusteId, Long kokonaisuusId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        VuosiluokkaKokonaisuus vk = kokonaisuusRepository.findOne(kokonaisuusId);
        if (sisalto != null && vk != null && sisalto.containsVuosiluokkakokonaisuus(vk)) {
            return mapper.map(vk, VuosiluokkaKokonaisuusDto.class);
        }
        throw new BusinessRuleViolationException("Haettu vuosiluokkakokonaisuus ei kuulu tähän perusteeseen");
    }

    @Override
    @Transactional(readOnly = true)
    public VuosiluokkaKokonaisuusDto getVuosiluokkaKokonaisuus(Long perusteId, Long kokonaisuusId, int revisio) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        VuosiluokkaKokonaisuus vk = kokonaisuusRepository.findOne(kokonaisuusId);
        if (sisalto != null && vk != null && sisalto.containsVuosiluokkakokonaisuus(vk)) {
            return mapper.map(kokonaisuusRepository.findRevision(kokonaisuusId, revisio), VuosiluokkaKokonaisuusDto.class);
        }
        throw new BusinessRuleViolationException("Haettu vuosiluokkakokonaisuus ei kuulu tähän perusteeseen");

    }

    @Override
    @Transactional(readOnly = true)
    public List<Revision> getVuosiluokkaKokonaisuusRevisions(long perusteId, long kokonaisuusId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        VuosiluokkaKokonaisuus vk = kokonaisuusRepository.findOne(kokonaisuusId);
        if (sisalto != null && vk != null && sisalto.containsVuosiluokkakokonaisuus(vk)) {
            return kokonaisuusRepository.getRevisions(kokonaisuusId);
        }
        throw new BusinessRuleViolationException("Haettu vuosiluokkakokonaisuus ei kuulu tähän perusteeseen");
    }

    @Override
    @Transactional(readOnly = true)
    public List<OppiaineSuppeaDto> getOppiaineet(Long perusteId, Long kokonaisuusId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        VuosiluokkaKokonaisuus vk = kokonaisuusRepository.findOne(kokonaisuusId);
        if (sisalto != null && vk != null && sisalto.containsVuosiluokkakokonaisuus(vk)) {
            HashSet<Oppiaine> aineet = new HashSet<>();
            for (OppiaineenVuosiluokkaKokonaisuus o : vk.getOppiaineet()) {
                Oppiaine a = o.getOppiaine();
                while (a.getOppiaine() != null) {
                    a = a.getOppiaine();
                }
                aineet.add(a);
            }
            return mapper.mapAsList(aineet, OppiaineSuppeaDto.class);
        }
        throw new BusinessRuleViolationException("Haettu vuosiluokkakokonaisuus ei kuulu tähän perusteeseen");
    }

    @Override
    public VuosiluokkaKokonaisuusDto updateVuosiluokkaKokonaisuus(Long perusteId, UpdateDto<VuosiluokkaKokonaisuusDto> updateDto) {
        VuosiluokkaKokonaisuusDto dto = updateDto.getDto();
        lockService.assertLock(VuosiluokkaKokonaisuusContext.of(perusteId, dto.getId()));
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        VuosiluokkaKokonaisuus vk = kokonaisuusRepository.findOne(dto.getId());
        if ( sisalto.getPeruste().getTila() == PerusteTila.VALMIS ) {
            if ( !vk.getVuosiluokat().equals(dto.getVuosiluokat()) ) {
                throw new BusinessRuleViolationException("vain-korjaukset-sallittu");
            }
        }
        mapper.map(dto, vk);
        kokonaisuusRepository.setRevisioKommentti(updateDto.getMetadataOrEmpty().getKommentti());
        kokonaisuusRepository.save(vk);
        muokkausTietoService.addMuokkaustieto(perusteId, vk, MuokkausTapahtuma.PAIVITYS);
        return mapper.map(vk, VuosiluokkaKokonaisuusDto.class);
    }
}
