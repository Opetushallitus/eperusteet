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

import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.SuoritustapaRepository;
import fi.vm.sade.eperusteet.service.SuoritustapaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author harrik
 */
@Service
public class SuoritustapaServiceImpl implements SuoritustapaService {
    
    @Autowired
    private SuoritustapaRepository suoritustapaRepository;
    @Autowired
    private PerusteenOsaViiteRepository perusteenOsaViiteRepository;

    @Override
    @Transactional
    public Suoritustapa createSuoritustapaWithSisaltoRoot(Suoritustapakoodi suoritustapakoodi) {
        Suoritustapa suoritustapa = new Suoritustapa();
        
        suoritustapa.setSuoritustapakoodi(suoritustapakoodi);
        PerusteenOsaViite perusteenOsaViite = new PerusteenOsaViite();
        perusteenOsaViite = perusteenOsaViiteRepository.save(perusteenOsaViite);

        suoritustapa.setSisalto(perusteenOsaViite);
        suoritustapa = suoritustapaRepository.save(suoritustapa);
        
        return suoritustapa;
    }
    
}
