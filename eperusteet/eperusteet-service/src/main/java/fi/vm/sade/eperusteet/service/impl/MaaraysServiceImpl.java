package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Maarays;
import fi.vm.sade.eperusteet.dto.MaaraysDto;
import fi.vm.sade.eperusteet.repository.MaaraysRepository;
import fi.vm.sade.eperusteet.service.MaaraysService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MaaraysServiceImpl implements MaaraysService {

    @Autowired
    private MaaraysRepository maaraysRepository;

    @Dto
    @Autowired
    private DtoMapper dtoMapper;

    @Override
    public List<MaaraysDto> getMaaraykset() {
        return dtoMapper.mapAsList(maaraysRepository.findAll(), MaaraysDto.class);
    }

    @Override
    public MaaraysDto addMaarays(MaaraysDto maaraysDto) {
        return dtoMapper.map(
                maaraysRepository.save(dtoMapper.map(maaraysDto, Maarays.class)),
                MaaraysDto.class);
    }

    @Override
    public MaaraysDto updateMaarays(MaaraysDto maaraysDto) {
        if (maaraysRepository.findOne(maaraysDto.getId()) == null) {
            throw new BusinessRuleViolationException("maaraysta-ei-loydy");
        }

        return dtoMapper.map(
                maaraysRepository.save(dtoMapper.map(maaraysDto, Maarays.class)),
                MaaraysDto.class);
    }

    @Override
    public void deleteMaarays(long id) {
        if (maaraysRepository.findOne(id) == null) {
            throw new BusinessRuleViolationException("maaraysta-ei-loydy");
        }

        maaraysRepository.delete(id);
    }
}
