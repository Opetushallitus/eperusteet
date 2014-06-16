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

import com.google.common.eventbus.EventBus;
import java.io.Serializable;
import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jhyoty
 */
public class RevisionListener implements EntityTrackingRevisionListener {
    private static final Logger LOG = LoggerFactory.getLogger(RevisionListener.class);

    private static final EventBus eventBus = new EventBus("revisionListener");

    public static EventBus getEventBus() {
        return eventBus;
    }
    
    @Override
    public void entityChanged(Class entityClass, String entityName, Serializable entityId, RevisionType revisionType, Object revisionEntity) {
        LOG.info("CHANGED: " + entityName +  " " + revisionEntity);
        eventBus.post(entityClass);
    }

    @Override
    public void newRevision(Object revisionEntity) {
        LOG.info("NEW REVISION" + revisionEntity.toString());
    }

}
