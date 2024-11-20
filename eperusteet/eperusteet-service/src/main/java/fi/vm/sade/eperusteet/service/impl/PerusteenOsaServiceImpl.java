package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.ammattitaitovaatimukset.AmmattitaitovaatimuksenKohdealue;
import fi.vm.sade.eperusteet.domain.permissions.PerusteenosanProjekti;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Osaamistavoite;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.ValmaTelmaSisalto;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.KommenttiDto;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.*;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektinPerusteenosaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.*;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteKontekstiDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.repository.*;
import fi.vm.sade.eperusteet.repository.authorization.PerusteprojektiPermissionRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.KommenttiService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.event.PerusteUpdatedEvent;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.internal.LockManager;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PerusteenOsaServiceImpl implements PerusteenOsaService {

    @Autowired
    private TutkinnonOsaRepositoryCustom tutkinnonOsaRepositoryCustom;

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

    @Autowired
    private TutkinnonOsaViiteRepository tutkinnonOsaViiteRepository;

    @Autowired
    private PerusteenOsaViiteRepository perusteenOsaViiteRepository;

    @Autowired
    private PerusteRepository perusteet;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private AmmattitaidonvaatimusRepository ammattitaidonvaatimusRepository;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private LockManager lockManager;

    @Autowired
    private ValmaTelmaSisaltoRepository valmaTelmaSisaltoRepository;

    @Autowired
    private PerusteprojektiPermissionRepository perusteprojektiPermissionRepository;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private PerusteenMuokkaustietoService muokkausTietoService;

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;

    @Override
    @Transactional(readOnly = true)
    public List<PerusteenOsaDto.Suppea> getAll() {
        return mapper.mapAsList(perusteenOsaRepo.findAll(), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Suppea.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteenOsaDto.Laaja get(final Long id) {
        return mapper.map(perusteenOsaRepo.findById(id).orElse(null), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteenOsaDto.Laaja getByViite(final Long viiteId) {
        PerusteenOsaViite viite = perusteenOsaViiteRepository.findOne(viiteId);
        if (viite == null || viite.getPerusteenOsa() == null) {
            throw new BusinessRuleViolationException("Virheellinen viiteId");
        }
        return mapper.map(viite.getPerusteenOsa(), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteenOsaViiteDto.Laaja getByViiteDeep(Long viiteId) {
        PerusteenOsaViite viite = perusteenOsaViiteRepository.findOne(viiteId);
        if (viite == null || viite.getPerusteenOsa() == null) {
            throw new BusinessRuleViolationException("Virheellinen viiteId");
        }
        return mapper.map(viite, PerusteenOsaViiteDto.Laaja.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getLatestRevision(final Long id) {
        return perusteenOsaRepo.getLatestRevisionId(id).getNumero();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerusteenOsaDto.Laaja> getAllByKoodiUri(final String koodiUri) {
        return mapper.mapAsList(tutkinnonOsaRepo.findByKoodiUri(koodiUri), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja.class);
    }

    @Override
    @Transactional(readOnly = true)
    public void onkoTutkinnonOsanKoodiKaytossa(final String koodiUri) {
        List<TutkinnonOsa> tosatByKoodi = tutkinnonOsaRepo.findByKoodiUri(koodiUri);
        for (TutkinnonOsa tosa : tosatByKoodi) {
            if (tosa.getTila() == PerusteTila.VALMIS) {
                throw new BusinessRuleViolationException("Tutkinnon osan koodi on jo käytössä");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Boolean> onkoTutkinnonOsanKoodiKaytossa(final List<String> koodiUrit) {
        Map<String, Boolean> booleanMap = koodiUrit.stream().collect(Collectors.toMap(koodiUri -> koodiUri, koodiUri -> false));
        List<TutkinnonOsa> tosatByKoodi = tutkinnonOsaRepo.findByKoodiUriIn(koodiUrit);
        for (TutkinnonOsa tosa : tosatByKoodi) {
            if (tosa.getTila() == PerusteTila.VALMIS) {
                booleanMap.put(tosa.getKoodi().getUri(), true);
            }
        }

        return booleanMap;
    }

    @Override
    @Transactional
    public <T extends PerusteenOsaDto.Laaja> T update(T perusteenOsaDto) {
        assertExists(perusteenOsaDto.getId());
        lockManager.ensureLockedByAuthenticatedUser(perusteenOsaDto.getId());
        PerusteenOsa current = perusteenOsaRepo.findOne(perusteenOsaDto.getId());

        perusteenOsaDto.setTila(current.getTila());
        PerusteenOsa updated = mapper.map(perusteenOsaDto, current.getType());

        if (perusteenOsaDto.getClass().equals(TutkinnonOsaDto.class)) {
            TutkinnonOsa tutkinnonOsa = (TutkinnonOsa) updated;
            tutkinnonOsa.setOsaAlueet(createOsaAlueIfNotExist(tutkinnonOsa.getOsaAlueet()));
            tutkinnonOsa.setValmaTelmaSisalto( createValmatelmaIfNotExist( tutkinnonOsa.getValmaTelmaSisalto() ) );
        }

        if (perusteenOsaDto.getClass().equals(TutkinnonOsaDto.class)) {
            removeMissingFromCurrent(perusteenOsaRepo.findOne(perusteenOsaDto.getId()), updated);
        }

        current.mergeState(updated);
        current = perusteenOsaRepo.save(current);
        notifyUpdate(current);
        mapper.map(current, perusteenOsaDto);
        return perusteenOsaDto;
    }

    private ValmaTelmaSisalto createValmatelmaIfNotExist(ValmaTelmaSisalto valmaTelmaSisalto) {
        ValmaTelmaSisalto tmp = null;

        if (valmaTelmaSisalto != null) {

            if (valmaTelmaSisalto.getId() == null) {
                tmp = valmaTelmaSisaltoRepository.save(valmaTelmaSisalto);
            }else{
                tmp = valmaTelmaSisalto;
            }
        }
        return tmp;
    }

    private void removeMissingFromCurrent(PerusteenOsa current, PerusteenOsa updated) {

        TutkinnonOsa tutkinnonOsa = (TutkinnonOsa) current;
        TutkinnonOsa updatedTutkinnonOsa = (TutkinnonOsa) updated;

        tutkinnonOsa.getAmmattitaitovaatimuksetLista().removeAll(updatedTutkinnonOsa.getAmmattitaitovaatimuksetLista());
        for (AmmattitaitovaatimuksenKohdealue ammattitaitovaatimuksenKohdealue : tutkinnonOsa.getAmmattitaitovaatimuksetLista()) {
            ammattitaidonvaatimusRepository.deleteById( ammattitaitovaatimuksenKohdealue.getId() );
        }

    }

    @Override
    @Transactional(readOnly = false)
    public <T extends PerusteenOsaDto.Laaja> T update(UpdateDto<T> perusteenOsaDto) {
        T updated = update(perusteenOsaDto.getDto());
        createMuokkaustieto(updated.getId());
        perusteenOsaRepo.setRevisioKommentti(perusteenOsaDto.getMetadataOrEmpty().getKommentti());
        return updated;
    }

    @Override
    @Transactional(readOnly = false)
    public <T extends PerusteenOsaDto.Laaja> T update(Long perusteId, Long viiteId, UpdateDto<T> perusteenOsaDto) {

        if (perusteenOsaDto.getDto().getClass().equals(TekstiKappaleDto.class)) {
            PerusteenOsa perusteenOsa = perusteenOsaRepo.findOne(perusteenOsaDto.getDto().getId());

            Set<PerusteenosanProjekti> perusteenosanProjektit = perusteprojektiPermissionRepository.findAllByPerusteenosa(perusteenOsa.getId());
            Peruste peruste = perusteet.findOne(perusteId);
            boolean tekstikappaleKopioitava = perusteenosanProjektit.stream()
                    .filter(pop -> !pop.getPerusteProjektiId().equals(peruste.getPerusteprojekti().getId()))
                    .anyMatch(projekti -> !projekti.getTila().equals(ProjektiTila.POISTETTU));

            if (tekstikappaleKopioitava) {
                PerusteenOsaViiteDto.Laaja viite = perusteenOsaViiteService.kloonaaTekstiKappale(perusteId, viiteId);
                lockManager.unlock(perusteenOsaDto.getDto().getId());
                lockManager.lock(viite.getPerusteenOsa().getId());
                perusteenOsaDto.getDto().setId(viite.getPerusteenOsa().getId());
            }
        }

        T updated = update(perusteenOsaDto.getDto());
        perusteenOsaRepo.setRevisioKommentti(perusteenOsaDto.getMetadataOrEmpty().getKommentti());
        muokkausTietoService.addMuokkaustieto(perusteId, perusteenOsaViiteRepository.findOne(viiteId), MuokkausTapahtuma.PAIVITYS);
        return updated;
    }

    @Transactional
    private <T extends PerusteenOsaDto.Laaja> T addImpl(PerusteenOsaViite viite, T perusteenOsaDto) {
        PerusteenOsa perusteenOsa = mapper.map(perusteenOsaDto, PerusteenOsa.class);
        viite.setPerusteenOsa(perusteenOsa);
        perusteenOsa = perusteenOsaRepo.saveAndFlush(perusteenOsa);
        mapper.map(perusteenOsa, perusteenOsaDto);
        return perusteenOsaDto;
    }

    @Override
    @Transactional
    public <T extends PerusteenOsaDto.Laaja> T add(PerusteenOsaViite viite, T perusteenOsaDto) {
        return addImpl(viite, perusteenOsaDto);
    }

    @Override
    @Transactional
    public <T extends PerusteenOsaDto.Laaja> T addJulkaistuun(PerusteenOsaViite viite, T perusteenOsaDto) {
        return addImpl(viite, perusteenOsaDto);
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
        TutkinnonOsa tutkinnonOsa = tutkinnonOsaRepo.findById(id).orElse(null);
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
    public OsaAlueKokonaanDto getTutkinnonOsaOsaAlue(Long viiteId, Long osaAlueId) {
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(viiteId);
        if (viite == null || viite.getTutkinnonOsa() == null || viite.getTutkinnonOsa().getOsaAlueet() == null) {
            throw new BusinessRuleViolationException("Virheellinen viiteId");
        }

        List<Long> osaAlueIds = new ArrayList<>();
        for (OsaAlue osaAlue : viite.getTutkinnonOsa().getOsaAlueet()) {
            osaAlueIds.add(osaAlue.getId());
        }
        if (!osaAlueIds.contains(osaAlueId)) {
            throw new BusinessRuleViolationException("Virheellinen osaAlueId");
        }

        return mapper.map(osaAlueRepository.findOne(osaAlueId), OsaAlueKokonaanDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public OsaAlueKokonaanDto updateTutkinnonOsaOsaAlue(Long viiteId, Long osaAlueId, OsaAlueKokonaanDto osaAlue) {

        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(viiteId);
        if (viite == null || viite.getTutkinnonOsa() == null || viite.getTutkinnonOsa().getOsaAlueet() == null) {
            throw new BusinessRuleViolationException("Virheellinen viiteId");
        }

        Long id = viite.getTutkinnonOsa().getId();
        assertExists(id);
        lockManager.ensureLockedByAuthenticatedUser(id);
        OsaAlue osaAlueEntity = osaAlueRepository.findOne(osaAlueId);
        if (osaAlueEntity == null) {
            throw new EntityNotFoundException("Osa-aluetta ei löytynyt id:llä: " + osaAlueId);
        }
        List<Osaamistavoite> uudetTavoitteet = tallennaUudetOsaamistavoitteet(osaAlue.getOsaamistavoitteet());

        OsaAlue osaAlueTmp = mapper.map(osaAlue, OsaAlue.class);
        osaAlueEntity.mergeState(osaAlueTmp);
        osaAlueEntity.getOsaamistavoitteet().addAll(uudetTavoitteet);
        osaAlueEntity.setValmaTelmaSisalto( createValmatelmaIfNotExist( osaAlueTmp.getValmaTelmaSisalto() ));
        osaAlueRepository.save(osaAlueEntity);

        aiheutaUusiTutkinnonOsaViiteRevisio(viiteId);
        notifyUpdate(viite.getTutkinnonOsa());
        return mapper.map(osaAlueEntity, OsaAlueKokonaanDto.class);
    }

    private void notifyUpdate(PerusteenOsa osa) {
         // Varmistetaan että tutkinnon osan muokkaus muuttaa valmiiden perusteiden viimeksi muokattu -päivämäärää (ja aiheuttaa uuden version).
        if (osa.getTila() == PerusteTila.VALMIS) {
            Set<Long> perusteIds;
            if ( osa instanceof TutkinnonOsa ) {
                perusteIds = perusteet.findByTutkinnonosaId(osa.getId(), PerusteTila.VALMIS);
            } else {
                final List<Long> roots = perusteenOsaViiteRepository.findRootsByPerusteenOsaId(osa.getId());
                perusteIds = roots.isEmpty() ? Collections.<Long>emptySet() : perusteet.findBySisaltoRoots(roots, PerusteTila.VALMIS);
            }
            for (Long perusteId : perusteIds) {
                eventPublisher.publishEvent(PerusteUpdatedEvent.of(this, perusteId));
            }
        }
    }

    private List<AmmattitaitovaatimuksenKohdealue> connectAmmattitaitovaatimusListToOsaamistavoite(Osaamistavoite tavoite) {
        for (AmmattitaitovaatimuksenKohdealue ammattitaitovaatimuksenKohdealue : tavoite.getAmmattitaitovaatimuksetLista()) {
            ammattitaitovaatimuksenKohdealue.connectAmmattitaitovaatimuksetToKohdealue( ammattitaitovaatimuksenKohdealue );
        }
        return tavoite.getAmmattitaitovaatimuksetLista();
    }

    @Transactional(readOnly = false)
    private List<Osaamistavoite> tallennaUudetOsaamistavoitteet(List<OsaamistavoiteLaajaDto> osaamistavoitteet) {
        List<Osaamistavoite> uudet = new ArrayList<>();

        Long tempId;
        Osaamistavoite tallennettuPakollinenTavoite;
        Iterator<OsaamistavoiteLaajaDto> osaamistavoiteDtoItr = osaamistavoitteet.iterator();
        // Tallennetaan uudet pakolliset osaamistavoitteet ja asetetaan valinnaisten osaamistavoitteiden esitietoid:t oikeiksi kantaId:ksi
        while (osaamistavoiteDtoItr.hasNext()) {
            OsaamistavoiteLaajaDto osaamistavoiteDto = osaamistavoiteDtoItr.next();
            // Jos id >= 0, niin kyseessä oikea tietokanta id. Id:t < 0 ovat generoitu UI päässä.
            if (osaamistavoiteDto.isPakollinen() && (osaamistavoiteDto.getId() == null || osaamistavoiteDto.getId() < 0)) {
                tempId = osaamistavoiteDto.getId();
                osaamistavoiteDto.setId(null);
                tallennettuPakollinenTavoite = mapper.map(osaamistavoiteDto, Osaamistavoite.class);
                connectAmmattitaitovaatimusListToOsaamistavoite(tallennettuPakollinenTavoite);
                tallennettuPakollinenTavoite = osaamistavoiteRepository.save( tallennettuPakollinenTavoite );
                uudet.add(tallennettuPakollinenTavoite);

                // käydään läpi valinnaiset ja asetetaan esitieto id kohdalleen.
                for (OsaamistavoiteLaajaDto osaamistavoiteValinnainenDto : osaamistavoitteet) {
                    if (!osaamistavoiteValinnainenDto.isPakollinen() && osaamistavoiteValinnainenDto.getEsitieto() != null) {
                        if (osaamistavoiteValinnainenDto.getEsitieto().getId().equals(tempId != null ? tempId.toString() : null)) {
                            osaamistavoiteValinnainenDto.setEsitieto(new Reference(tallennettuPakollinenTavoite.getId()));
                        }
                    }
                }
                osaamistavoiteDtoItr.remove();
            }
        }

        // Käydään vielä läpi valinnaiset osaamistavoitteet ja tallennetaan uudet.
        osaamistavoiteDtoItr = osaamistavoitteet.iterator();
        while (osaamistavoiteDtoItr.hasNext()) {
            OsaamistavoiteLaajaDto osaamistavoiteDto = osaamistavoiteDtoItr.next();
            // Jos id >= 0, niin kyseessä oikea tietokanta id. Id:t < 0 ovat generoitu UI päässä.
            if (!osaamistavoiteDto.isPakollinen() && (osaamistavoiteDto.getId() == null || osaamistavoiteDto.getId() < 0)) {
                osaamistavoiteDto.setId(null);

                tallennettuPakollinenTavoite = mapper.map(osaamistavoiteDto, Osaamistavoite.class);
                connectAmmattitaitovaatimusListToOsaamistavoite(tallennettuPakollinenTavoite);
                tallennettuPakollinenTavoite = osaamistavoiteRepository.save( tallennettuPakollinenTavoite );
                uudet.add(tallennettuPakollinenTavoite);
                osaamistavoiteDtoItr.remove();
            }
        }

        return uudet;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OsaAlueKokonaanDto> getTutkinnonOsaOsaAlueet(Long id) {
        TutkinnonOsa tutkinnonOsa = tutkinnonOsaRepo.findById(id).orElse(null);
        if (tutkinnonOsa == null) {
            throw new EntityNotFoundException("Tutkinnon osaa ei löytynyt id:llä: " + id);
        }

        return mapper.mapAsList(tutkinnonOsa.getOsaAlueet(), OsaAlueKokonaanDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OsaAlueKokonaanDto> getTutkinnonOsaOsaAlueetVersio(Long id, Integer versioId) {
        TutkinnonOsa t = tutkinnonOsaRepo.findRevision(id, versioId);
        if (t == null) {
            throw new EntityNotFoundException("Tutkinnon osa (id: " + id + ") versiota ei löytynyt versioId:llä " + versioId);
        }
        return mapper.mapAsList(t.getOsaAlueet(), OsaAlueKokonaanDto.class);
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
        TutkinnonOsa tutkinnonOsa = tutkinnonOsaRepo.findById(id).orElse(null);
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
            return null;
        }
        return mapper.mapAsList(osaAlue.getOsaamistavoitteet(), OsaamistavoiteLaajaDto.class);

    }

    @Override
    @Transactional(readOnly = false)
    public void removeOsaamistavoite(Long id, Long osaAlueId, Long osaamistavoiteId) {
        assertExists(id);
        lockManager.ensureLockedByAuthenticatedUser(id);
        PerusteenOsa current = perusteenOsaRepo.findById(id).orElse(null);
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
        PerusteenOsa current = perusteenOsaRepo.findById(id).orElse(null);
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
        notifyUpdate(perusteenOsaRepo.findById(id).orElse(null));
        return mapper.map(osaamistavoiteEntity, OsaamistavoiteLaajaDto.class);
    }

    @Override
    public void delete(final Long id) {
        assertExists(id);
        PerusteenOsa current = perusteenOsaRepo.findById(id).orElse(null);
        lockManager.lock(id);

        try {
            List<KommenttiDto> allByPerusteenOsa = kommenttiService.getAllByPerusteenOsa(id);
            for (KommenttiDto kommentti : allByPerusteenOsa) {
                kommenttiService.deleteReally(kommentti.getId());
            }
            perusteenOsaRepo.deleteById(id);
        } finally {
            lockManager.unlock(id);
        }
    }

    @Override
    public void delete(final Long id, final Long perusteId) {
        delete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Suppea> getAllWithName(String name) {
        return mapper
                .mapAsList(tutkinnonOsaRepo.findByNimiTekstiTekstiContainingIgnoreCase(name), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Suppea.class);
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
    @Transactional(readOnly = true)
    public List<Revision> getVersiotByViite(Long id) {
        PerusteenOsaViite p = perusteenOsaViiteRepository.findById(id).orElse(null);
        if (p == null || p.getPerusteenOsa() == null) {
            throw new EntityNotFoundException("Perusteen osaa ei löytynyt viite id:llä: " + id);
        }
        return getVersiot(p.getPerusteenOsa().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteenOsaDto getVersioByViite(Long id, Integer versioId) {
        PerusteenOsaViite p = perusteenOsaViiteRepository.findById(id).orElse(null);
        if (p == null || p.getPerusteenOsa() == null) {
            throw new EntityNotFoundException("Perusteen osaa ei löytynyt viite id:llä: " + id);
        }
        return getVersio(p.getPerusteenOsa().getId(), versioId);
    }

    @Override
    @Transactional
    public PerusteenOsaDto.Laaja revertToVersio(Long id, Integer versioId) {
        PerusteenOsa revision = perusteenOsaRepo.findRevision(id, versioId);
        PerusteenOsaDto.Laaja updated = update(mapper.map(revision, PerusteenOsaDto.Laaja.class));
        createMuokkaustieto(updated.getId());
        return updated;
    }

    private void createMuokkaustieto(Long id) {
        PerusteenOsa lisatty = perusteenOsaRepo.findById(id).orElse(null);
        if (lisatty != null && !lisatty.getViitteet().isEmpty()) {
            Peruste peruste = lisatty.getViitteet().stream().findFirst().get().getPeruste();
            Long viiteId = lisatty.getViitteet().stream().findFirst().get().getId();
            if (peruste != null) {
                muokkausTietoService.addMuokkaustieto(peruste.getId(), perusteenOsaViiteRepository.findOne(viiteId), MuokkausTapahtuma.PAIVITYS);
            }
        }
    }

    @Override
    public LukkoDto lock(Long id) {
        assertExists(id);
        return LukkoDto.of(lockManager.lock(id));
    }

    @Override
    public void unlock(Long id) {
        lockManager.unlock(id);
    }

    @Override
    public LukkoDto getLock(Long id) {
        assertExists(id);
        LukkoDto lukko = LukkoDto.of(lockManager.getLock(id));
        lockManager.lisaaNimiLukkoon(lukko);
        return lukko;
    }

    @Override
    @Transactional(readOnly = true)
    public Revision getLastModifiedRevision(final Long id) {
        PerusteTila tila = perusteenOsaRepo.getTila(id);
        if (tila == null) {
            return null;
        }
        if (tila == PerusteTila.LUONNOS) {
            //luonnos-tilassa olevan perusteen viimeisimmän muokkauksen määrittäminen on epäluotettavaa.
            return Revision.DRAFT;
        }
        return perusteenOsaRepo.getLatestRevisionId(id);
    }


    private void assertExists(Long id) {
        if (!perusteenOsaRepo.existsById(id)) {
            throw new NotExistsException("Pyydettyä perusteen osaa ei ole olemassa");
        }
    }

    private void aiheutaUusiTutkinnonOsaViiteRevisio(Long id) {
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findById(id).orElse(null);
        if (viite != null) {
            viite.setMuokattu(new Date());
            tutkinnonOsaViiteRepository.save(viite);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Set<PerusteprojektinPerusteenosaDto> getOwningProjektit(Long id) {
        return perusteprojektiPermissionRepository.findAllByPerusteenosa(id).stream()
                .map(pp -> pp.getPerusteProjektiId())
                .map(perusteProjektiId -> perusteprojektiRepository.findById(perusteProjektiId).orElseThrow())
                .map(pp -> mapper.map(pp, PerusteprojektinPerusteenosaDto.class))
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TutkinnonOsaDto> findTutkinnonOsatBy(TutkinnonOsaQueryDto pquery) {
        if (StringUtils.isEmpty(pquery.getKoodiUri())) {
            throw new BusinessRuleViolationException("koodiUri on pakollinen");
        }
        PageRequest p = PageRequest.of(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return tutkinnonOsaRepositoryCustom.findBy(p, pquery)
                .map((osa) -> mapper.map(osa, TutkinnonOsaDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TutkinnonOsaViiteKontekstiDto> findAllTutkinnonOsatBy(TutkinnonOsaQueryDto pquery) {
        Pageable pageable = PageRequest.of(pquery.getSivu(), pquery.getSivukoko(), Sort.by(Sort.Direction.fromString("ASC"), "teksti.teksti"));

        Page<TutkinnonOsaViite> viitteet = tutkinnonOsaViiteRepository.findByPerusteAndNimi(Optional.ofNullable(pquery.getPerusteId()).orElse(0l), pquery.getNimi(), pquery.isVanhentuneet(), Kieli.of(pquery.getKieli()), pageable);

        Page<TutkinnonOsaViiteKontekstiDto> resultDto = viitteet.map(tov -> {
            TutkinnonOsaViiteKontekstiDto tovkDto = mapper.map(tov, TutkinnonOsaViiteKontekstiDto.class);
            TutkinnonOsa tosa = tov.getTutkinnonOsa();
            Peruste peruste = tov.getSuoritustapa().getPerusteet().iterator().next();
            tovkDto.setPeruste(mapper.map(peruste, PerusteInfoDto.class));
            tovkDto.setTutkinnonOsaDto(mapper.map(tosa, TutkinnonOsaDto.class));
            tovkDto.setSuoritustapa(mapper.map(tov.getSuoritustapa(), SuoritustapaDto.class));

            PerusteprojektiKevytDto perusteprojekti = new PerusteprojektiKevytDto();
            perusteprojekti.setId(peruste.getPerusteprojekti().getId());
            tovkDto.setPerusteProjekti(perusteprojekti);
            return tovkDto;
        });

        return resultDto;
    }

    @Override
    @Transactional
    public List<TutkinnonOsaViiteKontekstiDto> findTutkinnonOsaViitteetByTutkinnonOsa(Long tutkinnonOsaId) {
        TutkinnonOsa tosa = getTutkinnonOsa(tutkinnonOsaId);
        List<TutkinnonOsaViite> viitteet = tutkinnonOsaViiteRepository.findAllByTutkinnonOsa(tosa);
        List<TutkinnonOsaViiteKontekstiDto> result = new ArrayList<>();

        for (TutkinnonOsaViite viite : viitteet) {
            Suoritustapa suoritustapa = viite.getSuoritustapa();
            Set<Peruste> perusteet = new HashSet<>(suoritustapa.getPerusteet());
            Set<Peruste> julkaistut = perusteet.stream()
                    .filter(peruste -> Objects.equals(
                            ProjektiTila.JULKAISTU,
                            perusteprojektiRepository.findOneByPeruste(peruste).getTila()))
                    .collect(Collectors.toSet());

            if (julkaistut.size() > 0) {
                TutkinnonOsaViiteKontekstiDto viiteDto = mapper.map(viite, TutkinnonOsaViiteKontekstiDto.class);
                viiteDto.setPeruste(mapper.map(julkaistut.iterator().next(), PerusteInfoDto.class));
                viiteDto.setSuoritustapa(mapper.map(suoritustapa, SuoritustapaDto.class));
                result.add(viiteDto);
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    private TutkinnonOsa getTutkinnonOsa(Long tutkinnonOsaId) {
        TutkinnonOsa tosa = tutkinnonOsaRepo.findOne(tutkinnonOsaId);
        if (tosa == null) {
            throw new BusinessRuleViolationException("tutkinnon-osaa-ei-ole");
        }
        return tosa;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutkinnonOsaKaikkiDto> getTutkinnonOsaKaikkiDtoByKoodi(String koodiUri) {
        return mapper.mapAsList(tutkinnonOsaRepo.findByKoodiUriAndValmiitPerusteet(koodiUri), TutkinnonOsaKaikkiDto.class);
    }

}
