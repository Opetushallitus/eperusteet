package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.GeneerinenArviointiasteikko;
import fi.vm.sade.eperusteet.dto.GeneerinenArviointiasteikkoDto;
import fi.vm.sade.eperusteet.repository.GeneerinenArviointiasteikkoRepository;
import fi.vm.sade.eperusteet.service.GeneerinenArviointiasteikkoService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class GeneerinenArviointiasteikkoServiceImpl implements GeneerinenArviointiasteikkoService {

    @Autowired
    private GeneerinenArviointiasteikkoRepository geneerinenArviointiasteikkoRepository;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Override
    public List<GeneerinenArviointiasteikkoDto> getAll() {
        List<GeneerinenArviointiasteikko> asteikot = geneerinenArviointiasteikkoRepository.findAll();
        return mapper.mapAsList(asteikot, GeneerinenArviointiasteikkoDto.class);
    }

    @Override
    public GeneerinenArviointiasteikkoDto getOne(Long id) {
        GeneerinenArviointiasteikko asteikko = geneerinenArviointiasteikkoRepository.findOne(id);
        if (asteikko == null) {
            throw new BusinessRuleViolationException("geneerinen-arivointiasteikko-ei-loytynyt");
        }
        return mapper.map(asteikko, GeneerinenArviointiasteikkoDto.class);
    }

    @Override
    public GeneerinenArviointiasteikkoDto add(GeneerinenArviointiasteikkoDto asteikkoDto) {
        GeneerinenArviointiasteikko asteikko = mapper.map(asteikkoDto, GeneerinenArviointiasteikko.class);
        return null;
    }

    @Override
    public GeneerinenArviointiasteikkoDto update(Long id) {
        return null;
    }

    @Override
    public void remove(Long id) {

    }
}
