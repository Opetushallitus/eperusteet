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
package fi.vm.sade.eperusteet.service.mapping;

import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import javax.persistence.EntityManager;
import javax.persistence.Inheritance;
import javax.persistence.PersistenceContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 *
 * @author teele1
 */
@Component
public class ReferenceableEntityConverter extends BidirectionalConverter<ReferenceableEntity, EntityReference> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
        return (this.sourceType.isAssignableFrom(sourceType) && this.destinationType.isAssignableFrom(destinationType))
            || (this.sourceType.isAssignableFrom(destinationType) && this.destinationType.isAssignableFrom(sourceType));
    }

    @Override
    public EntityReference convertTo(ReferenceableEntity s, Type<EntityReference> type) {
        return s.getReference();
    }

    @Override
    public ReferenceableEntity convertFrom(EntityReference reference, Type<ReferenceableEntity> type) {
        if (type.getRawType().isAnnotationPresent(Inheritance.class)) {
            /*
            perintähierarkioiden tapauksessa getReference() aiheuttaa ongelmia mappauksen kanssa
            (viitteen luokka on perintähierarkian isäluokka eikä "oikea" luokka)
             */
            ReferenceableEntity e = em.find(type.getRawType(), Long.valueOf(reference.getId()));
            if ( e == null ) {
                throw new fi.vm.sade.eperusteet.service.exception.IllegalArgumentException("virheellinen-viite-reference",
                        new HashMap<String, Object>(){{ put("reference", reference); }});
            }
        }
        return em.getReference(type.getRawType(), Long.valueOf(reference.getId()));
    }
}
