package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.OpasSisalto;
import fi.vm.sade.eperusteet.domain.OppaanKiinnitettyKoodi;
import fi.vm.sade.eperusteet.dto.peruste.OpasSisaltoKevytDto;
import fi.vm.sade.eperusteet.repository.OpasSisaltoRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OpasSisaltoServiceImpl implements OpasSisaltoService {

    @Autowired
    private OpasSisaltoRepository opasSisaltoRepository;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Override
    public OpasSisaltoKevytDto update(Long perusteId, OpasSisaltoKevytDto opasSisaltoDto) {
        OpasSisalto opasSisalto = opasSisaltoRepository.findOne(opasSisaltoDto.getId());
        List<OppaanKiinnitettyKoodi> oppaanKiinnitetytKoodit = mapper.mapAsList(opasSisaltoDto.getOppaanKiinnitetytKoodit(), OppaanKiinnitettyKoodi.class);
        opasSisalto.setOppaanKiinnitetytKoodit(oppaanKiinnitetytKoodit.stream().map(oppaanKiinnitettyKoodi -> {
            oppaanKiinnitettyKoodi.setOpasSisalto(opasSisalto);
            return oppaanKiinnitettyKoodi;
        }).collect(Collectors.toList()));

        return mapper.map(opasSisaltoRepository.save(opasSisalto), OpasSisaltoKevytDto.class);
    }
}
