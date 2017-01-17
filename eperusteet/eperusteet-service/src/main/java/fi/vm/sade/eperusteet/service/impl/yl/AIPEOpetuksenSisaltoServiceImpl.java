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

import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineBaseDto;
import fi.vm.sade.eperusteet.service.yl.AIPEOpetuksenSisaltoService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author nkala
 */
@Service
public class AIPEOpetuksenSisaltoServiceImpl implements AIPEOpetuksenSisaltoService {

    @Override
    public <T extends OppiaineBaseDto> List<T> getOppiaineet(Long perusteId, Class<T> view) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T extends PerusteenOsaViiteDto<?>> T getSisalto(Long perusteId, Long sisaltoId, Class<T> view) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PerusteenOsaViiteDto.Matala addSisalto(Long perusteId, Long viiteId, PerusteenOsaViiteDto.Matala dto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeSisalto(Long perusteId, Long viiteId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
