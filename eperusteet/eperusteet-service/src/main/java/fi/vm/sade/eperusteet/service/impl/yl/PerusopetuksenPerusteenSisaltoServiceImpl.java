/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.yl.LaajaalainenOsaaminen;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.LaajaalainenOsaaminenRepository;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.PerusopetuksenPerusteenSisaltoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PerusopetuksenPerusteenSisaltoServiceImpl implements PerusopetuksenPerusteenSisaltoService {

    @Autowired
    private PerusopetuksenPerusteenSisaltoRepository sisaltoRepository;
    @Autowired
    private LaajaalainenOsaaminenRepository osaaminenRepository;
    @Autowired
    @Dto
    private DtoMapper mapper;

    @Override
    public List<LaajaalainenOsaaminenDto> getLaajaalaisetOsaamiset(Long perusteId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        assertExists(sisalto, "Pyydettyä perustetta ei ole olemassa");
        return mapper.mapAsList(sisalto.getLaajaalaisetOsaamiset(), LaajaalainenOsaaminenDto.class);
    }

    @Override
    public LaajaalainenOsaaminenDto addLaajaalainenOsaaminen(Long perusteId, LaajaalainenOsaaminenDto dto) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        assertExists(sisalto, "Päivitettävää tietoa ei ole olemassa");
        LaajaalainenOsaaminen tmp = mapper.map(dto, LaajaalainenOsaaminen.class);
        tmp = osaaminenRepository.save(tmp);
        sisalto.addLaajaalainenOsaaminen(tmp);
        return mapper.map(tmp, LaajaalainenOsaaminenDto.class);
    }

    @Override
    public LaajaalainenOsaaminenDto updateLaajaalainenOsaaminen(Long perusteId, LaajaalainenOsaaminenDto dto) {

        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        assertExists(sisalto, "Päivitettävää tietoa ei ole olemassa");
        LaajaalainenOsaaminen current = sisalto.getLaajaalainenOsaaminen(dto.getId());
        assertExists(current, "Päivitettävää tietoa ei ole olemassa");
        mapper.map(dto, current);
        sisaltoRepository.save(sisalto);
        return mapper.map(current, LaajaalainenOsaaminenDto.class);
    }

    @Override
    public LaajaalainenOsaaminenDto getLaajaalainenOsaaminen(Long perusteId, Long id) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        assertExists(sisalto, "Perustetta ei ole olemassa");
        return mapper.map(sisalto.getLaajaalainenOsaaminen(id), LaajaalainenOsaaminenDto.class);
    }

    @Override
    public void deleteLaajaalainenOsaaminen(Long perusteId, Long id) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        assertExists(sisalto, "Perustetta ei ole olemassa");
        LaajaalainenOsaaminen lo = sisalto.getLaajaalainenOsaaminen(id);
        assertExists(lo, "Laaja-alaista osaamista ei ole olemassa");

        //TODO. tarkista että ei ole käytössä ja/tai poista viitteet?
        osaaminenRepository.delete(lo);
    }

    @Override
    public List<OppiaineSuppeaDto> getOppiaineet(Long perusteId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        return mapper.mapAsList(sisalto.getOppiaineet(), OppiaineSuppeaDto.class);
    }

    @Override
    public List<VuosiluokkaKokonaisuusDto> getVuosiluokkaKokonaisuudet(Long perusteId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        return mapper.mapAsList(sisalto.getVuosiluokkakokonaisuudet(), VuosiluokkaKokonaisuusDto.class);
    }

    private static void assertExists(Object o, String msg) {
        if (o == null) {
            throw new BusinessRuleViolationException(msg);
        }
    }

}
