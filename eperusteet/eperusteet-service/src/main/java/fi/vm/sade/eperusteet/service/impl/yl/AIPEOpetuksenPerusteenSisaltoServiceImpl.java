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

import fi.vm.sade.eperusteet.dto.yl.AIPEKurssiDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEKurssiSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEVaiheDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEVaiheSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.service.yl.AIPEOpetuksenPerusteenSisaltoService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author nkala
 */
@Service
public class AIPEOpetuksenPerusteenSisaltoServiceImpl implements AIPEOpetuksenPerusteenSisaltoService {

    @Override
    public LaajaalainenOsaaminenDto getLaajaalainen(Long perusteId, Long laajalainenId) {
        return null;
    }

    @Override
    public LaajaalainenOsaaminenDto addLaajaalainen(Long perusteId, LaajaalainenOsaaminenDto laajaalainenDto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LaajaalainenOsaaminenDto updateLaajaalainen(Long perusteId, Long laajalainenId, LaajaalainenOsaaminenDto laajaalainenDto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeLaajaalainen(Long perusteId, Long laajalainenId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AIPEKurssiDto getKurssi(Long perusteId, Long vaiheId, Long oppiaineId, Long kurssiId) {
        return null;
    }

    @Override
    public List<AIPEKurssiSuppeaDto> getKurssit(Long perusteId, Long vaiheId, Long oppiaineId) {
        return new ArrayList<>();
    }

    @Override
    public AIPEKurssiDto addKurssi(Long perusteId, Long vaiheId, Long oppiaineId, AIPEKurssiDto kurssiDto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AIPEKurssiDto updateKurssi(Long perusteId, Long vaiheId, Long oppiaineId, Long kurssiId, AIPEKurssiDto kurssiDto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeKurssi(Long perusteId, Long vaiheId, Long oppiaineId, Long kurssiId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AIPEOppiaineDto getOppiaine(Long perusteId, Long vaiheId, Long oppiaineId) {
        return null;
    }

    @Override
    public AIPEOppiaineDto updateOppiaine(Long perusteId, Long vaiheId, Long oppiaineId, AIPEOppiaineDto oppiaineDto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AIPEOppiaineDto addOppiaine(Long perusteId, Long vaiheId, AIPEOppiaineDto oppiaineDto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeOppiaine(Long perusteId, Long vaiheId, Long oppiaineId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AIPEOppiaineDto addOppiaine(Long perusteId, Long vaiheId, Long oppiaineId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<AIPEOppiaineSuppeaDto> getOppiaineet(Long perusteId, Long vaiheId) {
        return new ArrayList<>();
    }

    @Override
    public AIPEVaiheDto getVaihe(Long perusteId, Long vaiheId) {
        return null;
    }

    @Override
    public List<AIPEVaiheSuppeaDto> getVaiheet(Long perusteId) {
        return new ArrayList<>();
    }

    @Override
    public AIPEVaiheDto addVaihe(Long perusteId, AIPEVaiheDto vaiheDto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AIPEVaiheDto updateVaihe(Long perusteId, Long vaiheId, AIPEVaiheDto vaiheDto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeVaihe(Long perusteId, Long vaiheId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<LaajaalainenOsaaminenDto> getLaajaalaiset(Long perusteId) {
        return new ArrayList<>();
    }

}
