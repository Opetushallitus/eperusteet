package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.vst.VapaasivistystyoSisalto;
import fi.vm.sade.eperusteet.dto.vst.VapaasivistystyoSisaltoDto;
import fi.vm.sade.eperusteet.repository.VapaasivistystyoSisaltoRepository;
import fi.vm.sade.eperusteet.service.VapaasivistystyoSisaltoService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VapaasivistystyoSisaltoServiceImpl implements VapaasivistystyoSisaltoService {

    @Autowired
    private VapaasivistystyoSisaltoRepository vapaasivistystyoSisaltoRepository;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Override
    public VapaasivistystyoSisaltoDto update(Long perusteId, VapaasivistystyoSisaltoDto vapaasivistystyoSisaltoDto) {
        VapaasivistystyoSisalto vapaasivistystyoSisalto = vapaasivistystyoSisaltoRepository.getOne(vapaasivistystyoSisaltoDto.getId());
        vapaasivistystyoSisalto.setLaajuus(vapaasivistystyoSisaltoDto.getLaajuus());
        return mapper.map(vapaasivistystyoSisaltoRepository.save(vapaasivistystyoSisalto), VapaasivistystyoSisaltoDto.class);

    }
}
