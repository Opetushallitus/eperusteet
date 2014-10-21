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
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.PerusopetuksenPerusteenSisaltoService;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PerusopetuksenPerusteenSisaltoServiceImpl implements PerusopetuksenPerusteenSisaltoService {

    @Autowired
    private PerusopetuksenPerusteenSisaltoRepository sisaltoRepository;
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
    public List<LaajaalainenOsaaminenDto> updateLaajaalaisetOsaamiset(Long perusteId, List<LaajaalainenOsaaminenDto> dtos) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        assertExists(sisalto, "Päivitettävää tietoa ei ole olemassa");

        Set<LaajaalainenOsaaminen> laajaalaisetOsaamiset = sisalto.getLaajaalaisetOsaamiset();
        mapper.mapToCollection(dtos, laajaalaisetOsaamiset, LaajaalainenOsaaminen.class);
        sisalto.setLaajaalaisetOsaamiset(laajaalaisetOsaamiset);
        
        sisaltoRepository.save(sisalto);
        return mapper.mapAsList(sisalto.getLaajaalaisetOsaamiset(), LaajaalainenOsaaminenDto.class);
    }

    @Getter
    @Setter
    public static class WrapperDto {

        public WrapperDto(List<LaajaalainenOsaaminenDto> laajaalaisetOsaamiset) {
            this.laajaalaisetOsaamiset = laajaalaisetOsaamiset;
        }

        List<LaajaalainenOsaaminenDto> laajaalaisetOsaamiset;
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
