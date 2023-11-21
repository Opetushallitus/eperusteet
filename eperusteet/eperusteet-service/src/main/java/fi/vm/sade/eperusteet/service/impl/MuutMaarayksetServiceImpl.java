package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.MuuMaarays;
import fi.vm.sade.eperusteet.dto.MuuMaaraysDto;
import fi.vm.sade.eperusteet.repository.MuuMaaraysRepository;
import fi.vm.sade.eperusteet.service.MuutMaarayksetService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MuutMaarayksetServiceImpl implements MuutMaarayksetService {

    @Autowired
    private MuuMaaraysRepository muuMaaraysRepository;

    @Dto
    @Autowired
    private DtoMapper dtoMapper;

    @Override
    public List<MuuMaaraysDto> getMaaraykset() {
        return dtoMapper.mapAsList(muuMaaraysRepository.findAll(), MuuMaaraysDto.class);
    }

    @Override
    public MuuMaaraysDto addMaarays(MuuMaaraysDto muuMaaraysDto) {
        return dtoMapper.map(
                muuMaaraysRepository.save(dtoMapper.map(muuMaaraysDto, MuuMaarays.class)),
                MuuMaaraysDto.class);
    }

    @Override
    public MuuMaaraysDto updateMaarays(MuuMaaraysDto muuMaaraysDto) {
        if (muuMaaraysRepository.findOne(muuMaaraysDto.getId()) == null) {
            throw new BusinessRuleViolationException("maaraysta-ei-loydy");
        }

        return dtoMapper.map(
                muuMaaraysRepository.save(dtoMapper.map(muuMaaraysDto, MuuMaarays.class)),
                MuuMaaraysDto.class);
    }

    @Override
    public void deleteMaarays(long id) {
        if (muuMaaraysRepository.findOne(id) == null) {
            throw new BusinessRuleViolationException("maaraysta-ei-loydy");
        }

        muuMaaraysRepository.delete(id);
    }
}
