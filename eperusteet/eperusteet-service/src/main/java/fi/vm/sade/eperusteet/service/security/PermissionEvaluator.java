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
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.repository.authorization.PerusteprojektiPermissionRepository;
import fi.vm.sade.eperusteet.service.util.Pair;
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

    @Autowired
    private PermissionHelper helper;

    private static final Logger LOG = LoggerFactory.getLogger(PermissionEvaluator.class);

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if ( targetDomainObject instanceof ReferenceableEntity ) {
           String targetType;
           if ( targetDomainObject instanceof PerusteenOsa ) {
               targetType = "perusteenosa";
           } else {
               targetType = targetDomainObject.getClass().getSimpleName();
           }
           return hasPermission(authentication, ((ReferenceableEntity)targetDomainObject).getId(), targetType, permission);
        }
        return false;
    }

    private static final EnumSet<ProjektiTila> MUOKKAUS_TILAT = EnumSet.complementOf(EnumSet.of(ProjektiTila.JULKAISTU, ProjektiTila.POISTETTU));

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        LOG.warn(String.format("Checking permission %s to %s{id=%s} by %s", permission, targetType, targetId, authentication));

        if ( targetId == null ) {
            //yleiset oikeudet jotka eivät kohdistu mihinkään objektiin; esim. perusteprojektin luonti
            LOG.warn("*** yleisten oikeuksien tarkistusta ei ole vielä toteutettu ***");
            return true;
        }

        if ("LUKU".equals(permission) && targetId != null) {
            //tarkistetaan onko lukuoikeus suoraan julkaistu -statuksen perusteella
            if (PerusteTila.VALMIS == helper.findPerusteTilaFor(targetType, targetId)) {
                return true;
            }
        }

        boolean verdict = false;
        if (authentication.isAuthenticated()) {
            for (Pair<String, ProjektiTila> p : findPerusteProjektiTila(targetType, targetId)) {
                if (MUOKKAUS_TILAT.contains(p.getSecond())) {
                    verdict = true;
                    break;
                }
            }
        }
        return verdict;
    }

    private Set<Pair<String, ProjektiTila>> findPerusteProjektiTila(String targetType, Serializable targetId) {

        if (!(targetId instanceof Long)) {
            throw new IllegalArgumentException("Expected Long");
        }
        final Long id = (Long) targetId;

        final Set<Pair<String, ProjektiTila>> empty = Collections.emptySet();
        switch (targetType.toLowerCase()) {
            case "peruste": {
                return Sets.newHashSet(perusteProjektit.findByPeruste(id));
            }
            case "perusteprojekti": {
                Perusteprojekti pp = perusteProjektit.findOne(id);
                return pp == null ? empty : Collections.singleton(Pair.of(pp.getRyhmaOid(), pp.getTila()));
            }
            case "perusteenosa": {
                return Sets.newHashSet(perusteProjektit.findTilaByPerusteenOsaId(id));
            }
            default:
                throw new IllegalArgumentException(targetType);
        }
    }

}
