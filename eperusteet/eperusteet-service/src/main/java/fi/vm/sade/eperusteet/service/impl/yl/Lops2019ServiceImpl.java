package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminen;
import fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuus;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019LaajaAlainenRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019OppiaineRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019SisaltoRepository;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.Lops2019Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        return mapper.map(oppiaine, Lops2019OppiaineDto.class);
    }

    @Override
    public Lops2019OppiaineDto updateOppiaine(Long perusteId, Lops2019OppiaineDto dto) {
        Lops2019Oppiaine oppiaine = mapper.map(dto, Lops2019Oppiaine.class);
        oppiaine = oppiaineRepository.save(oppiaine);
        // Todo: tarkista, että kuuluu perusteeseen
        return mapper.map(oppiaine, Lops2019OppiaineDto.class);
    }

    @Override
    public void removeOppiaine(Long perusteId, Long id) {
        Lops2019Sisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        Lops2019Oppiaine oppiaine = oppiaineRepository.findOne(id);
        boolean removed = sisalto.getOppiaineet().remove(oppiaine);

        // Poistetaan, jos viitattu perusteen sisällöstä
        if (removed) {
            oppiaineRepository.delete(id);
        }
    }

}
