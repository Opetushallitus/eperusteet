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

import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.VuosiluokkaKokonaisuusRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkakokonaisuusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jhyoty
 */
@Service
@Transactional(readOnly = false)
public class VuosiluokkaKokonaisuusServiceImpl implements VuosiluokkakokonaisuusService {

    @Autowired
    private VuosiluokkaKokonaisuusRepository kokonaisuusRepository;
    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusopetuksenPerusteenSisaltoRepository sisaltoRepository;

    @Override
    @Transactional
    public VuosiluokkaKokonaisuusDto addVuosiluokkaKokonaisuus(Long perusteId, VuosiluokkaKokonaisuusDto dto) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        VuosiluokkaKokonaisuus vk = mapper.map(dto, VuosiluokkaKokonaisuus.class);
        vk = kokonaisuusRepository.save(vk);
        sisalto.addVuosiluokkakokonaisuus(vk);
        return mapper.map(vk, VuosiluokkaKokonaisuusDto.class);
    }

    @Override
    public void deleteVuosiluokkaKokonaisuus(Long perusteId, Long VuosiluokkaKokonaisuusId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VuosiluokkaKokonaisuusDto getVuosiluokkaKokonaisuus(Long perusteId, Long VuosiluokkaKokonaisuusId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VuosiluokkaKokonaisuusDto updateVuosiluokkaKokonaisuus(Long perusteId, VuosiluokkaKokonaisuusDto dto) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
