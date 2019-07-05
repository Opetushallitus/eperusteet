package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.JulkaistuPeruste;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuDto;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JulkaisutServiceImpl implements JulkaisutService {

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private JulkaisutRepository julkaisutRepository;

    @Override
    public JulkaisuDto getJulkaisu(long id) {
        Peruste peruste = perusteRepository.findOne(id);
        if (peruste == null) {
            throw new BusinessRuleViolationException("perustetta-ei-loytynyt");
        }

        JulkaistuPeruste one = julkaisutRepository.findOneByPeruste(peruste);
        JulkaisuDto julkaisu = mapper.map(one, JulkaisuDto.class);
        return julkaisu;
    }
}
