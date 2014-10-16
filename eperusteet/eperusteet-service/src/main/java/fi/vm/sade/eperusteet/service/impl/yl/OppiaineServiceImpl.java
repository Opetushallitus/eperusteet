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
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineenVuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.OppiaineRepository;
import fi.vm.sade.eperusteet.repository.OppiaineenVuosiluokkakokonaisuusRepository;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jhyoty
 */
@Service
@Transactional(readOnly = true)
public class OppiaineServiceImpl implements OppiaineService {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private OppiaineRepository oppiaineRepository;

    @Autowired
    private OppiaineenVuosiluokkakokonaisuusRepository vuosiluokkakokonaisuusRepository;

    @Autowired
    private PerusopetuksenPerusteenSisaltoRepository sisaltoRepository;

    private static final Logger LOG = LoggerFactory.getLogger(OppiaineServiceImpl.class);

    @Override
    @Transactional(readOnly = false)
    public OppiaineDto addOppiaine(Long perusteId, OppiaineDto dto) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        if (sisalto != null) {
            Oppiaine oppiaine = addOppiaine(dto, null);
            sisalto.addOppiaine(oppiaine);
            return mapper.map(oppiaine, OppiaineDto.class);
        }
        throw new BusinessRuleViolationException("Perustetta ei ole");
    }

    private Oppiaine addOppiaine(OppiaineDto dto, Oppiaine parent) {
        Oppiaine oppiaine = mapper.map(dto, Oppiaine.class);
        oppiaine = oppiaineRepository.save(oppiaine);
        if (parent != null) {
            parent.addOppimaara(oppiaine);
        }
        final Set<OppiaineenVuosiluokkaKokonaisuusDto> vuosiluokkakokonaisuudet = dto.getVuosiluokkakokonaisuudet();
        if (vuosiluokkakokonaisuudet != null) {
            for (OppiaineenVuosiluokkaKokonaisuusDto v : vuosiluokkakokonaisuudet) {
                addOppiaineenVuosiluokkaKokonaisuus(oppiaine, v);
            }
        }
        return oppiaine;
    }

    @Override
    @Transactional(readOnly = false)
    public OppiaineenVuosiluokkaKokonaisuusDto addOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, OppiaineenVuosiluokkaKokonaisuusDto dto) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);
        if (sisalto.containsOppiaine(aine)) {
            return mapper.map(addOppiaineenVuosiluokkaKokonaisuus(aine, dto), OppiaineenVuosiluokkaKokonaisuusDto.class);
        } else {
            throw new BusinessRuleViolationException("oppiaine ei kuulu tähän perusteeseen");
        }
    }

    private OppiaineenVuosiluokkaKokonaisuus addOppiaineenVuosiluokkaKokonaisuus(Oppiaine aine, OppiaineenVuosiluokkaKokonaisuusDto dto) {
        OppiaineenVuosiluokkaKokonaisuus ovk = mapper.map(dto, OppiaineenVuosiluokkaKokonaisuus.class);
        ovk.setId(null);
        aine.addVuosiluokkaKokonaisuus(ovk);
        ovk = vuosiluokkakokonaisuusRepository.save(ovk);
        return ovk;
    }

    @Override
    @Transactional(readOnly = false)
    public OppiaineDto addOppimaara(Long perusteId, Long oppiaineId, OppiaineDto dto) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        Oppiaine aine = oppiaineRepository.findOne(oppiaineId);
        if (sisalto == null || aine == null) {
            throw new BusinessRuleViolationException("Oppiainetta ei ole");
        } else {
            if (sisalto.containsOppiaine(aine)) {
                return mapper.map(addOppiaine(dto, aine), OppiaineDto.class);
            } else {
                throw new BusinessRuleViolationException("Oppiaine ei kuulu tähän sisältöön");
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public OppiaineDto deleteOppiaine(Long perusteId, Long oppiaineId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @Transactional(readOnly = false)
    public OppiaineenVuosiluokkaKokonaisuusDto deleteOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, Long vuosiluokkaKokonaisuusId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public OppiaineDto getOppiaine(Long perusteId, Long oppiaineId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        Oppiaine aine = oppiaineRepository.getOne(oppiaineId);
        if (sisalto != null && sisalto.containsOppiaine(aine)) {
            return mapper.map(aine, OppiaineDto.class);
        } else {
            throw new BusinessRuleViolationException("Pyydettyä oppiainetta ei ole");
        }
    }

    @Override
    public OppiaineenVuosiluokkaKokonaisuusDto getOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, Long vuosiluokkaKokonaisuusId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        OppiaineenVuosiluokkaKokonaisuus vk = vuosiluokkakokonaisuusRepository.findByIdAndOppiaineId(vuosiluokkaKokonaisuusId, oppiaineId);
        if (sisalto != null && vk != null && sisalto.containsOppiaine(vk.getOppiaine())) {
            return mapper.map(vuosiluokkakokonaisuusRepository.findOne(oppiaineId), OppiaineenVuosiluokkaKokonaisuusDto.class);
        } else {
            throw new BusinessRuleViolationException("Pyydettyä vuosiluokkakokonaisuutta ei ole");
        }
    }

    @Override
    public List<OppiaineSuppeaDto> getOppimaarat(Long perusteId, Long oppiaineId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        Oppiaine oa = oppiaineRepository.findOne(oppiaineId);
        if (sisalto.containsOppiaine(oa)) {
            return mapper.mapAsList(oa.getOppimaarat(), OppiaineSuppeaDto.class);
        }
        throw new BusinessRuleViolationException("Pyydettyä oppiainetta ei ole");
    }

    @Override
    @Transactional(readOnly = false)
    public OppiaineDto updateOppiaine(Long perusteId, OppiaineDto dto) {
        Oppiaine aine = oppiaineRepository.findOne(dto.getId());
        if (aine == null) {
            throw new BusinessRuleViolationException("Oppiainetta ei ole");
        }
        final Set<OppiaineenVuosiluokkaKokonaisuusDto> vuosiluokkakokonaisuudet = dto.getVuosiluokkakokonaisuudet();
        if (vuosiluokkakokonaisuudet != null) {
            for (OppiaineenVuosiluokkaKokonaisuusDto v : vuosiluokkakokonaisuudet) {
                if (v.getId() == null) {
                    addOppiaineenVuosiluokkaKokonaisuus(aine, v);
                } else {
                    updateOppiaineenVuosiluokkaKokonaisuus(perusteId, aine.getId(), v);
                }
                //TODO poisto -- vai pitäikö jättää "ominaisuudeksi" että vuosiluokkakokonaisuuksia ei voi poistaa tätä kautta.
            }
        }
        return mapper.map(aine, OppiaineDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public OppiaineenVuosiluokkaKokonaisuusDto updateOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, OppiaineenVuosiluokkaKokonaisuusDto dto) {
        OppiaineenVuosiluokkaKokonaisuus ovk = vuosiluokkakokonaisuusRepository.findOne(dto.getId());
        if (ovk == null || !ovk.getOppiaine().getId().equals(oppiaineId)) {
            throw new BusinessRuleViolationException("Vuosiluokkakokonaisuus ei kuulu tähän oppiaineeseen");
        }
        mapper.map(dto, ovk);
        ovk = vuosiluokkakokonaisuusRepository.save(ovk);
        return mapper.map(ovk, OppiaineenVuosiluokkaKokonaisuusDto.class);
    }

}
