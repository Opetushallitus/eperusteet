package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminen;
import fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuus;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.dto.lops2019.Lops2019OppiaineKaikkiDto;
import fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliBaseDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019LaajaAlainenRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019ModuuliRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019OppiaineRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019SisaltoRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.Lops2019Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class Lops2019ServiceImpl implements Lops2019Service {

    @Autowired
    private EntityManager em;

    @Autowired
    private PerusteenOsaViiteService viiteService;

    @Autowired
    private Lops2019SisaltoRepository sisaltoRepository;

    @Autowired
    private Lops2019LaajaAlainenRepository laajaAlainenRepository;

    @Autowired
    private Lops2019OppiaineRepository oppiaineRepository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private Lops2019ModuuliRepository moduuliRepository;

    @Autowired
    @Dto
    protected DtoMapper mapper;

    @Override
    public PerusteenOsaViiteDto.Matala addSisalto(final Long perusteId, final Long viiteId, final PerusteenOsaViiteDto.Matala dto) {
        final Peruste peruste = perusteRepository.findOne(perusteId);
        if (Objects.equals(peruste.getTila(), PerusteTila.VALMIS)) {
            throw new BusinessRuleViolationException("vain-korjaukset-sallittu");
        }

        final Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        if (viiteId == null) {
            return viiteService.addSisalto(perusteId, sisalto.getSisalto().getId(), dto);
        } else {
            return viiteService.addSisalto(perusteId, viiteId, dto);
        }
    }

    @Override
    public void removeSisalto(final Long perusteId, final Long viiteId) {
        final Peruste peruste = perusteRepository.findOne(perusteId);
        if (Objects.equals(peruste.getTila(), PerusteTila.VALMIS)) {
            throw new BusinessRuleViolationException("vain-korjaukset-sallittu");
        }

        viiteService.removeSisalto(perusteId, viiteId);
    }

    @Override
    public Lops2019LaajaAlainenOsaaminenKokonaisuusDto getLaajaAlainenOsaaminenKokonaisuus(final Long perusteId) {
        final Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        final Lops2019LaajaAlainenOsaaminenKokonaisuus kokonaisuus = sisalto.getLaajaAlainenOsaaminen();

        return mapper.map(kokonaisuus, Lops2019LaajaAlainenOsaaminenKokonaisuusDto.class);
    }

    @Override
    public Lops2019LaajaAlainenOsaaminenKokonaisuusDto updateLaajaAlainenOsaaminenKokonaisuus(
            final Long perusteId,
            final Lops2019LaajaAlainenOsaaminenKokonaisuusDto dto
    ) {

        Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        final List<Lops2019LaajaAlainenOsaaminen> laajaAlaiset = mapper.mapAsList(dto.getLaajaAlaisetOsaamiset(),
                Lops2019LaajaAlainenOsaaminen.class);

        final Peruste peruste = perusteRepository.findOne(perusteId);

        final Lops2019LaajaAlainenOsaaminenKokonaisuus kokonaisuus = sisalto.getLaajaAlainenOsaaminen();

        if (Objects.equals(peruste.getTila(), PerusteTila.VALMIS)) {
            kokonaisuus.getLaajaAlaisetOsaamiset().forEach(lao -> {
                laajaAlaiset.forEach(olao -> {
                    if (Objects.equals(lao.getId(), olao.getId()) && !lao.structureEquals(olao)) {
                        throw new BusinessRuleViolationException("vain-korjaukset-sallittu");
                    }
                });
            });
        }

        kokonaisuus.setLaajaAlaisetOsaamiset(laajaAlaiset);

        sisalto = sisaltoRepository.save(sisalto);

        return mapper.map(sisalto.getLaajaAlainenOsaaminen(), Lops2019LaajaAlainenOsaaminenKokonaisuusDto.class);
    }

    @Override
    public Lops2019LaajaAlainenOsaaminenDto addLaajaAlainenOsaaminen(final Long perusteId) {
        Lops2019LaajaAlainenOsaaminen laajaAlainenOsaaminen = new Lops2019LaajaAlainenOsaaminen();

        final Peruste peruste = perusteRepository.findOne(perusteId);
        if (Objects.equals(peruste.getTila(), PerusteTila.VALMIS)) {
            throw new BusinessRuleViolationException("vain-korjaukset-sallittu");
        }

        laajaAlainenOsaaminen = laajaAlainenRepository.save(laajaAlainenOsaaminen);

        return mapper.map(laajaAlainenOsaaminen, Lops2019LaajaAlainenOsaaminenDto.class);
    }

    @Override
    public List<Lops2019OppiaineDto> getOppiaineet(final Long perusteId) {
        final Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        return mapper.mapAsList(sisalto.getOppiaineet(), Lops2019OppiaineDto.class);
    }

    @Override
    public List<Lops2019OppiaineDto> sortOppiaineet(final Long perusteId, final List<Lops2019OppiaineDto> dtos) {
        final List<Lops2019Oppiaine> oppiaineet = mapper.mapAsList(dtos, Lops2019Oppiaine.class);

        // Asetetaan järjestysnumero jokaiselle oppiaineelle
        for (int i = 0; i < oppiaineet.size(); i++) {
            Lops2019Oppiaine oppiaine = oppiaineet.get(i);
            oppiaine = oppiaineRepository.findOne(oppiaine.getId());
            oppiaine.setJarjestys(i);
            oppiaineRepository.save(oppiaine);
        }

        return mapper.mapAsList(oppiaineet, Lops2019OppiaineDto.class);
    }

    @Override
    public Lops2019OppiaineKaikkiDto getOppiaineRevisionData(final Long perusteId, final Long oppiaineId, final int rev) {
        final Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        final Lops2019Oppiaine oppiaine = oppiaineRepository.findOne(oppiaineId);
        if (!sisalto.getOppiaineet().contains(oppiaine)) {
            throw new BusinessRuleViolationException("vain-omaa-paatason-voi-muokata");
        }

        final Lops2019Oppiaine oa = oppiaineRepository.findRevision(oppiaineId, rev);
        if (oa == null) {
            throw new BusinessRuleViolationException("versiota-ei-loytynyt");
        }
        final Lops2019OppiaineKaikkiDto oaDto = mapper.map(oa, Lops2019OppiaineKaikkiDto.class);
        if (oa.getOppimaarat() != null) {
            final List<Lops2019OppiaineKaikkiDto> omt = oa.getOppimaarat().stream()
                    .map(om -> {
                        Lops2019OppiaineKaikkiDto omDto = mapper.map(om, Lops2019OppiaineKaikkiDto.class);
                        omDto.setModuulit(mapper.mapAsList(om.getModuulit(), Lops2019ModuuliDto.class));
                        return omDto;
                    })
                    .collect(Collectors.toList());
            oaDto.setOppimaarat(omt);
        }

        if (oa.getModuulit() != null) {
            final List<Lops2019ModuuliDto> moduulit = mapper.mapAsList(oa.getModuulit(), Lops2019ModuuliDto.class);
            oaDto.setModuulit(moduulit);
        }
        return oaDto;
    }

    @Override
    @Transactional
    public void restoreOppiaineRevisionInplace(final Long perusteId, final Long oppiaineId, final int rev) {
        Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        final Lops2019Oppiaine oppiaine = oppiaineRepository.findOne(oppiaineId);
        if (!sisalto.getOppiaineet().contains(oppiaine)) {
            throw new BusinessRuleViolationException("vain-omaa-paatason-voi-muokata");
        }

        Lops2019Oppiaine oa = oppiaineRepository.findRevision(oppiaineId, rev);
        if (oa != null) {
            oa = oa.copy(true);
            oa.setId(oppiaine.getId());
            sisalto.getOppiaineet().removeIf(lops2019Oppiaine -> Objects.equals(lops2019Oppiaine.getId(), oppiaine.getId()));
            sisalto = sisaltoRepository.save(sisalto);
//            oppiaineRepository.delete(oppiaine);
            final Lops2019Oppiaine saved = oppiaineRepository.save(oa);
            sisalto.getOppiaineet().add(saved);
            sisaltoRepository.save(sisalto);
        }
    }

    @Override
    public List<Revision> getOppiaineRevisions(final Long perusteId, final Long oppiaineId) {
        final Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        final Lops2019Oppiaine oppiaine = oppiaineRepository.findOne(oppiaineId);
        final List<Revision> revs = oppiaineRepository.getRevisions(oppiaineId);
        return revs;
    }

    @Override
    public void palautaSisaltoOppiaineet(final Long perusteId) {
        final Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        final Long sisaltoId = sisalto.getId();

        // Lisätään nykyiset versiot
        final List<Lops2019Oppiaine> oppiaineet = sisalto.getOppiaineet();
        final List<Lops2019Oppiaine> palautetut = new ArrayList<>();

        oppiaineet.forEach(oa -> {
            final Long oaId = oa.getId();
            final ArrayList<Lops2019Oppiaine> oppiaineVersiot = new ArrayList<>();
            oppiaineVersiot.add(oa);

            // Haetaan eri versiot mukaan
            final List<Revision> revisions = oppiaineRepository.getRevisions(oaId);
            revisions.forEach(rev -> {
                final Lops2019Oppiaine oppiaineRevision = oppiaineRepository.findRevision(oaId, rev.getNumero());
                oppiaineVersiot.add(oppiaineRevision);
            });

            // Yritetään palauttaa viimeisin versio, jossa on oppimäärät tai moduulit tallessa
            boolean found = false;
            for (final Lops2019Oppiaine rev : oppiaineVersiot) {
                if (!ObjectUtils.isEmpty(rev.getOppimaarat()) || !ObjectUtils.isEmpty(rev.getModuulit())) {
                    palautetut.add(rev.copy());
                    found = true;
                    break;
                }
            }
            // Jos ei löytynyt palautettavaa versioita, säilytetään nykyinen
            if (!found && oppiaineVersiot.size() > 0) {
                palautetut.add(oppiaineVersiot.get(0));
            }
        });

        // Asetetaan järjestys, jotta kopiot ovat oikeassa järjestyksessä
        for (int i = 0; i < palautetut.size(); i++) {
            palautetut.get(i).setJarjestys(i);
        }

        sisalto.getOppiaineet().clear();
        sisalto.getOppiaineet().addAll(palautetut);

        sisaltoRepository.save(sisalto);
    }

    @Override
    public Lops2019OppiaineDto addOppiaine(final Long perusteId, final Lops2019OppiaineDto dto) {
        if (dto.getOppiaine() != null && !ObjectUtils.isEmpty(dto.getOppimaarat())) {
            throw new BusinessRuleViolationException("oppimaaralla-ei-voi-olla-oppimaaria");
        }

        final Peruste peruste = perusteRepository.findOne(perusteId);
        if (Objects.equals(peruste.getTila(), PerusteTila.VALMIS)) {
            throw new BusinessRuleViolationException("vain-korjaukset-sallittu");
        }

        Lops2019Oppiaine oppiaine = mapper.map(dto, Lops2019Oppiaine.class);

        oppiaine = oppiaineRepository.save(oppiaine);

        // Lisätään sisältöön viittaus oppiaineeseen
        final Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        sisalto.getOppiaineet().add(oppiaine);

        return mapper.map(oppiaine, Lops2019OppiaineDto.class);
    }

    private Lops2019Oppiaine findOppiaine(final Long perusteId, final Long oppiaineId) {
        // FIXME: Mappaa taaksepäin perusteen sisältöön
        final Lops2019Oppiaine oa = oppiaineRepository.findOne(oppiaineId);
        final Peruste peruste = perusteRepository.findOne(perusteId);
        final boolean perusteHasOppiaine = peruste.getLops2019Sisalto().getOppiaineet().stream()
                .map(x -> Stream.concat(Stream.of(oa), oa.getOppimaarat().stream()))
                .flatMap(x -> x)
                .anyMatch(oa::equals);
        if (!perusteHasOppiaine) {
            throw new BusinessRuleViolationException("oppiainetta-ei-ole");
        }
        return oa;
    }

    @Override
    public void palautaOppiaineenModuulit(final Long perusteId, final Long id) {
        final Lops2019Oppiaine oppiaine = this.findOppiaine(perusteId, id);
        final Set<Long> nykyiset = oppiaine.getModuulit().stream()
                .map(AbstractAuditedReferenceableEntity::getId)
                .collect(Collectors.toSet());
        final Set<Long> kaikki = oppiaineRepository.getRevisions(id).stream()
                .map(rev -> oppiaineRepository.findRevision(id, rev.getNumero()))
                .map(Lops2019Oppiaine::getModuulit)
                .flatMap(Collection::stream)
                .map(AbstractAuditedReferenceableEntity::getId)
                .collect(Collectors.toSet());
        kaikki.removeAll(nykyiset);
        final List<Lops2019Moduuli> moduulit = oppiaine.getModuulit();
        kaikki.forEach(moduuliId -> {
            final List<Revision> revisions = moduuliRepository.getRevisions(moduuliId);
            revisions.sort(Comparator.comparingInt(Revision::getNumero).reversed());
            if (revisions.size() > 1) {
                final Lops2019Moduuli poistettu = moduuliRepository.findRevision(moduuliId, revisions.get(1).getNumero());
                Lops2019Moduuli kopio = poistettu.copy();
                kopio.setKoodi(null); // Koodit pitää asettaa uudestaan
                kopio = moduuliRepository.save(kopio);
                moduulit.add(kopio);
            }
        });
        for (int i = 0; i < moduulit.size(); i++) {
            moduulit.get(i).setJarjestys(i);
        }
        oppiaine.setModuulit(moduulit);
        oppiaineRepository.save(oppiaine);
    }

    @Override
    public Lops2019OppiaineKaikkiDto getOppiaineKaikki(final Long perusteId, final Long oppiaineId) {
        final Lops2019Oppiaine oppiaine = this.findOppiaine(perusteId, oppiaineId);
        final Lops2019OppiaineKaikkiDto oppiaineDto = mapper.map(oppiaine, Lops2019OppiaineKaikkiDto.class);

        // FIXME: Miksi?
        // Haetaan manuaalisesti oppimäärät ja moduulit
        oppiaineDto.setOppimaarat(mapper.mapAsList(oppiaine.getOppimaarat(), Lops2019OppiaineKaikkiDto.class));
        oppiaineDto.setModuulit(mapper.mapAsList(oppiaine.getModuulit(), Lops2019ModuuliDto.class));

        return oppiaineDto;
    }

    @Override
    public Lops2019OppiaineDto getOppiaine(final Long perusteId, final Long oppiaineId) {
        final Lops2019Oppiaine oppiaine = this.findOppiaine(perusteId, oppiaineId);
        final Lops2019OppiaineDto oppiaineDto = mapper.map(oppiaine, Lops2019OppiaineDto.class);

        // Haetaan manuaalisesti oppimäärät ja moduulit
        oppiaineDto.setOppimaarat(mapper.mapAsList(oppiaine.getOppimaarat(), Lops2019OppiaineDto.class));
        oppiaineDto.setModuulit(mapper.mapAsList(oppiaine.getModuulit(), Lops2019ModuuliBaseDto.class));

        return oppiaineDto;
    }

    @Override
    public Lops2019OppiaineDto updateOppiaine(final Long perusteId, final Lops2019OppiaineDto dto) {
        if (dto.getOppiaine() != null && !ObjectUtils.isEmpty(dto.getOppimaarat())) {
            throw new BusinessRuleViolationException("oppimaaralla-ei-voi-olla-oppimaaria");
        }

        final Peruste peruste = perusteRepository.findOne(perusteId);
        Lops2019Oppiaine oppiaine = this.findOppiaine(perusteId, dto.getId());

        final Lops2019Oppiaine updatedOppiaine = mapper.map(dto, Lops2019Oppiaine.class);

        final Map<Long, Lops2019Oppiaine> oppiaineetMap = oppiaine.getOppimaarat().stream()
                .collect(Collectors.toMap(AbstractAuditedReferenceableEntity::getId, o -> o));

        final Map<Long, Lops2019Moduuli> moduulitMap = oppiaine.getModuulit().stream()
                .collect(Collectors.toMap(AbstractAuditedReferenceableEntity::getId, o -> o));


        final List<Lops2019Oppiaine> collectedOppimaarat = dto.getOppimaarat().stream()
                .map(om -> om.getId() != null
                        ? oppiaineetMap.get(om.getId())
                        : mapper.map(om, Lops2019Oppiaine.class))
                .collect(Collectors.toList());

        final List<Lops2019Moduuli> collectedModuulit = dto.getModuulit().stream()
                .map(m -> m.getId() != null
                        ? moduulitMap.get(m.getId())
                        : mapper.map(m, Lops2019Moduuli.class))
                .collect(Collectors.toList());

        updatedOppiaine.setOppimaarat(collectedOppimaarat);
        updatedOppiaine.setModuulit(collectedModuulit);

        if (Objects.equals(peruste.getTila(), PerusteTila.VALMIS) && !oppiaine.structureEquals(updatedOppiaine)) {
            throw new BusinessRuleViolationException("vain-korjaukset-sallittu");
        }

        oppiaine = updatedOppiaine;

        // Asetetaan oppimäärien järjetys
        final List<Lops2019Oppiaine> oppimaarat = oppiaine.getOppimaarat();
        if (!ObjectUtils.isEmpty(oppimaarat)) {
            for (int i = 0; i < oppimaarat.size(); i++) {
                final Lops2019Oppiaine oppimaara = oppimaarat.get(i);
                oppimaara.setJarjestys(i);
            }
        }

        // Asetetaan moduulien järjetys
        final List<Lops2019Moduuli> moduulit = oppiaine.getModuulit();
        if (!ObjectUtils.isEmpty(moduulit)) {
            for (int i = 0; i < moduulit.size(); i++) {
                final Lops2019Moduuli moduuli = moduulit.get(i);
                moduuli.setJarjestys(i);
            }
        }

        // Tallennetaan muokattu oppiaine
        oppiaine = oppiaineRepository.save(oppiaine);

        final Lops2019OppiaineDto oppiaineDto = mapper.map(oppiaine, Lops2019OppiaineDto.class);

        // Haetaan manuaalisesti oppimäärät ja moduulit mukaan
        oppiaineDto.setOppimaarat(mapper.mapAsList(oppiaine.getOppimaarat(), Lops2019OppiaineDto.class));
        oppiaineDto.setModuulit(mapper.mapAsList(oppiaine.getModuulit(), Lops2019ModuuliBaseDto.class));

        return oppiaineDto;
    }

    @Override
    public void removeOppiaine(final Long perusteId, final Long oppiaineId) {
        final Peruste peruste = perusteRepository.findOne(perusteId);
        if (Objects.equals(peruste.getTila(), PerusteTila.VALMIS)) {
            throw new BusinessRuleViolationException("vain-korjaukset-sallittu");
        }

        final Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        final Lops2019Oppiaine oppiaine = oppiaineRepository.findOne(oppiaineId);
        final Lops2019Oppiaine parent = oppiaine.getOppiaine();

        final boolean removed;

        if (parent != null) {
            removed = parent.getOppimaarat().remove(oppiaine);

        } else {
            removed = sisalto.getOppiaineet().remove(oppiaine);
        }

        // Poistetaan, jos viitattu perusteen sisällöstä
        if (removed) {
            oppiaineRepository.delete(oppiaineId);
        }
    }

    @Override
    public Lops2019ModuuliDto getModuuli(final Long perusteId, final Long oppiaineId, final Long moduuliId) {
        final Lops2019Oppiaine oppiaine = this.findOppiaine(perusteId, oppiaineId);
        final Optional<Lops2019Moduuli> moduuliOptional = oppiaine.getModuulit().stream()
                .filter(moduuli -> Objects.equals(moduuli.getId(), moduuliId))
                .findAny();

        return moduuliOptional.map(lops2019Moduuli -> mapper.map(lops2019Moduuli, Lops2019ModuuliDto.class))
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public Lops2019ModuuliDto updateModuuli(final Long perusteId, final Lops2019ModuuliDto dto) {
        Lops2019Moduuli moduuli = moduuliRepository.findOne(dto.getId());

        // Poistetaan tyhjät tavoitteet
        if (dto.getTavoitteet() != null) {
            List<LokalisoituTekstiDto> tavoitteet = dto.getTavoitteet().getTavoitteet();
            if (!ObjectUtils.isEmpty(tavoitteet)) {
                List<LokalisoituTekstiDto> filtered = tavoitteet.stream()
                        .filter(t -> t.getId() != null || (t.getTekstit() != null && t.getTekstit().size() > 0))
                        .collect(Collectors.toList());
                tavoitteet.clear();
                tavoitteet.addAll(filtered);
            }
        }

        // Poistetaan tyhjät sisällöt
        if (!ObjectUtils.isEmpty(dto.getSisallot())) {
            dto.getSisallot().forEach(s -> {
                List<LokalisoituTekstiDto> sisallot = s.getSisallot();
                if (!ObjectUtils.isEmpty(sisallot)) {
                    List<LokalisoituTekstiDto> filtered = sisallot.stream()
                            .filter(t -> t.getId() != null || (t.getTekstit() != null && t.getTekstit().size() > 0))
                            .collect(Collectors.toList());
                    sisallot.clear();
                    sisallot.addAll(filtered);
                }
            });
        }

        mapper.map(dto, moduuli);

        moduuli = moduuliRepository.save(moduuli);
        return mapper.map(moduuli, Lops2019ModuuliDto.class);
    }

    @Override
    public void removeModuuli(final Long perusteId, final Long oppiaineId, final Long moduuliId) {

        final Peruste peruste = perusteRepository.findOne(perusteId);
        if (Objects.equals(peruste.getTila(), PerusteTila.VALMIS)) {
            throw new BusinessRuleViolationException("vain-korjaukset-sallittu");
        }

        final Lops2019Oppiaine oppiaine = oppiaineRepository.findOne(oppiaineId);
        final Lops2019Moduuli moduuli = moduuliRepository.findOne(moduuliId);
        final boolean removed = oppiaine.getModuulit().remove(moduuli);

        // Poistetaan, jos viitattu perusteen sisällöstä
        if (removed) {
            moduuliRepository.delete(moduuliId);
        }
    }

}
