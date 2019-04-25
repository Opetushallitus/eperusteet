package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineBaseDto;
import fi.vm.sade.eperusteet.repository.Lops2019Repository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class Lops2019ServiceImpl implements fi.vm.sade.eperusteet.service.yl.Lops2019Service {

    @Autowired
    private PerusteenOsaViiteService viiteService;

    @Autowired
    private Lops2019Repository repository;

    @Autowired
    @Dto
    protected DtoMapper mapper;

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

    @Override
    public Lops2019LaajaAlainenOsaaminenKokonaisuusDto getLaajaAlainenOsaaminenKokonaisuus(Long perusteId) {
        Lops2019Sisalto sisalto = repository.findByPerusteId(perusteId);
        return mapper.map(sisalto.getLaajaAlainenOsaaminen(), Lops2019LaajaAlainenOsaaminenKokonaisuusDto.class);
    }

    @Override
    public List<Lops2019OppiaineDto> getOppiaineet(Long perusteId) {
        Lops2019Sisalto sisalto = repository.findByPerusteId(perusteId);
        return mapper.mapAsList(sisalto.getOppiaineet(), Lops2019OppiaineDto.class);
    }

}
