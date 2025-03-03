package fi.vm.sade.eperusteet.service.impl.yl;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.yl.*;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine.OsaTyyppi;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.*;
import fi.vm.sade.eperusteet.dto.yl.lukio.OppiaineJarjestysDto;
import fi.vm.sade.eperusteet.repository.*;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;

import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.LukioOpetussuunnitelmaRakenneLockContext;
import fi.vm.sade.eperusteet.service.yl.OppiaineLockContext;
import fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.vm.sade.eperusteet.domain.yl.Oppiaine.inLukioPeruste;
import static fi.vm.sade.eperusteet.service.util.OptionalUtil.found;
import static fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
@Transactional
public class OppiaineServiceImpl implements OppiaineService {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private OppiaineRepository oppiaineRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private OppiaineenVuosiluokkakokonaisuusRepository vuosiluokkakokonaisuusRepository;

    @Autowired
    private PerusopetuksenPerusteenSisaltoRepository perusOpetuksenSisaltoRepository;

    @Autowired
    private OpetuksenKohdeAlueRepository kohdeAlueRepository;

    @Autowired
    private PerusteenMuokkaustietoService muokkausTietoService;

    @Autowired
    @LockCtx(OppiaineLockContext.class)
    private LockService<OppiaineLockContext> lockService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    @LockCtx(LukioOpetussuunnitelmaRakenneLockContext.class)
    private LockService<LukioOpetussuunnitelmaRakenneLockContext> lukioRakenneLockService;

    private static final Logger LOG = LoggerFactory.getLogger(OppiaineServiceImpl.class);

    @Override
    @Transactional
    public OppiaineDto addOppiaine(Long perusteId, OppiaineDto dto, OppiaineOpetuksenSisaltoTyyppi tyyppi) {
        AbstractOppiaineOpetuksenSisalto sisalto = tyyppi.getLockedByPerusteId(applicationContext, perusteId);
        if (sisalto != null) {
            Oppiaine oppiaine = saveOppiaine(dto);
            sisalto.addOppiaine(oppiaine);
            muokkausTietoService.addMuokkaustieto(perusteId, oppiaine, MuokkausTapahtuma.LUONTI);
            return mapper.map(oppiaine, OppiaineDto.class);
        }
        throw new BusinessRuleViolationException("Perustetta ei ole");
    }

    private Oppiaine saveOppiaine(OppiaineBaseUpdateDto dto) {
        Oppiaine oppiaine = mapper.map(dto, new Oppiaine());
        oppiaine = oppiaineRepository.save(oppiaine);
        if (dto instanceof OppiaineDto) {
            final Set<OppiaineenVuosiluokkaKokonaisuusDto> vuosiluokkakokonaisuudet
                    = ((OppiaineDto) dto).getVuosiluokkakokonaisuudet();
            if (vuosiluokkakokonaisuudet != null) {
                for (OppiaineenVuosiluokkaKokonaisuusDto v : vuosiluokkakokonaisuudet) {
                    addOppiaineenVuosiluokkaKokonaisuus(oppiaine, v);
                }
            }
        }
        return oppiaine;
    }

