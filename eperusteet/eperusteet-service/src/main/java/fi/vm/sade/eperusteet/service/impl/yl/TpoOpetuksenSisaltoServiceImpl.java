package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.dto.util.TaiteenalaViiteUpdateDto;
import fi.vm.sade.eperusteet.dto.yl.TaiteenalaDto;
import fi.vm.sade.eperusteet.service.yl.TpoOpetuksenSisaltoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Deprecated
public class  TpoOpetuksenSisaltoServiceImpl implements TpoOpetuksenSisaltoService {


    @Override
    public TaiteenalaDto getTaiteenala(Long perusteId, Long taiteenalaId) {
        return null;
    }

    @Override
    public List<TaiteenalaViiteUpdateDto> getTaiteenalat(Long perusteId) {
        return null;
    }

    @Override
    public TaiteenalaDto updateTaiteenala(Long perusteId, Long taiteenalaId, TaiteenalaDto oppiaineDto) {
        return null;
    }

    @Override
    public void removeTaiteenala(Long perusteId, Long taiteenalaId) {

    }

    @Override
    public TaiteenalaDto addTaiteenala(Long perusteId, TaiteenalaDto oppiaineDto) {
        return null;
    }
}
