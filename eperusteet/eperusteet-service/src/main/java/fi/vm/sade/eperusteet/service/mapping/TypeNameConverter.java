package fi.vm.sade.eperusteet.service.mapping;

import com.fasterxml.jackson.annotation.JsonTypeName;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

/**
 * Muutaa luokan nimen tyyppitiedoksi. Oletuksena tyyppi
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
