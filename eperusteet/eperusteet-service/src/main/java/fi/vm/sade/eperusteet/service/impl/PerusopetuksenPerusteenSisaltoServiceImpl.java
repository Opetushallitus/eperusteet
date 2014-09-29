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
package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineenVuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.OppiaineRepository;
import fi.vm.sade.eperusteet.service.PerusopetuksenPerusteenSisaltoService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.List;
import java.util.Set;
import javax.validation.Validator;
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
public class PerusopetuksenPerusteenSisaltoServiceImpl implements PerusopetuksenPerusteenSisaltoService {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private OppiaineRepository oppiaineRepository;

    @Autowired
    private Validator validator;

    private static final Logger LOG = LoggerFactory.getLogger(PerusopetuksenPerusteenSisaltoServiceImpl.class);

    @Override
    public OppiaineDto addOppiaine(Long perusteId, OppiaineDto dto) {
        Set<OppiaineenVuosiluokkaKokonaisuusDto> vuosiluokkakokonaisuudet = dto.getVuosiluokkakokonaisuudet();
        dto.setVuosiluokkakokonaisuudet(null);

        Oppiaine oppiaine = mapper.map(dto, Oppiaine.class);
        oppiaine = oppiaineRepository.save(oppiaine);

        if ( vuosiluokkakokonaisuudet != null ) {
            for ( OppiaineenVuosiluokkaKokonaisuusDto v : vuosiluokkakokonaisuudet ) {
                addOppiaineenVuosiluokkaKokonaisuus(perusteId, oppiaine.getId(), v);
            }
        }
        return mapper.map(oppiaine, OppiaineDto.class);
    }

    @Override
    public OppiaineenVuosiluokkaKokonaisuusDto addOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, OppiaineenVuosiluokkaKokonaisuusDto dto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OppiaineDto addOppimaara(Long perusteId, Long oppiaineId, OppiaineDto dto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public VuosiluokkaKokonaisuusDto addVuosiluokkaKokonaisuus(Long perusteId, VuosiluokkaKokonaisuusDto dto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OppiaineDto deleteOppiaine(Long perusteId, Long oppiaineId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OppiaineenVuosiluokkaKokonaisuusDto deleteOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, Long vuosiluokkaKokonaisuusId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteVuosiluokkaKokonaisuus(Long perusteId, Long VuosiluokkaKokonaisuusId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<LaajaalainenOsaaminenDto> getLaajaAlainenOsaaminen(Long perusteId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OppiaineDto getOppiaine(Long perusteId, Long oppiaineId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OppiaineenVuosiluokkaKokonaisuusDto getOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, Long vuosiluokkaKokonaisuusId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<OppiaineDto> getOppiaineet(Long perusteId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<OppiaineDto> getOppimaarat(Long perusteId, Long oppiaineId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<VuosiluokkaKokonaisuusDto> getVuosiluokkaKokonaisuudet(Long perusteId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public VuosiluokkaKokonaisuusDto getVuosiluokkaKokonaisuus(Long perusteId, Long VuosiluokkaKokonaisuusId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<LaajaalainenOsaaminenDto> updateLaajaAlainenOsaaminen(Long perusteId, List<LaajaalainenOsaaminenDto> dtos) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OppiaineDto updateOppiaine(Long perusteId, OppiaineDto dto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OppiaineenVuosiluokkaKokonaisuusDto updateOppiaineenVuosiluokkaKokonaisuus(Long perusteId, Long oppiaineId, OppiaineenVuosiluokkaKokonaisuusDto dto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public VuosiluokkaKokonaisuusDto updateVuosiluokkaKokonaisuus(Long perusteId, VuosiluokkaKokonaisuusDto dto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
