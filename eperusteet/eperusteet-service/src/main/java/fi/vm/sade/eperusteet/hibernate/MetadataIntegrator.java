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

package fi.vm.sade.eperusteet.hibernate;

import fi.ratamaa.dtoconverter.reflection.Property;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: tommiratamaa
 * Date: 27.11.2015
 * Time: 16.24
 */
public class MetadataIntegrator implements Integrator {
    private static Map<Class<?>, List<Property>> anywhareReferencedFromProperties = new HashMap<>();

    @Override
    public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        configuration.getClassMappings().forEachRemaining(
                mapping -> Property.mapForClass(mapping.getMappedClass())
                        .values().stream().filter(p -> p.getContainedTypeOrType()
                                .isAnnotationPresent(RelatesToPeruste.FromAnywhereReferenced.class)
                            && !p.isAnnotationPresent(Transient.class))
                        .forEach(p -> classPropertyList(p.getContainedTypeOrType()).add(p)));
    }

    private List<Property> classPropertyList(Class<?> clz) {
        List<Property> list = anywhareReferencedFromProperties.get(clz);
        if (list == null) {
            list = new ArrayList<>();
            anywhareReferencedFromProperties.put(clz, list);
        }
        return list;
    }

    public static List<Property> findPropertiesReferencingTo(Class<?> clz) {
        List<Property> list = anywhareReferencedFromProperties.get(clz);
        return list == null ? new ArrayList<>() : list;
    }

    @Override
    public void integrate(MetadataImplementor metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        anywhareReferencedFromProperties = new HashMap<>();
    }
}
