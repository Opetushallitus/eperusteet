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

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

/**
 *
 * @author teele1
 */
@Component
public class ReferenceableEntityConverter extends BidirectionalConverter<ReferenceableEntity, EntityReference> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public EntityReference convertTo(ReferenceableEntity source, Type<EntityReference> destinationType, MappingContext mappingContext) {
        return source.getReference();
    }

    @Override
    public ReferenceableEntity convertFrom(EntityReference source, Type<ReferenceableEntity> destinationType, MappingContext mappingContext) {
        if (destinationType.getRawType().isAnnotationPresent(Inheritance.class)) {
            // Perintähierarkioiden tapauksessa getReference() aiheuttaa ongelmia mappauksen kanssa
            // (viitteen luokka on perintähierarkian isäluokka eikä "oikea" luokka)
            ReferenceableEntity e = em.find(destinationType.getRawType(), Long.valueOf(source.getId()));
            if ( e == null ) {
                throw new IllegalArgumentException("Virheellinen viite " + source);
            }
        }
        return em.getReference(destinationType.getRawType(), Long.valueOf(source.getId()));
    }

    @Override
    public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
        return (this.sourceType.isAssignableFrom(sourceType) && this.destinationType.isAssignableFrom(destinationType))
            || (this.sourceType.isAssignableFrom(destinationType) && this.destinationType.isAssignableFrom(sourceType));
    }

}
