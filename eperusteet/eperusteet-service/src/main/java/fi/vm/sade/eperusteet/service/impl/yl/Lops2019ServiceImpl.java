package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminen;
import fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuus;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019LaajaAlainenRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019ModuuliRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019OppiaineRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019SisaltoRepository;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.Lops2019Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

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
    public Lops2019OppiaineDto addOppiaine(Long perusteId, Lops2019OppiaineDto dto) {
        Lops2019Oppiaine oppiaine = mapper.map(dto, Lops2019Oppiaine.class);
        oppiaine = oppiaineRepository.save(oppiaine);

        // Lisätään sisältöön viittaus oppiaineeseen
        Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        sisalto.getOppiaineet().add(oppiaine);

        return mapper.map(oppiaine, Lops2019OppiaineDto.class);
    }

    @Override
    public Lops2019OppiaineDto getOppiaine(Long perusteId, Long oppiaineId) {
        Lops2019Oppiaine oppiaine = oppiaineRepository.findOne(oppiaineId);
        // Todo: tarkista, että kuuluu perusteeseen
        Lops2019OppiaineDto oppiaineDto = mapper.map(oppiaine, Lops2019OppiaineDto.class);

        List<Lops2019OppiaineDto> oppimaarat = mapper.mapAsList(oppiaine.getOppimaarat(), Lops2019OppiaineDto.class);
        oppiaineDto.setOppimaarat(oppimaarat);

        List<Lops2019ModuuliDto> moduulit = mapper.mapAsList(oppiaine.getModuulit(), Lops2019ModuuliDto.class);
        oppiaineDto.setModuulit(moduulit);

        return oppiaineDto;
    }

    @Override
    public Lops2019OppiaineDto updateOppiaine(Long perusteId, Lops2019OppiaineDto dto) {
        Lops2019Oppiaine oppiaine = mapper.map(dto, Lops2019Oppiaine.class);
        oppiaine = oppiaineRepository.save(oppiaine);
        // Todo: tarkista, että kuuluu perusteeseen
        Lops2019OppiaineDto oppiaineDto = mapper.map(oppiaine, Lops2019OppiaineDto.class);

        List<Lops2019OppiaineDto> oppimaarat = mapper.mapAsList(oppiaine.getOppimaarat(), Lops2019OppiaineDto.class);
        oppiaineDto.setOppimaarat(oppimaarat);

        List<Lops2019ModuuliDto> moduulit = mapper.mapAsList(oppiaine.getModuulit(), Lops2019ModuuliDto.class);
        oppiaineDto.setModuulit(moduulit);

        return oppiaineDto;
    }

    @Override
    public void removeOppiaine(Long perusteId, Long oppiaineId) {
        Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        Lops2019Oppiaine oppiaine = oppiaineRepository.findOne(oppiaineId);
        boolean removed = sisalto.getOppiaineet().remove(oppiaine);

        // Poistetaan, jos viitattu perusteen sisällöstä
        if (removed) {
            oppiaineRepository.delete(oppiaineId);
        }
    }

    @Override
    public Lops2019ModuuliDto getModuuli(Long perusteId, Long oppiaineId, Long moduuliId) {
        Lops2019Oppiaine oppiaine = oppiaineRepository.findOne(oppiaineId);
        // Todo: tarkista, että kuuluu perusteeseen
        Optional<Lops2019Moduuli> moduuliOptional = oppiaine.getModuulit().stream()
                .filter(moduuli -> moduuli.getId().equals(moduuliId))
                .findAny();

        return moduuliOptional.map(lops2019Moduuli -> mapper.map(lops2019Moduuli, Lops2019ModuuliDto.class))
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public Lops2019ModuuliDto updateModuuli(Long perusteId, Lops2019ModuuliDto dto) {
        Lops2019Moduuli moduuli = mapper.map(dto, Lops2019Moduuli.class);
        moduuli = moduuliRepository.save(moduuli);
        return mapper.map(moduuli, Lops2019ModuuliDto.class);
    }

}
