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
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.*;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.util.*;
import java.util.stream.Stream;

import static com.google.common.base.Optional.fromNullable;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * Interceptor to update PerusteVersion timestamp automatically when any
 * Peruste related entity changes.
 *
 * @see RelatesToPeruste to mark the relation in entity classes.
 *
 * Will possibly cause additional extra lazy queries when saving or updating
 * a Peruste related entity. However, this is a tradeoff to circumvent the
 * need to remember to implement this in every change causing service method and
 * to deep copy the entity under possible change operation with all of its
 * related entities (and thereby cause lazy queries) and check (and impl the check)
 * if anything changed within.
 *
 * As an Hibernate Interceptor would break the general rule of not accessing Session or lazy
 * objects (although would work in this case). Thus implemented as an Integrator:
 * See https://docs.jboss.org/hibernate/orm/4.2/devguide/en-US/html/ch07.html#integrators
 *
 * User: tommiratamaa
 * Date: 12.11.2015
 * Time: 14.57
 */
public class PerusteRevisionIntegrator implements Integrator {
    private static final Map<Class<?>, List<Property>> routesToPeruste = new HashMap<>();

    protected void onCollectionUpdate(PersistentCollection collection) {
        updatePerusteRelatedTimestamps(collection.getOwner());
    }

    protected void onSaveOrUpdateEntity(Object entity) {
        updatePerusteRelatedTimestamps(entity);
    }

    protected void onDeleteEntity(Object entity) {
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
        return Stream.of(relation.value()).map(path -> Property.getForPath(entityClz, path.split("\\.")))
                .collect(toList());
    }

    private Class<?> resolveRealEntityClass(Object entity) {
        if (entity instanceof HibernateProxy) {
            return ((HibernateProxy) entity).getHibernateLazyInitializer().getPersistentClass();
        }
        return entity.getClass();
    }

    @Override
    public void integrate(Configuration configuration,
                          SessionFactoryImplementor sessionFactory,
                          SessionFactoryServiceRegistry serviceRegistry) {
        final EventListenerRegistry eventListenerRegistry = serviceRegistry.getService( EventListenerRegistry.class );
        eventListenerRegistry.appendListeners(EventType.FLUSH_ENTITY,
                new FlushEntityEventListener() {
                    @Override
                    public void onFlushEntity(FlushEntityEvent event) throws HibernateException {
                        if (event.isDirtyCheckHandledByInterceptor()
                                && event.getDirtyProperties().length > 0) {
                            // harmi vain, että tänne ei tulla
                            onSaveOrUpdateEntity(event.getEntity());
                        }
                    }
                });
        eventListenerRegistry.prependListeners(EventType.SAVE_UPDATE,
                new SaveOrUpdateEventListener() {
                    @Override
                    public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException {
                        onSaveOrUpdateEntity(event.getObject());
                    }
                });
        eventListenerRegistry.prependListeners(EventType.DELETE,
                new DeleteEventListener() {
                    @Override
                    public void onDelete(DeleteEvent event) throws HibernateException {
                        onDeleteEntity(event.getObject());
                    }

                    @Override
                    public void onDelete(DeleteEvent event, Set transientEntities) throws HibernateException {
                    }
                });
        eventListenerRegistry.prependListeners(EventType.POST_COLLECTION_UPDATE,
                new PostCollectionUpdateEventListener() {
                    @Override
                    public void onPostUpdateCollection(PostCollectionUpdateEvent event) {
                        onCollectionUpdate(event.getCollection());
                    }
                });
        eventListenerRegistry.prependListeners(EventType.POST_COLLECTION_REMOVE,
                new PostCollectionRemoveEventListener() {
                    @Override
                    public void onPostRemoveCollection(PostCollectionRemoveEvent event) {
                        onCollectionUpdate(event.getCollection());
                    }
                });
        eventListenerRegistry.prependListeners(EventType.POST_COLLECTION_RECREATE,
                new PostCollectionRecreateEventListener() {
                    @Override
                    public void onPostRecreateCollection(PostCollectionRecreateEvent event) {
                        onCollectionUpdate(event.getCollection());
                    }
                });
    }

    @Override
    public void integrate(MetadataImplementor metadata,
                          SessionFactoryImplementor sessionFactory,
                          SessionFactoryServiceRegistry serviceRegistry) {
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory,
                             SessionFactoryServiceRegistry serviceRegistry) {
    }
}
