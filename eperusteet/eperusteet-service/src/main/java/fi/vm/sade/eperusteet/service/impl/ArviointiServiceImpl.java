package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiDto;
import fi.vm.sade.eperusteet.repository.ArviointiRepository;
import fi.vm.sade.eperusteet.service.internal.ArviointiService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ArviointiServiceImpl implements ArviointiService{

    @Autowired
    private ArviointiRepository arviointiRepository;

    @Autowired
    @Dto
    private DtoMapper dtoMapper;

    @Override
    public List<ArviointiDto> findAll() {
        return dtoMapper.mapAsList(arviointiRepository.findAll(), ArviointiDto.class);
    }

    @Override
    public ArviointiDto findById(Long id) {
        return dtoMapper.map(arviointiRepository.findOne(id), ArviointiDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public ArviointiDto add(ArviointiDto arviointiDto) {
        return dtoMapper.map(arviointiRepository.save(dtoMapper.map(arviointiDto, Arviointi.class)), ArviointiDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public Arviointi kopioi(Arviointi arviointi) {
        if (arviointi == null) {
            return null;
        }
        Arviointi uusiArviointi = new Arviointi(arviointi);
        return arviointiRepository.save(uusiArviointi);
    }
}
