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

import com.google.common.base.Optional;
import fi.ratamaa.dtoconverter.reflection.Property;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteVersion;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.*;

import static com.google.common.base.Optional.fromNullable;
import static java.util.Collections.singletonList;

/**
 * User: tommiratamaa
 * Date: 12.11.2015
 * Time: 14.57
 */
public class HibernateInterceptor extends EmptyInterceptor {
    private static final Map<Class<?>, List<Property>> routesToPeruste = new HashMap<>();

    @Override
    public boolean onFlushDirty(Object entity, Serializable id,
                                Object[] currentState, Object[] previousState,
                                String[] propertyNames, Type[] types) {
        updatePerusteRelatedTimestamps(entity);
        return false;
    }

    @Override
    public boolean onSave(Object entity, Serializable id,
                          Object[] state, String[] propertyNames, Type[] types) {
        updatePerusteRelatedTimestamps(entity);
        return false;
    }

    @Override
    public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
        if (collection instanceof PersistentCollection) {
            updatePerusteRelatedTimestamps(((PersistentCollection) collection).getOwner());
        }
    }

    @Override
    public void onCollectionRemove(Object collection, Serializable key) throws CallbackException {
        onCollectionUpdate(collection, key);
    }

    @Override
    public void onCollectionRecreate(Object collection, Serializable key) throws CallbackException {
        onCollectionUpdate(collection, key);
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        updatePerusteRelatedTimestamps(entity);
    }

    private void updatePerusteRelatedTimestamps(Object entity) {
        resolveRouteToPeruste(entity).stream()
                .map(route -> fromNullable(route.get(entity))).filter(Optional::isPresent)
                .map(Optional::get).forEach(target -> {
            if (target instanceof Collection) {
                Collection<?> collection = (Collection<?>) target;
                collection.stream().forEach(this::updateTimestamp);
            } else {
                this.updateTimestamp(target);
            }
        });
    }

    private void updateTimestamp(Object target) {
        if (Peruste.class.isAssignableFrom(resolveRealEntityClass(target))) {
            Peruste peruste = (Peruste) target;
            PerusteVersion version = peruste.getGlobalVersion();
            if (version == null) {
                version = new PerusteVersion(peruste);
                peruste.setGlobalVersion(version);
            }
            version.setAikaleima(new Date());
        } else {
            updatePerusteRelatedTimestamps(target);
        }
    }

    private List<Property> resolveRouteToPeruste(Object entity) {
        Class<?> entityClz = resolveRealEntityClass(entity);
        List<Property> paths = routesToPeruste.get(entityClz);
        if (paths == null) {
            paths = resolveRoutes(entityClz);
            routesToPeruste.put(entityClz, paths);
        }
        return paths;
    }

    private List<Property> resolveRoutes(Class<?> entityClz) {
        if (Peruste.class.isAssignableFrom(entityClz)) {
            return singletonList(Property.getVirtualThisPropertyForClass(entityClz));
        }
        RelatesToPeruste.Through relation = entityClz.getAnnotation(RelatesToPeruste.Through.class);
        if (relation == null) {
            return Property.findForAnnotation(entityClz, RelatesToPeruste.class);
        }
        return singletonList(Property.getForPath(entityClz, relation.value().split("\\.")));
    }

    private Class<?> resolveRealEntityClass(Object entity) {
        if (entity instanceof HibernateProxy) {
            return ((HibernateProxy) entity).getHibernateLazyInitializer().getPersistentClass();
        }
        return entity.getClass();
    }
}
