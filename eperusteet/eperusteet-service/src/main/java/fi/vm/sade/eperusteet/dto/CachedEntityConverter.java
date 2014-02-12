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

package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.domain.CachedEntity;
import javax.persistence.EntityManager;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author teele1
 */
public class CachedEntityConverter extends BidirectionalConverter<CachedEntity, Long>{

    private static final Logger LOG = LoggerFactory.getLogger(CachedEntityConverter.class);
    private final EntityManager em;
    
    public CachedEntityConverter(EntityManager em) {
        this.em = em;
    }
    
    @Override
    public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {       
        return (this.sourceType.isAssignableFrom(sourceType) && this.destinationType.equals(destinationType))
                || (this.sourceType.isAssignableFrom(destinationType) && this.destinationType.equals(sourceType));
    }

    @Override
    public Long convertTo(CachedEntity s, Type<Long> type) {
        return s.getId();
    }

    @Override
    public CachedEntity convertFrom(Long id, Type<CachedEntity> type) {
        return em.getReference(type.getRawType(), id);
        
    }
}
