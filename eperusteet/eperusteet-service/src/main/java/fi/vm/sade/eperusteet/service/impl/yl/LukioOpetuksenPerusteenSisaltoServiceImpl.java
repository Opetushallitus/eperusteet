/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.yl.lukio.LukioOpetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.repository.LukioOpetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.service.yl.LukioOpetuksenPerusteenSisaltoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: tommiratamaa
 * Date: 21.9.15
 * Time: 15.33
 */
@Service
public class LukioOpetuksenPerusteenSisaltoServiceImpl
        extends AbstractOppiaineOpetuksenSisaltoService<LukioOpetuksenPerusteenSisalto>
        implements LukioOpetuksenPerusteenSisaltoService {

    @Autowired
    private LukioOpetuksenPerusteenSisaltoRepository sisaltoRepository;

    @Override
    protected LukioOpetuksenPerusteenSisalto getByPerusteId(Long perusteId) {
        LukioOpetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        assertExists(sisalto, "Perustetta ei ole olemassa");
        return sisalto;
    }
}
