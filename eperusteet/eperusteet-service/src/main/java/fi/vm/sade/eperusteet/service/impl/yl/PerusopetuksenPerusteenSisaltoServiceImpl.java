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
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.yl.PerusopetuksenPerusteenSisaltoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PerusopetuksenPerusteenSisaltoServiceImpl
        extends AbstractOppiaineOpetuksenSisaltoService<PerusopetuksenPerusteenSisalto>
        implements PerusopetuksenPerusteenSisaltoService {

    @Autowired
    protected PerusopetuksenPerusteenSisaltoRepository sisaltoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LaajaalainenOsaaminenDto> getLaajaalaisetOsaamiset(Long perusteId) {
        return mapper.mapAsList(getByPerusteId(perusteId).getLaajaalaisetosaamiset(), LaajaalainenOsaaminenDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VuosiluokkaKokonaisuusDto> getVuosiluokkaKokonaisuudet(Long perusteId) {
        return mapper.mapAsList(getByPerusteId(perusteId).getVuosiluokkakokonaisuudet(), VuosiluokkaKokonaisuusDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    protected PerusopetuksenPerusteenSisalto getByPerusteId(Long perusteId) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        if (sisalto == null) {
            throw new BusinessRuleViolationException("perusteen-sisaltoa-ei-loydy");
        }

        return sisalto;
    }
}
