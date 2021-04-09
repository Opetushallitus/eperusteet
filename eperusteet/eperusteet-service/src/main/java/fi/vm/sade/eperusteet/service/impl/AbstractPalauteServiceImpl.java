package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Palaute;
import fi.vm.sade.eperusteet.dto.PalauteDto;
import fi.vm.sade.eperusteet.repository.PalauteRepository;
import fi.vm.sade.eperusteet.service.PalauteService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPalauteServiceImpl implements PalauteService {

    @Autowired
    protected PalauteRepository palauteRepository;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Override
    public List<PalauteDto> getPalauteStatus(String palautekanava) {
        return mapper.mapAsList(palauteRepository.findByKey(palautekanava), PalauteDto.class);
    }

    @Override
    public PalauteDto paivitaPalaute(PalauteDto palauteDto) {
        Palaute palaute = mapper.map(palauteDto, Palaute.class);
        palaute = palauteRepository.save(palaute);
        return mapper.map(palaute, PalauteDto.class);
    }
}
