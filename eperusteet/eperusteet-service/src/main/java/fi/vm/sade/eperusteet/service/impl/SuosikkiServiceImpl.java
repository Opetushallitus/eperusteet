package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.dto.kayttaja.SuosikkiDto;
import fi.vm.sade.eperusteet.repository.SuosikkiRepository;
import fi.vm.sade.eperusteet.service.SuosikkiService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SuosikkiServiceImpl implements SuosikkiService {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    SuosikkiRepository suosikki;

    @Override
    public SuosikkiDto get(Long suosikkiId) {
        return mapper.map(suosikki.findOne(suosikkiId), SuosikkiDto.class);
    }

}