    @Override
    @Transactional
    public OppiaineenVuosiluokkaKokonaisuusDto addOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId,
                                                                                   OppiaineenVuosiluokkaKokonaisuusDto dto) {
        PerusopetuksenPerusteenSisalto sisalto =  perusOpetuksenSisaltoRepository.findByPerusteId(perusteId);
        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);
        if (sisalto.containsOppiaine(aine)) {
            oppiaineRepository.lock(aine);
            return mapper.map(addOppiaineenVuosiluokkaKokonaisuus(aine, dto), OppiaineenVuosiluokkaKokonaisuusDto.class);
        } else {
            throw new BusinessRuleViolationException("oppiaine ei kuulu tähän perusteeseen");
        }
    }

    private OppiaineenVuosiluokkaKokonaisuus addOppiaineenVuosiluokkaKokonaisuus(Oppiaine aine,
                                                                                 OppiaineenVuosiluokkaKokonaisuusDto dto) {
        OppiaineenVuosiluokkaKokonaisuus ovk = mapper.map(dto, OppiaineenVuosiluokkaKokonaisuus.class);
        ovk.setId(null);
        aine.addVuosiluokkaKokonaisuus(ovk);
        ovk = vuosiluokkakokonaisuusRepository.save(ovk);
        return ovk;
    }

    @Override
    @Transactional
    public void deleteOppiaine(Long perusteId, Long oppiaineId, OppiaineOpetuksenSisaltoTyyppi tyyppi) {
        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);
        AbstractOppiaineOpetuksenSisalto sisalto = tyyppi.getRepository(applicationContext).findByPerusteId(perusteId);
        if (sisalto == null || !sisalto.containsOppiaine(aine)) {
            throw new BusinessRuleViolationException("Oppiainetta ei ole tai se ei kuulu tähän perusteeseen");
        }

        final OppiaineLockContext ctx = OppiaineLockContext.of(tyyppi, perusteId, oppiaineId, null);
        oppiaineRepository.lock(aine);
        lockService.lock(ctx);
        try {
            for (OppiaineenVuosiluokkaKokonaisuus k : aine.getVuosiluokkakokonaisuudet()) {
                deleteOppiaineenVuosiluokkaKokonaisuus(perusteId, oppiaineId, k.getId(), false);
            }

            if (aine.isKoosteinen()) {
                for (Oppiaine m : aine.getOppimaarat()) {
                    final OppiaineLockContext vkctx = OppiaineLockContext.of(tyyppi, perusteId, oppiaineId, null);
                    lockService.lock(vkctx);
                    deleteOppiaine(perusteId, m.getId(), tyyppi);
                }
            }
        } finally {
            lockService.unlock(ctx);
        }
        if (aine.getOppiaine() != null) {
            aine.getOppiaine().removeOppimaara(aine);
        } else {
            sisalto.removeOppiaine(aine);
        }
        if (sisalto instanceof LukiokoulutuksenPerusteenSisalto) {
            // Rakenteelle palautuspiste
            ((LukiokoulutuksenPerusteenSisalto) sisalto).getOpetussuunnitelma().setMuokattu(new Date());
        }
        oppiaineRepository.delete(aine);
        muokkausTietoService.addMuokkaustieto(perusteId, aine, MuokkausTapahtuma.POISTO);
    }

    @Override
    @Transactional
    public void deleteOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, Long vuosiluokkaKokonaisuusId) {
        deleteOppiaineenVuosiluokkaKokonaisuus(perusteId, oppiaineId, vuosiluokkaKokonaisuusId, true);
    }

    private void deleteOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, Long vuosiluokkaKokonaisuusId,
                                                        boolean lockOppiaine) {
        OppiaineenVuosiluokkaKokonaisuus vk = vuosiluokkakokonaisuusRepository.findOne(vuosiluokkaKokonaisuusId);
        PerusopetuksenPerusteenSisalto sisalto = perusOpetuksenSisaltoRepository.findByPerusteId(perusteId);
        if (sisalto == null || vk == null || !sisalto.containsOppiaine(vk.getOppiaine())) {
            throw new BusinessRuleViolationException("Virheellinen vuosiluokkakokonaisuus");
        }
        OppiaineLockContext ctx = OppiaineLockContext.of(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS,
                perusteId, oppiaineId, vk.getId());

        lockService.lock(ctx);
        if (lockOppiaine) {
            oppiaineRepository.lock(vk.getOppiaine());
        }
        try {
            for (OpetuksenTavoite t : vk.getTavoitteet()) {
                t.setSisaltoalueet(null);
                t.setLaajattavoitteet(null);
                t.setKohdealueet(null);
            }
        } finally {
            lockService.unlock(ctx);
        }
        vk.getOppiaine().removeVuosiluokkaKokonaisuus(vk);
        vuosiluokkakokonaisuusRepository.delete(vk);
    }

    @Override
    public OppiaineDto getOppiaine(long perusteId, long oppiaineId, OppiaineOpetuksenSisaltoTyyppi tyyppi) {
        AbstractOppiaineOpetuksenSisalto sisalto = tyyppi.getRepository(applicationContext).findByPerusteId(perusteId);
        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);

        if (sisalto != null && sisalto.containsOppiaine(aine)) {
            return mapper.map(aine, OppiaineDto.class);
        } else {
            throw new BusinessRuleViolationException(OPPIAINETTA_EI_OLE);
        }
    }


    @Override
    public OppiaineDto getOppiaine(long perusteId, long oppiaineId, int revisio, OppiaineOpetuksenSisaltoTyyppi tyyppi) {
        OppiaineSisaltoRepository<? extends AbstractOppiaineOpetuksenSisalto> repo = tyyppi.getRepository(applicationContext);
        AbstractOppiaineOpetuksenSisalto sisalto = repo.findByPerusteId(perusteId);
        Oppiaine original = oppiaineRepository.findOne(oppiaineId),
                aine = oppiaineRepository.findRevision(oppiaineId, revisio);
        if (sisalto != null && sisalto.containsOppiaine(original)) {
            OppiaineDto dto = mapper.map(aine, OppiaineDto.class);
            setOriginalReferences(original, dto);
            return dto;
        } else {
            throw new BusinessRuleViolationException(OPPIAINETTA_EI_OLE);
        }
    }

    private void setOriginalReferences(Oppiaine original, OppiaineBaseUpdateDto dto) {
        // keep the parent reference the same (could not haven been modified in perusopetus but could be
        // modified in lukiokoulutus through structure)
        dto.setOppiaine(original.getOppiaine() != null ? Optional.of(new Reference(original.getOppiaine().getId()))
                : Optional.empty());
    }

    @Override
    public List<Revision> getOppiaineRevisions(long perusteId, long oppiaineId, OppiaineOpetuksenSisaltoTyyppi tyyppi) {
        OppiaineSisaltoRepository<? extends AbstractOppiaineOpetuksenSisalto> repo = tyyppi.getRepository(applicationContext);
        AbstractOppiaineOpetuksenSisalto sisalto = repo.findByPerusteId(perusteId);
        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);
        if (sisalto != null && sisalto.containsOppiaine(aine)) {
            return oppiaineRepository.getRevisions(oppiaineId);
        } else {
            throw new BusinessRuleViolationException(OPPIAINETTA_EI_OLE);
        }
    }

    @Override
    public OppiaineDto revertOppiaine(long perusteId, long oppiaineId, int revisio, OppiaineOpetuksenSisaltoTyyppi tyyppi) {
        OppiaineSisaltoRepository<?> repository = tyyppi.getRepository(applicationContext);
        AbstractOppiaineOpetuksenSisalto sisalto = repository.findByPerusteId(perusteId);
        Oppiaine original = oppiaineRepository.findOne(oppiaineId),
                aine = oppiaineRepository.findRevision(oppiaineId, revisio);
        if (sisalto != null && sisalto.containsOppiaine(original)) {
            // we don't want to update oppimaarat (structure related), vuosiluokkakokonaisuudet etc. here
            Class<? extends OppiaineBaseUpdateDto> dtoClz
                    = tyyppi == LUKIOKOULUTUS ? LukioOppiaineUpdateDto.class : OppiaineDto.class;
            OppiaineBaseUpdateDto dto = mapper.map(aine, dtoClz);
            setOriginalReferences(original, dto);
            return updateOppiaine(perusteId, new UpdateDto<>(dto), tyyppi);
        } else {
            throw new BusinessRuleViolationException(OPPIAINETTA_EI_OLE);
        }
    }

    @Override
    public OppiaineenVuosiluokkaKokonaisuusDto getOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId,
                                                                                   Long vuosiluokkaKokonaisuusId) {
        PerusopetuksenPerusteenSisalto sisalto = perusOpetuksenSisaltoRepository.findByPerusteId(perusteId);
        OppiaineenVuosiluokkaKokonaisuus vk = sisalto == null ? null
            : vuosiluokkakokonaisuusRepository.findByIdAndOppiaineId(vuosiluokkaKokonaisuusId, oppiaineId);
        if (sisalto != null && vk != null && sisalto.containsOppiaine(vk.getOppiaine())) {
            return mapper.map(vk, OppiaineenVuosiluokkaKokonaisuusDto.class);
        } else {
            throw new BusinessRuleViolationException("Pyydettyä vuosiluokkakokonaisuutta ei ole");
        }
    }

    @Override
    public List<OppiaineSuppeaDto> getOppimaarat(Long perusteId, Long oppiaineId, OppiaineOpetuksenSisaltoTyyppi tyyppi) {
        AbstractOppiaineOpetuksenSisalto sisalto = tyyppi.getRepository(applicationContext).findByPerusteId(perusteId);
        Oppiaine oa = oppiaineRepository.findOne(oppiaineId);
        if (sisalto.containsOppiaine(oa)) {
            return mapper.mapAsList(oa.getOppimaarat(), OppiaineSuppeaDto.class);
        }
        throw new BusinessRuleViolationException(OPPIAINETTA_EI_OLE);
    }

    @Override
    @Transactional
    public <T extends OppiaineBaseUpdateDto> OppiaineDto updateOppiaine(Long perusteId, UpdateDto<T> updateDto,
                                                                        OppiaineOpetuksenSisaltoTyyppi tyyppi) {
        T dto = updateDto.getDto();
        Oppiaine aine = oppiaineRepository.findOne(dto.getId());
        AbstractOppiaineOpetuksenSisalto sisalto = tyyppi.getRepository(applicationContext).findByPerusteId(perusteId);
        if (aine == null || sisalto == null || !sisalto.containsOppiaine(aine)) {
            throw new NotExistsException("Oppiainetta ei ole");
        }
        lockService.assertLock(OppiaineLockContext.of(tyyppi, perusteId, dto.getId(), null));
        oppiaineRepository.lock(aine);

        Integer rev = null;
        if (sisalto.getPeruste().getTila() == PerusteTila.VALMIS) {
            rev = oppiaineRepository.getLatestRevisionId();
        }

        if (tyyppi == LUKIOKOULUTUS && dto.getPartial() != null && dto.getPartial()) {
            for (OsaTyyppi osaTyyppi : Oppiaine.OsaTyyppi.values()) {
                TekstiOsa osa = osaTyyppi.getter().apply(aine);
                TekstiOsaDto dtoOsa = dto.getOsa(osaTyyppi);
                if (osa != null && dtoOsa != null) {
                    mapper.map(dtoOsa, osa);
                }
            }
        } else {
            mapper.map(dto, aine);
            aine = oppiaineRepository.save(aine);
        }

        if (dto instanceof OppiaineDto) {
            OppiaineDto oppiaineDto = (OppiaineDto) dto;
            final Set<OppiaineenVuosiluokkaKokonaisuusDto> vuosiluokkakokonaisuudet
                    = oppiaineDto.getVuosiluokkakokonaisuudet();
            if (vuosiluokkakokonaisuudet != null) {
                final boolean valmis = sisalto.getPeruste().getTila() == PerusteTila.VALMIS;
                for (OppiaineenVuosiluokkaKokonaisuusDto v : vuosiluokkakokonaisuudet) {
                    if (v.getId() == null) {
                        if (valmis) {
                            throw new BusinessRuleViolationException(VAIN_KORJAUKSET_SALLITTU);
                        }
                        addOppiaineenVuosiluokkaKokonaisuus(aine, v);
                    } else {
                        final OppiaineLockContext vkctx = OppiaineLockContext.of(tyyppi,
                                perusteId, dto.getId(), v.getId());
                        try {
                            lockService.lock(vkctx);
                            if (sisalto instanceof PerusopetuksenPerusteenSisalto) {
                                doUpdateOppiaineenVuosiluokkaKokonaisuus(
                                        (PerusopetuksenPerusteenSisalto) sisalto, aine.getId(), v, false);
                            }
                        } finally {
                            lockService.unlock(vkctx);
                        }
                    }
                }
            }
        }
        if (rev != null) {
            Oppiaine latest = oppiaineRepository.findRevision(aine.getId(), rev);
            if (!latest.structureEquals(aine)) {
                throw new BusinessRuleViolationException(VAIN_KORJAUKSET_SALLITTU);
            }
        }
        oppiaineRepository.setRevisioKommentti(updateDto.getMetadataOrEmpty().getKommentti());
        aine = oppiaineRepository.save(aine);
        oppiaineRepository.setRevisioKommentti("Muokattu oppiainetta " + (aine.getNimi() != null ? aine.getNimi().toString() : ""));
        muokkausTietoService.addMuokkaustieto(perusteId, aine, MuokkausTapahtuma.PAIVITYS);
        return mapper.map(aine, OppiaineDto.class);
    }

    @Override
    @Transactional
    public void updateOppiaineJarjestys(Long perusteId, List<OppiaineSuppeaDto> oppiaineet) {
        PerusopetuksenPerusteenSisalto sisalto =  perusOpetuksenSisaltoRepository.findByPerusteId(perusteId);
        sisalto.getOppiaineet().forEach(sisallonOppiaine -> {
            OppiaineSuppeaDto oppiaineDto = oppiaineet.stream().filter(oppiaine -> oppiaine.getId().equals(sisallonOppiaine.getId())).findFirst().get();
            sisallonOppiaine.setJnro((long)oppiaineet.indexOf(oppiaineDto));
        });
    }

    @Override
    @Transactional
    public OppiaineenVuosiluokkaKokonaisuusDto updateOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, UpdateDto<OppiaineenVuosiluokkaKokonaisuusDto> updateDto) {
        PerusopetuksenPerusteenSisalto sisalto = perusOpetuksenSisaltoRepository.findByPerusteId(perusteId);
        OppiaineenVuosiluokkaKokonaisuusDto tmp = mapper.map(doUpdateOppiaineenVuosiluokkaKokonaisuus(sisalto, oppiaineId, updateDto.getDto(), true), OppiaineenVuosiluokkaKokonaisuusDto.class);
        vuosiluokkakokonaisuusRepository.setRevisioKommentti(updateDto.getMetadataOrEmpty().getKommentti());

        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);
        muokkausTietoService.addMuokkaustieto(perusteId, aine, MuokkausTapahtuma.PAIVITYS);
        return tmp;
    }

    private OppiaineenVuosiluokkaKokonaisuus doUpdateOppiaineenVuosiluokkaKokonaisuus(
            PerusopetuksenPerusteenSisalto sisalto,  Long oppiaineId, OppiaineenVuosiluokkaKokonaisuusDto dto,
            boolean lock) {
        OppiaineenVuosiluokkaKokonaisuus ovk = vuosiluokkakokonaisuusRepository.findByIdAndOppiaineId(dto.getId(), oppiaineId);
        if (ovk == null) {
            throw new BusinessRuleViolationException("Vuosiluokkakokonaisuus ei kuulu tähän oppiaineeseen");
        }
        lockService.assertLock(OppiaineLockContext.of(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS,
                sisalto.getPeruste().getId(), oppiaineId, dto.getId()));
        if (lock) {
            oppiaineRepository.lock(ovk.getOppiaine());
        }

        //hibernate deletoi ja updatee väärässä järjestyksessä jos @ordercolumn ja @jointable
        List<Long> poistuneetTavoitteet = ovk.getTavoitteet().stream()
                .map(OpetuksenTavoite::getId)
                .filter(id -> !dto.getTavoitteet().stream().map(OpetuksenTavoiteDto::getId).collect(Collectors.toList()).contains(id))
                        .collect(Collectors.toList());
        ovk.setTavoitteet(
                Stream.concat(
                        ovk.getTavoitteet().stream().filter(tavoite -> !poistuneetTavoitteet.contains(tavoite.getId())),
                        ovk.getTavoitteet().stream().filter(tavoite -> poistuneetTavoitteet.contains(tavoite.getId()))
                        ).collect(Collectors.toList()));

        ovk = vuosiluokkakokonaisuusRepository.saveAndFlush(ovk);
        mapper.map(dto, ovk);

        Set<Long> sisaltoalueetIdt = ovk.getSisaltoalueet().stream().map(KeskeinenSisaltoalue::getId).collect(Collectors.toSet());
        ovk.setTavoitteet(ovk.getTavoitteet().stream().peek(tavoite -> tavoite.setSisaltoalueet(tavoite.getSisaltoalueet().stream()
                .filter(sisaltoalue -> sisaltoalueetIdt.contains(sisaltoalue.getId()))
                .collect(Collectors.toSet()))).collect(Collectors.toList()));
        ovk = vuosiluokkakokonaisuusRepository.save(ovk);
        ovk.getOppiaine().muokattu();
        oppiaineRepository.setRevisioKommentti("Muokattu oppiaineen vuosiluokkakokonaisuutta");
        return ovk;
    }

    private Oppiaine getAndLockOppiaine(Long perusteId, Long oppiaineId) {
        PerusopetuksenPerusteenSisalto sisalto = perusOpetuksenSisaltoRepository.findByPerusteId(perusteId);
        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);
        if (sisalto == null || !sisalto.containsOppiaine(aine)) {
            throw new BusinessRuleViolationException("oppiaine ei kuulu tähän perusteeseen");
        }
        oppiaineRepository.lock(aine);
        return aine;
    }

    @Override
    @Transactional
    public OpetuksenKohdealueDto addKohdealue(Long perusteId, Long oppiaineId, OpetuksenKohdealueDto kohdealue) {
        Oppiaine aine = getAndLockOppiaine(perusteId, oppiaineId);
        OpetuksenKohdealue kohde = null;
        if (kohdealue.getId() != null) {
            OpetuksenKohdealue vanhaKohde = kohdeAlueRepository.findById(kohdealue.getId()).orElse(null);
            mapper.map(kohdealue, vanhaKohde);
        } else {
            kohde = mapper.map(kohdealue, OpetuksenKohdealue.class);
            kohde = aine.addKohdealue(kohde);
            kohdeAlueRepository.save(kohde);
            oppiaineRepository.save(aine);
        }

        muokkausTietoService.addMuokkaustieto(perusteId, aine, MuokkausTapahtuma.PAIVITYS);
        return mapper.map(kohde, OpetuksenKohdealueDto.class);
    }

    @Override
    @Transactional
    public List<OpetuksenKohdealueDto> updateKohdealueet(Long perusteId, Long oppiaineId, List<OpetuksenKohdealueDto> kohdealueetDto) {
        Oppiaine aine = getAndLockOppiaine(perusteId, oppiaineId);

        Set<OpetuksenKohdealue> kohdealueet = Sets.newHashSet(mapper.mapAsList(kohdealueetDto, OpetuksenKohdealue.class));
        aine.setKohdealueet(kohdealueet);
        kohdeAlueRepository.saveAll(kohdealueet);
        oppiaineRepository.save(aine);

        muokkausTietoService.addMuokkaustieto(perusteId, aine, MuokkausTapahtuma.PAIVITYS);
        return mapper.mapAsList(kohdealueet, OpetuksenKohdealueDto.class);
    }

    @Override
    @Transactional
    public void deleteKohdealue(Long perusteId, Long id, Long kohdealueId) {
        getAndLockOppiaine(perusteId, id);
        OpetuksenKohdealue kohdealue = kohdeAlueRepository.getOne(kohdealueId);
        List<OpetuksenTavoite> tavoitteet = oppiaineRepository.findAllTavoitteetByKohdealue(kohdealue);
        for (OpetuksenTavoite t : tavoitteet) {
            t.getKohdealueet().remove(kohdealue);
        }
        Oppiaine oppiaine = oppiaineRepository.findById(id).orElse(null);
        oppiaine.removeKohdealue(kohdealue);
        muokkausTietoService.addMuokkaustieto(perusteId, oppiaine, MuokkausTapahtuma.PAIVITYS);
    }

    @Override
    @Transactional
    public void reArrangeLukioOppiaineet(long perusteId, List<OppiaineJarjestysDto> oppiaineet, Integer tryRestoreFromRevision) {
        lukioRakenneLockService.assertLock(new LukioOpetussuunnitelmaRakenneLockContext(perusteId));
        Map<Long, OppiaineJarjestysDto> dtosById = oppiaineet.stream().collect(toMap(OppiaineJarjestysDto::getId, o -> o));
        Set<Long> oppiaineIds = new HashSet<>(dtosById.keySet());
        LukiokoulutuksenPerusteenSisalto sisalto = (LukiokoulutuksenPerusteenSisalto)
                LUKIOKOULUTUS.getRepository(applicationContext).findByPerusteId(perusteId);
        oppiaineIds.addAll(oppiaineet.stream().filter(oa -> oa.getOppiaineId() != null)
                .map(OppiaineJarjestysDto::getOppiaineId).collect(toSet()));
        Map<Long, Oppiaine> byId = oppiaineRepository.findAllById(oppiaineIds).stream().collect(toMap(Oppiaine::getId, o -> o));
        dtosById.values().stream().sorted(comparing(OppiaineJarjestysDto::getOppiaineId, nullsFirst(naturalOrder()))).forEach(dto -> {
            Oppiaine oa = lookupOrRestoreOppiaine(perusteId, dto.getId(), tryRestoreFromRevision, byId, sisalto);
            oppiaineRepository.lock(oa);
            oa.setJnro(dto.getJarjestys());
            if (!oa.isKoosteinen() && dto.getOppiaineId() != null) {
                oa.setOppiaineForce(found(lookupOrRestoreOppiaine(perusteId, dto.getOppiaineId(),
                                tryRestoreFromRevision, byId, sisalto), Oppiaine::isKoosteinen,
                        () -> new NotExistsException("No koosteinen Oppiaine found as parent in peruste "
                                + " by id="+dto.getOppiaineId())));
            } else {
                oa.setOppiaine(null);
            }
        });
    }

    private Oppiaine lookupOrRestoreOppiaine(long perusteId, Long id, Integer tryRestoreFromRevision,
                                   Map<Long, Oppiaine> byId,
                                   LukiokoulutuksenPerusteenSisalto sisalto) {
        if (tryRestoreFromRevision != null && byId.get(id) == null) {
            Oppiaine oldOppiaine = found(oppiaineRepository.findRevision(id,
                    tryRestoreFromRevision), inLukioPeruste(perusteId),
                    () -> new NotExistsException("No Oppiaine for id "+id
                            +" to restore from Lukioperuste at revision="+tryRestoreFromRevision));
            LukioOppiaineUpdateDto lukioOppiaine = mapper.map(oldOppiaine, new LukioOppiaineUpdateDto());
            if (oldOppiaine.getOppiaine() != null) {
                lukioOppiaine.setOppiaine(Optional.of(new Reference(
                        lookupOrRestoreOppiaine(perusteId, oldOppiaine.getOppiaine().getId(),
                                tryRestoreFromRevision, byId, sisalto).getId())));
            }
            Oppiaine newOppiaine = saveOppiaine(lukioOppiaine);
            if (newOppiaine.getOppiaine() == null) {
                sisalto.addOppiaine(newOppiaine);
                // NOTE: would cause duplicates here but this reference is not visible (don't check it)
                //newOppiaine.getLukioRakenteet().add(sisalto.getOpetussuunnitelma());
            }
            oppiaineRepository.flush();
            Long newId = newOppiaine.getId();
            Oppiaine newOppinaine = found(oppiaineRepository.findOne(newId));
            byId.put(id, newOppinaine);
            byId.put(newId, newOppinaine); // new id differs
            return newOppinaine;
        }
        return found(byId.get(id), inLukioPeruste(perusteId),
                () -> new NotExistsException("No Oppiaine found in lukioperuste by id="+id));
    }

    private static final String OPPIAINETTA_EI_OLE = "Pyydettyä oppiainetta ei ole";
    private static final String VAIN_KORJAUKSET_SALLITTU = "vain-korjaukset-sallittu";
}
