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
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.annotation.Identifiable;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.service.event.PerusteUpdateStore;
import lombok.Getter;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * Interceptor to update PerusteVersion timestamp automatically when any
 * Peruste related entity changes.
 *
 * @see RelatesToPeruste to mark the relation in entity classes.
 *
 * Breaks the general rule of Hibernate Intercetpr not to reference Session or
 * lazy collections within * interceptor methods. However, works in this case.
 *
 * Will possibly cause additional extra lazy queries when saving or updating
 * a Peruste related entity. However, this is a tradeoff to circumvent the
 * need to remember to implement this in every change causing service method and
 * to deep copy the entity under possible change operation with all of its
 * related entities (and thereby cause lazy queries) and check (and impl the check)
 * if anything changed within.
 *
 * User: tommiratamaa
 * Date: 12.11.2015
 * Time: 14.57
 */
public class HibernateInterceptor extends EmptyInterceptor {
    private static final Map<Class<?>, List<Property>> routesToPeruste = new HashMap<>();

    @Autowired
    private PerusteUpdateStore perusteUpdateStore;

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
        // Store require request scope
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return;
        }

        Callback callback = new Callback(id -> perusteUpdateStore.perusteUpdated(id));
        findRelatedPeruste(entity, callback);
        if (!callback.isFound() && entity instanceof Identifiable) {
            Class<?> entityClz = resolveRealEntityClass(entity);
            if (entityClz.isAnnotationPresent(RelatesToPeruste.FromAnywhereReferenced.class)) {
                Long id = ((Identifiable) entity).getId();
                if (id != null) {
                    perusteUpdateStore.resolveRelationLater(entityClz, id);
                }
            }
        }
    }

    @Getter
    private static class Callback {
        private final Consumer<Long> perusteIdConsumer;
        private boolean found = false;

        private Callback(Consumer<Long> perusteIdConsumer) {
            this.perusteIdConsumer = perusteIdConsumer;
        }

        public void found(Long perusteId) {
            this.found = true;
            perusteIdConsumer.accept(perusteId);
        }
    }

    public static void findRelatedPeruste(Object entity, Consumer<Long> consumer) {
        findRelatedPeruste(entity, new Callback(consumer));
    }

    private static void findRelatedPeruste(Object entity, Callback callback) {
        resolveRouteToPeruste(entity).stream()
                .map(route -> Optional.ofNullable(route.get(entity)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(target -> {
            if (target instanceof Collection) {
                Collection<?> collection = (Collection<?>) target;
                collection.forEach(o -> proceed(o, callback));
            } else {
                proceed(target, callback);
            }
        });
    }

    private static void proceed(Object target, Callback callback) {
        if (Peruste.class.isAssignableFrom(resolveRealEntityClass(target))) {
            Peruste peruste = (Peruste) target;
            if (peruste.getId() != null) {
                callback.found(peruste.getId());
            }
        } else {
            findRelatedPeruste(target, callback);
        }
    }

    private static List<Property> resolveRouteToPeruste(Object entity) {
        Class<?> entityClz = resolveRealEntityClass(entity);
        List<Property> paths = routesToPeruste.get(entityClz);
        if (paths == null) {
            paths = resolveRoutes(entityClz);
            routesToPeruste.put(entityClz, paths);
        }
        return paths;
    }

    private static List<Property> resolveRoutes(Class<?> entityClz) {
        if (Peruste.class.isAssignableFrom(entityClz)) {
            return singletonList(Property.getVirtualThisPropertyForClass(entityClz));
        }
        RelatesToPeruste.Through relation = entityClz.getAnnotation(RelatesToPeruste.Through.class);
        if (relation == null) {
            return Property.findForAnnotation(entityClz, RelatesToPeruste.class);
        }
        return Stream.of(relation.value()).map(path -> Property.getForPath(entityClz, path.split("\\.")))
                .collect(toList());
    }

    private static Class<?> resolveRealEntityClass(Object entity) {
        if (entity instanceof HibernateProxy) {
            return ((HibernateProxy) entity).getHibernateLazyInitializer().getPersistentClass();
        }
        return entity.getClass();
    }

}
