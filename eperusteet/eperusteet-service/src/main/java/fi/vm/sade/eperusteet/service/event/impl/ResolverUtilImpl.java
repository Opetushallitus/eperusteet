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

package fi.vm.sade.eperusteet.service.event.impl;

import fi.ratamaa.dtoconverter.reflection.Property;
import fi.vm.sade.eperusteet.hibernate.HibernateInterceptor;
import fi.vm.sade.eperusteet.hibernate.MetadataIntegrator;
import fi.vm.sade.eperusteet.service.event.ResolvableReferenced;
import fi.vm.sade.eperusteet.service.event.ResolverUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: tommiratamaa
 * Date: 27.11.2015
 * Time: 16.03
 */
@Component
public class ResolverUtilImpl implements ResolverUtil {
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public Set<Long> findPerusteIdsByFirstResolvable(ResolvableReferenced resolvable) {
        Set<Long> ids = new HashSet<>();
        for (Property referencingProperty : MetadataIntegrator.findPropertiesReferencingTo(resolvable.getEntityClass())) {
            List owners = em.createQuery("select t from "
                    + referencingProperty.getDirectDeclaringClass().getSimpleName()
                    + " t where t." + referencingProperty.getName() + ".id = :id")
                    .setParameter("id", resolvable.getId()).getResultList();
            for (Object entity : owners) {
                // most likely none
                HibernateInterceptor.findRelatedPeruste(entity, ids::add);
                if (!ids.isEmpty()) {
                    return ids;
                }
            }
        }
        return ids;
    }
}
