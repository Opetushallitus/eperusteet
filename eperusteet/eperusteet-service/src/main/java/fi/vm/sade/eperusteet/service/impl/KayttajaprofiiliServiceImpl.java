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

import fi.vm.sade.eperusteet.domain.Kayttajaprofiili;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.repository.KayttajaprofiiliRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.KayttajaprofiiliService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author harrik
 */
@Service
public class KayttajaprofiiliServiceImpl implements KayttajaprofiiliService {
    private static final Logger LOG = LoggerFactory.getLogger(KayttajaprofiiliServiceImpl.class);
    
    @Autowired
    KayttajaprofiiliRepository kayttajaprofiiliRepo;
    
    @Autowired
    PerusteRepository perusteRepo;
    
    @Override
    @Transactional
    public Kayttajaprofiili get(final Long id) {
        LOG.info("get " + kayttajaprofiiliRepo);

        Kayttajaprofiili k = kayttajaprofiiliRepo.findOneEager(id);
        
        return k;
    }

    @Override
    @Transactional
    public Kayttajaprofiili addSuosikki(final Long id, final Long perusteId) {
        LOG.info("addSuosikki " + perusteId);
        
        Kayttajaprofiili kayttajaprofiili = kayttajaprofiiliRepo.findOneEager(id);
        Peruste peruste = perusteRepo.findOne(perusteId);
        
        if (!kayttajaprofiili.getSuosikit().contains(peruste)) {
            kayttajaprofiili.getSuosikit().add(peruste);
        }
        
        return kayttajaprofiili;
    }

    @Override
    @Transactional
    public Kayttajaprofiili deleteSuosikki(Long id, Long perusteId) throws IllegalArgumentException {
        LOG.info("deleteSuosikki " + perusteId);
        
        Kayttajaprofiili kayttajaprofiili = kayttajaprofiiliRepo.findOneEager(id);
        if (kayttajaprofiili == null) {
            throw new IllegalArgumentException("Käyttäjäprofilia ei ole olemassa.");
        }
        Peruste peruste = perusteRepo.findOne(perusteId);
        
        kayttajaprofiili.getSuosikit().remove(peruste);
        
        return kayttajaprofiili;
    }
}
