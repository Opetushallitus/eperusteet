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

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class Lops2019ServiceImpl implements Lops2019Service {

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
    public PerusteenOsaViiteDto.Matala addSisalto(Long perusteId, Long viiteId, PerusteenOsaViiteDto.Matala dto) {
        Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        if (viiteId == null) {
            return viiteService.addSisalto(perusteId, sisalto.getSisalto().getId(), dto);
        } else {
            return viiteService.addSisalto(perusteId, viiteId, dto);
        }
    }

    @Override
    public void removeSisalto(Long perusteId, Long viiteId) {
        viiteService.removeSisalto(perusteId, viiteId);
    }

    @Override
    public Lops2019LaajaAlainenOsaaminenKokonaisuusDto getLaajaAlainenOsaaminenKokonaisuus(Long perusteId) {
        Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        Lops2019LaajaAlainenOsaaminenKokonaisuus kokonaisuus = sisalto.getLaajaAlainenOsaaminen();

        return mapper.map(kokonaisuus, Lops2019LaajaAlainenOsaaminenKokonaisuusDto.class);
    }

    @Override
    public Lops2019LaajaAlainenOsaaminenKokonaisuusDto updateLaajaAlainenOsaaminenKokonaisuus(
            Long perusteId,
            Lops2019LaajaAlainenOsaaminenKokonaisuusDto dto
    ) {
        Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        List<Lops2019LaajaAlainenOsaaminen> laajaAlaiset = mapper.mapAsList(dto.getLaajaAlaisetOsaamiset(),
                Lops2019LaajaAlainenOsaaminen.class);
        sisalto.getLaajaAlainenOsaaminen().setLaajaAlaisetOsaamiset(laajaAlaiset);
        sisalto = sisaltoRepository.save(sisalto);

        return mapper.map(sisalto.getLaajaAlainenOsaaminen(), Lops2019LaajaAlainenOsaaminenKokonaisuusDto.class);
    }

    @Override
    public Lops2019LaajaAlainenOsaaminenDto addLaajaAlainenOsaaminen(Long perusteId) {
        Lops2019LaajaAlainenOsaaminen laajaAlainenOsaaminen = new Lops2019LaajaAlainenOsaaminen();
        laajaAlainenOsaaminen = laajaAlainenRepository.save(laajaAlainenOsaaminen);

        return mapper.map(laajaAlainenOsaaminen, Lops2019LaajaAlainenOsaaminenDto.class);
    }

    @Override
    public List<Lops2019OppiaineDto> getOppiaineet(Long perusteId) {
        Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);

        return mapper.mapAsList(sisalto.getOppiaineet(), Lops2019OppiaineDto.class);
    }

    @Override
    public List<Lops2019OppiaineDto> sortOppiaineet(Long perusteId, List<Lops2019OppiaineDto> dtos) {
        List<Lops2019Oppiaine> oppiaineet = mapper.mapAsList(dtos, Lops2019Oppiaine.class);

        // Asetetaan järjestysnumero jokaiselle oppiaineelle
        for (int i = 0; i < oppiaineet.size(); i++) {
            Lops2019Oppiaine oppiaine = oppiaineet.get(i);
            oppiaine.setJarjestys(i);
            oppiaineRepository.save(oppiaine);
        }

        return mapper.mapAsList(oppiaineet, Lops2019OppiaineDto.class);
    }

    @Override
    public Lops2019OppiaineDto addOppiaine(Long perusteId, Lops2019OppiaineDto dto) {
        if (dto.getOppiaine() != null && !ObjectUtils.isEmpty(dto.getOppimaarat())) {
            throw new BusinessRuleViolationException("oppimaaralla-ei-voi-olla-oppimaaria");
        }

        Lops2019Oppiaine oppiaine = mapper.map(dto, Lops2019Oppiaine.class);

        oppiaine = oppiaineRepository.save(oppiaine);

        // Lisätään sisältöön viittaus oppiaineeseen
        Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        sisalto.getOppiaineet().add(oppiaine);

        return mapper.map(oppiaine, Lops2019OppiaineDto.class);
    }

    private Lops2019Oppiaine findOppiaine(Long perusteId, Long oppiaineId) {
        // FIXME: Mappaa taaksepäin perusteen sisältöön
        Lops2019Oppiaine oa = oppiaineRepository.findOne(oppiaineId);
        Peruste peruste = perusteRepository.findOne(perusteId);
        boolean perusteHasOppiaine = peruste.getLops2019Sisalto().getOppiaineet().stream()
                .map(x -> Stream.concat(Stream.of(oa), oa.getOppimaarat().stream()))
                .flatMap(x -> x)
                .anyMatch(oa::equals);
        if (!perusteHasOppiaine) {
            throw new BusinessRuleViolationException("oppiainetta-ei-ole");
        }
        return oa;
    }

    @Override
    public void palautaOppiaineenModuulit(Long perusteId, Long id) {
        Lops2019Oppiaine oppiaine = findOppiaine(perusteId, id);
        Set<Long> nykyiset = oppiaine.getModuulit().stream()
                .map(AbstractAuditedReferenceableEntity::getId)
                .collect(Collectors.toSet());
        Set<Long> kaikki = oppiaineRepository.getRevisions(id).stream()
                .map(rev -> oppiaineRepository.findRevision(id, rev.getNumero()))
                .map(Lops2019Oppiaine::getModuulit)
                .flatMap(Collection::stream)
                .map(AbstractAuditedReferenceableEntity::getId)
                .collect(Collectors.toSet());
        kaikki.removeAll(nykyiset);
        List<Lops2019Moduuli> moduulit = oppiaine.getModuulit();
        kaikki.forEach(moduuliId -> {
            List<Revision> revisions = moduuliRepository.getRevisions(moduuliId);
            revisions.sort(Comparator.comparingInt(Revision::getNumero).reversed());
            if (revisions.size() > 1) {
                Lops2019Moduuli poistettu = moduuliRepository.findRevision(moduuliId, revisions.get(1).getNumero());
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
    public Lops2019OppiaineKaikkiDto getOppiaineKaikki(Long perusteId, Long oppiaineId) {
        Lops2019Oppiaine oppiaine = findOppiaine(perusteId, oppiaineId);
        Lops2019OppiaineKaikkiDto oppiaineDto = mapper.map(oppiaine, Lops2019OppiaineKaikkiDto.class);

        // FIXME: Miksi?
        // Haetaan manuaalisesti oppimäärät ja moduulit
        oppiaineDto.setOppimaarat(mapper.mapAsList(oppiaine.getOppimaarat(), Lops2019OppiaineKaikkiDto.class));
        oppiaineDto.setModuulit(mapper.mapAsList(oppiaine.getModuulit(), Lops2019ModuuliDto.class));

        return oppiaineDto;
    }

    @Override
    public Lops2019OppiaineDto getOppiaine(Long perusteId, Long oppiaineId) {
        Lops2019Oppiaine oppiaine = findOppiaine(perusteId, oppiaineId);
        Lops2019OppiaineDto oppiaineDto = mapper.map(oppiaine, Lops2019OppiaineDto.class);

        // Haetaan manuaalisesti oppimäärät ja moduulit
        oppiaineDto.setOppimaarat(mapper.mapAsList(oppiaine.getOppimaarat(), Lops2019OppiaineDto.class));
        oppiaineDto.setModuulit(mapper.mapAsList(oppiaine.getModuulit(), Lops2019ModuuliBaseDto.class));

        return oppiaineDto;
    }

    @Override
    public Lops2019OppiaineDto updateOppiaine(Long perusteId, Lops2019OppiaineDto dto) {
        if (dto.getOppiaine() != null && !ObjectUtils.isEmpty(dto.getOppimaarat())) {
            throw new BusinessRuleViolationException("oppimaaralla-ei-voi-olla-oppimaaria");
        }

        Peruste peruste = perusteRepository.findOne(perusteId);
        Lops2019Oppiaine oppiaine = findOppiaine(perusteId, dto.getId());

        Lops2019Oppiaine updatedOppiaine = mapper.map(dto, Lops2019Oppiaine.class);
        if (peruste.getTila() == PerusteTila.VALMIS && !oppiaine.structureEquals(updatedOppiaine)) {
            throw new BusinessRuleViolationException("Vain korjaukset sallittu");
        }

        Map<Long, Lops2019Oppiaine> oppiaineetMap = oppiaine.getOppimaarat().stream()
                .collect(Collectors.toMap(AbstractAuditedReferenceableEntity::getId, o -> o));

        if (dto.getOppimaarat() != null) {
            List<Lops2019Oppiaine> collected = dto.getOppimaarat().stream()
                    .map(om -> om.getId() != null
                            ? oppiaineetMap.get(om.getId())
                            : mapper.map(om, Lops2019Oppiaine.class))
                    .collect(Collectors.toList());
            dto.setOppimaarat(null);
            oppiaine = updatedOppiaine;
            oppiaine.setOppimaarat(collected);

            // Asetetaan oppimäärien järjetys
            List<Lops2019Oppiaine> oppimaarat = oppiaine.getOppimaarat();
            if (!ObjectUtils.isEmpty(oppimaarat)) {
                for (int i = 0; i < oppimaarat.size(); i++) {
                    Lops2019Oppiaine oppimaara = oppimaarat.get(i);
                    oppimaara.setJarjestys(i);
                }
            }

            // Asetetaan moduulien järjetys
            List<Lops2019Moduuli> moduulit = oppiaine.getModuulit();
            if (!ObjectUtils.isEmpty(moduulit)) {
                for (int i = 0; i < moduulit.size(); i++) {
                    Lops2019Moduuli moduuli = moduulit.get(i);
                    moduuli.setJarjestys(i);
                }
            }
        }

        // Tallennetaan muokattu oppiaine
        oppiaine = oppiaineRepository.save(oppiaine);

        Lops2019OppiaineDto oppiaineDto = mapper.map(oppiaine, Lops2019OppiaineDto.class);

        // Haetaan manuaalisesti oppimäärät ja moduulit mukaan
        oppiaineDto.setOppimaarat(mapper.mapAsList(oppiaine.getOppimaarat(), Lops2019OppiaineDto.class));
        oppiaineDto.setModuulit(mapper.mapAsList(oppiaine.getModuulit(), Lops2019ModuuliBaseDto.class));

        return oppiaineDto;
    }

    @Override
    public void removeOppiaine(Long perusteId, Long oppiaineId) {
        Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        Lops2019Oppiaine oppiaine = oppiaineRepository.findOne(oppiaineId);
        Lops2019Oppiaine parent = oppiaine.getOppiaine();

        boolean removed;

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
    public Lops2019ModuuliDto getModuuli(Long perusteId, Long oppiaineId, Long moduuliId) {
        Lops2019Oppiaine oppiaine = findOppiaine(perusteId, oppiaineId);
        Optional<Lops2019Moduuli> moduuliOptional = oppiaine.getModuulit().stream()
                .filter(moduuli -> Objects.equals(moduuli.getId(), moduuliId))
                .findAny();

        return moduuliOptional.map(lops2019Moduuli -> mapper.map(lops2019Moduuli, Lops2019ModuuliDto.class))
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public Lops2019ModuuliDto updateModuuli(Long perusteId, Lops2019ModuuliDto dto) {
        Lops2019Moduuli moduuli = moduuliRepository.findOne(dto.getId());

        mapper.map(dto, moduuli);

        moduuli = moduuliRepository.save(moduuli);
        return mapper.map(moduuli, Lops2019ModuuliDto.class);
    }

    @Override
    public void removeModuuli(Long perusteId, Long oppiaineId, Long moduuliId) {
        Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        Lops2019Oppiaine oppiaine = oppiaineRepository.findOne(oppiaineId);
        Lops2019Moduuli moduuli = moduuliRepository.findOne(moduuliId);
        boolean removed = oppiaine.getModuulit().remove(moduuli);

        // Poistetaan, jos viitattu perusteen sisällöstä
        if (removed) {
            moduuliRepository.delete(moduuliId);
        }
    }

}
