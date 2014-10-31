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

import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.domain.yl.OppiaineenVuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.VuosiluokkaKokonaisuusRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkakokonaisuusService;
import java.util.HashSet;
import java.util.List;
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
    public VuosiluokkaKokonaisuusDto getVuosiluokkaKokonaisuus(Long perusteId, Long kokonaisuusId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        VuosiluokkaKokonaisuus vk = kokonaisuusRepository.findOne(kokonaisuusId);
        if (sisalto != null && vk != null && sisalto.containsVuosiluokkakokonaisuus(vk)) {
            return mapper.map(vk, VuosiluokkaKokonaisuusDto.class);
        }
        throw new BusinessRuleViolationException("Haettu vuosiluokkakokonaisuus ei kuulu tähän perusteeseen");
    }

    @Override
    public List<OppiaineSuppeaDto> getOppiaineet(Long perusteId, Long kokonaisuusId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        VuosiluokkaKokonaisuus vk = kokonaisuusRepository.findOne(kokonaisuusId);
        if (sisalto != null && vk != null && sisalto.containsVuosiluokkakokonaisuus(vk)) {
            HashSet<Oppiaine> aineet = new HashSet<>();
            for (OppiaineenVuosiluokkaKokonaisuus o : vk.getOppiaineet()) {
                Oppiaine a = o.getOppiaine();
                while (a.getOppiaine() != null) {
                    a = a.getOppiaine();
                }
                aineet.add(a);
            }
            return mapper.mapAsList(aineet, OppiaineSuppeaDto.class);
        }
        throw new BusinessRuleViolationException("Haettu vuosiluokkakokonaisuus ei kuulu tähän perusteeseen");
    }

    @Override
    public VuosiluokkaKokonaisuusDto updateVuosiluokkaKokonaisuus(Long perusteId, VuosiluokkaKokonaisuusDto dto) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        VuosiluokkaKokonaisuus vk = kokonaisuusRepository.findOne(dto.getId());
        if (sisalto != null && vk != null && sisalto.containsVuosiluokkakokonaisuus(vk)) {
            mapper.map(dto, vk);
            kokonaisuusRepository.save(vk);
            return mapper.map(vk, VuosiluokkaKokonaisuusDto.class);
        }
        throw new BusinessRuleViolationException("Vuosiluokkakokonaisuus ei kuulu tähän perusteeseen");
    }
}
