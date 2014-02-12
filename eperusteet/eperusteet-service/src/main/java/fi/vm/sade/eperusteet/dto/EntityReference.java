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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;

/**
 *
 * @author teele1
 * @param <E>
 */
@JsonSerialize(using = EntityReferenceSerializer.class)
@JsonDeserialize(using = EntityReferenceDeserializer.class)
public class EntityReference<E> {
    
    @Getter
    private final String id;
    
    @Getter
    private final String entityClass;
    
    public EntityReference(Long id, Class<E> entityClass) {
        this.id = id.toString();
        this.entityClass = entityClass.getSimpleName().toLowerCase();
    }
    
    public EntityReference(String id, String entityClass) {
        this.id = id;
        this.entityClass = entityClass;
    }
}
