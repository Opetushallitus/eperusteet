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

import com.fasterxml.jackson.annotation.JsonTypeName;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

/**
 * Muutaa luokan nimen tyyppitiedoksi. Oletuksena tyyppi
 * @author jhyoty
 */
class TypeNameConverter extends CustomConverter<Class<?>, String>{

    @Override
    public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
        return this.sourceType.isAssignableFrom(sourceType) && this.destinationType.isAssignableFrom(destinationType);
    }

    @Override
    public String convert(Class<?> source, Type<? extends String> destinationType, MappingContext mappingContext) {
        if (source.isAnnotationPresent(JsonTypeName.class)) {
            return source.getAnnotation(JsonTypeName.class).value();
        }
        return source.getSimpleName().toLowerCase();
    }

}
