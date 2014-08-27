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
package fi.vm.sade.eperusteet.service.security;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.repository.authorization.PerusteprojektiPermissionRepository;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

/**
 * Oikeuksien tarkistelu.
 *
 * @author jhyoty
 */
public class PermissionEvaluator implements org.springframework.security.access.PermissionEvaluator {

    @Autowired
    private PerusteprojektiPermissionRepository perusteProjektit;

    private static final Logger LOG = LoggerFactory.getLogger(PermissionEvaluator.class);
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        //TODO
        LOG.error("ei toteutettu");
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {

        final EnumSet<ProjektiTila> muokkausTilat = EnumSet.complementOf(EnumSet.of(ProjektiTila.JULKAISTU, ProjektiTila.POISTETTU));
        Set<ProjektiTila> tila = findPerusteProjektiTila(targetType, targetId);
        tila.retainAll(muokkausTilat);
        boolean verdict = !tila.isEmpty() && authentication.isAuthenticated();
        LOG.warn(String.format("%s to %s{id=%s} by %s: verdict: %s", permission, targetType, targetId, authentication, verdict));
        return verdict;
    }

    private Set<ProjektiTila> findPerusteProjektiTila(String targetType, Serializable targetId) {

        if (!(targetId instanceof Long)) {
            throw new IllegalArgumentException("Expected Long");
        }
        final Long id = (Long) targetId;

        // keskeneräiset perusteprojektit joihin käyttäjällä on jokin oikeus
        // näistä tarvitaan projektin tila
        switch (targetType.toLowerCase()) {
            case "peruste": {
                Perusteprojekti pp = perusteProjektit.findByPeruste(id);
                return pp == null ? Collections.<ProjektiTila>emptySet() : Collections.singleton(pp.getTila());
            }
            case "perusteprojekti": {
                Perusteprojekti pp = perusteProjektit.findOne(id);
                return pp == null ? Collections.<ProjektiTila>emptySet() : Collections.singleton(pp.getTila());
            }
            case "perusteenosa": {
                return Sets.newHashSet(perusteProjektit.findTilaByPerusteenOsaId(id));
            }
            default:
                throw new IllegalArgumentException(targetType);
        }
    }

}
