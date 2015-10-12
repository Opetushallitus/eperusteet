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

import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.lukiokoulutus.AihekokonaisuudetYleiskuvausDto;
import fi.vm.sade.eperusteet.dto.lukiokoulutus.LukiokoulutuksenYleisetTavoitteetDto;
import fi.vm.sade.eperusteet.repository.LukiokoulutuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.service.yl.LukiokoulutuksenPerusteenSisaltoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: tommiratamaa
 * Date: 21.9.15
 * Time: 15.33
 */
@Service
public class LukiokoulutuksenPerusteenSisaltoServiceImpl
        extends AbstractOppiaineOpetuksenSisaltoService<LukiokoulutuksenPerusteenSisalto>
        implements LukiokoulutuksenPerusteenSisaltoService {

    @Autowired
    private LukiokoulutuksenPerusteenSisaltoRepository sisaltoRepository;

    @Override
    protected LukiokoulutuksenPerusteenSisalto getByPerusteId(Long perusteId) {
        LukiokoulutuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        assertExists(sisalto, "Sisaltoä annetulle perusteelle ei ole olemassa");
        return sisalto;
    }

    @Override
    public AihekokonaisuudetYleiskuvausDto getAihekokonaisuudet(Long perusteId) {
        LukiokoulutuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        assertExists(sisalto, "Sisaltoä annetulle perusteelle ei ole olemassa");
        return mapper.map(sisalto.getAihekokonaisuudet(), AihekokonaisuudetYleiskuvausDto.class);
    }

    @Override
    public LukiokoulutuksenYleisetTavoitteetDto getYleisetTavoitteet(Long perusteId) {
        return null;
    }
}
