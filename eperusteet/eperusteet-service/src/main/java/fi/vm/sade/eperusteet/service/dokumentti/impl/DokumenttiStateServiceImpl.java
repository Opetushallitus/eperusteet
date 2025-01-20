package fi.vm.sade.eperusteet.service.dokumentti.impl;

import fi.vm.sade.eperusteet.domain.Dokumentti;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.repository.DokumenttiRepository;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiStateService;

import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DokumenttiStateServiceImpl implements DokumenttiStateService {

    @Autowired
    private DokumenttiRepository dokumenttiRepository;

    @Dto
    @Autowired
    private DtoMapper mapper;

    
    @Transactional
    public DokumenttiDto save(DokumenttiDto dto) {
        Dokumentti dokumentti = mapper.map(dto, Dokumentti.class);
        dokumentti = dokumenttiRepository.save(dokumentti);
        return mapper.map(dokumentti, DokumenttiDto.class);
    }
}
