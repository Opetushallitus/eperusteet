package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.repository.Lops2019Repository;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class Lops2019ServiceImpl implements fi.vm.sade.eperusteet.service.yl.Lops2019Service {

    @Autowired
    private PerusteenOsaViiteService viiteService;

    @Autowired
    private Lops2019Repository repository;

    @Override
    public PerusteenOsaViiteDto.Matala addSisalto(Long perusteId, Long viiteId, PerusteenOsaViiteDto.Matala dto) {
        Lops2019Sisalto sisalto = repository.findByPerusteId(perusteId);
        if (viiteId == null) {
            return viiteService.addSisalto(perusteId, sisalto.getSisalto().getId(), dto);
        } else {
            return viiteService.addSisalto(perusteId, viiteId, dto);
        }
    }

    @Override
    public void removeSisalto(Long perusteId, Long viiteId) {
        viiteService.removeSisalto(perusteId, viiteId);
    }
}
