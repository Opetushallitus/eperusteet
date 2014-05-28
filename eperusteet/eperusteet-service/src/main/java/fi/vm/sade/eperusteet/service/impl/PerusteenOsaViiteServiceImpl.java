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

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Tila;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author harrik
 */
@Service
@Transactional(readOnly = true)
public class PerusteenOsaViiteServiceImpl implements PerusteenOsaViiteService{

    @Autowired
    private PerusteenOsaViiteRepository repository;
    @PersistenceContext
    private EntityManager em;
    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Override
    @Transactional(readOnly = false)
    public void removeSisalto(Long id) {
        PerusteenOsaViite viite = repository.findOne(id);
        if (viite == null) {
            throw new BusinessRuleViolationException("Perusteenosaviitettä ei ole olemassa");
        }

        if (viite.getVanhempi() == null) {
            throw new BusinessRuleViolationException("Suoritustavan juurielementtiä ei voi poistaa");
        }

        if (viite.getLapset() != null && !viite.getLapset().isEmpty() ) {
                throw new BusinessRuleViolationException("Sisällöllä on lapsia, ei voida poistaa");
        }

        if (viite.getPerusteenOsa() != null && viite.getPerusteenOsa().getTila().equals(Tila.LUONNOS)) {

            PerusteenOsa perusteenOsa = viite.getPerusteenOsa();
            perusteenOsaService.delete(perusteenOsa.getId());

            viite.setPerusteenOsa(null);
            viite.getVanhempi().getLapset().remove(viite);
            viite.setVanhempi(null);
            repository.delete(viite);
        }
    }
}
