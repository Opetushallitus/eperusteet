package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.yl.PerusopetuksenPerusteenSisaltoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PerusopetuksenPerusteenSisaltoServiceImpl
        extends AbstractOppiaineOpetuksenSisaltoService<PerusopetuksenPerusteenSisalto>
        implements PerusopetuksenPerusteenSisaltoService {

    @Autowired
    protected PerusopetuksenPerusteenSisaltoRepository sisaltoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LaajaalainenOsaaminenDto> getLaajaalaisetOsaamiset(Long perusteId) {
        return mapper.mapAsList(getByPerusteId(perusteId).getLaajaalaisetosaamiset(), LaajaalainenOsaaminenDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VuosiluokkaKokonaisuusDto> getVuosiluokkaKokonaisuudet(Long perusteId) {
        return mapper.mapAsList(getByPerusteId(perusteId).getVuosiluokkakokonaisuudet(), VuosiluokkaKokonaisuusDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    protected PerusopetuksenPerusteenSisalto getByPerusteId(Long perusteId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        if (sisalto == null) {
            throw new BusinessRuleViolationException("Perusteen sisältöä ei löydy");
        }

        return sisalto;
    }
}
