package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteAikataulu;
import fi.vm.sade.eperusteet.dto.peruste.PerusteAikatauluDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.PerusteAikatauluService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerusteAikatauluServiceImpl implements PerusteAikatauluService {

    @Autowired
    PerusteRepository perusteRepository;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Override
    @Transactional
    public List<PerusteAikatauluDto> save(Long perusteId, List<PerusteAikatauluDto> perusteAikataulut) {

        Peruste peruste = perusteRepository.getOne(perusteId);
        List<PerusteAikataulu> aikataulut = mapper.mapAsList(perusteAikataulut, PerusteAikataulu.class).stream().map(aikataulu -> {
            aikataulu.setPeruste(peruste);
            return aikataulu;
        }).collect(Collectors.toList());
        peruste.setPerusteenAikataulut(aikataulut);

        perusteRepository.save(peruste);

        return mapper.mapAsList(peruste.getPerusteenAikataulut(), PerusteAikatauluDto.class);
    }
}
