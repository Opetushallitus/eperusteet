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
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.dto.Reference;
import ma.glasnost.orika.*;
import ma.glasnost.orika.metadata.Type;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import java.util.Collection;
import java.util.Optional;

/**
 * Tuki Javan Optional-luokalle Orika mapperin yhteydessä.
 * <p>
 * Tarkoitettu Dto-Entiteetti-Dto mappaukseen.
 * <p>
 * Mahdollistaa mappauksen siten, että DTO-luokissa voi määritellä attribuutteja Optional&lt;Attr&gt; ja mappaus entiteetteihin toimii seuraavasti:
 * <ul>
 * <li>null: pidetään kohdearvo
 * <li>present: mapätään rekursiivisesti kohdearvoon
 * <li>empty: asetetaan kohdearvo NULL-arvoksi
 * </ul>
 * TODO: Kohdearvo ei voi olla itse Optional (ainakaan kaikissa tapauksissa).
 *
 * @author jhyoty
 */
public final class OptionalSupport {

    private OptionalSupport() {
    }

    public static void register(MapperFactory factory) {
        factory.getConverterFactory().registerConverter(new ToOptionalConverter());
        factory.getConverterFactory().registerConverter(new OptionalImmutableConverter());
        factory.getConverterFactory().registerConverter(new OptionalEntitityReferenceConverter());
        factory.registerFilter(new Filter());
        factory.registerFilter(new CollectionFilter());
        factory.registerMapper(new Mapper());
    }

    static final class Mapper extends CustomMapper<Optional<?>, Object> {

        @Override
        public void mapAtoB(Optional<?> a, Object b, MappingContext context) {
            if (a.isPresent()) {
                mapperFacade.map(a.get(), unproxy(b), context);
            } else {
                throw new MappingException("Optional.absent havaittu");
            }
        }

        @Override
        //ToOptionalConverter hoitaa tämän suunnan
        public void mapBtoA(Object b, Optional<?> a, MappingContext context) {
            throw new MappingException("mapBtoA ei ole tuettu");
        }

    }

    static final class Filter extends NullFilter<Optional<?>, Object> {

        @Override
        public <S extends Optional<?>> S filterSource(S sourceValue, Type<S> sourceType, String sourceName, Type<?> destType, String destName, MappingContext mappingContext) {
            if (sourceValue != null && !sourceValue.isPresent()) {
                return null;
            }
            return sourceValue;
        }

        @Override
        public boolean filtersSource() {
            return true;
        }

        @Override
        public <S extends Optional<?>, D> boolean shouldMap(Type<S> sourceType, String sourceName, S source, Type<D> destType, String destName, D dest, MappingContext mappingContext) {
            return source != null;
        }

    }

    static final class CollectionFilter extends NullFilter<Collection<?>, Collection<?>> {

        @Override
        public <S extends Collection<?>, D extends Collection<?>> boolean shouldMap(Type<S> sourceType, String sourceName, S source, Type<D> destType, String destName, D dest, MappingContext mappingContext) {
            return source != null;
        }

    }

    static final class ToOptionalConverter extends CustomConverter<Object, Optional<?>> {

        @Override
        public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
            return this.destinationType.isAssignableFrom(destinationType);
        }

        @Override
        public Optional<?> convert(Object source, Type<? extends Optional<?>> destinationType, MappingContext mappingContext) {
            if (source != null) {
                return Optional.of(mapperFacade.map(source, destinationType.getComponentType().getRawType()));
            }
            return null;
        }
    }

    static final class OptionalEntitityReferenceConverter extends CustomConverter<Optional<Reference>, ReferenceableEntity> {

        @Override
        public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
            return this.sourceType.isAssignableFrom(sourceType) && this.destinationType.isAssignableFrom(destinationType);

        }

        @Override
        public ReferenceableEntity convert(Optional<Reference> source, Type<? extends ReferenceableEntity> destinationType, MappingContext mappingContext) {
            if (source != null && source.isPresent()) {
                return mapperFacade.map(source.get(), destinationType.getRawType());
            }
            return null;
        }

    }

    static final class OptionalImmutableConverter extends CustomConverter<Optional<?>, Object> {

        @Override
        public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
            return this.sourceType.isAssignableFrom(sourceType) && isImmutable(destinationType);
        }

        @Override
        public Object convert(Optional<?> source, Type<?> destinationType, MappingContext mappingContext) {
            if (source != null && source.isPresent()) {
                return mapperFacade.map(source.get(), destinationType.getRawType());
            }
            return null;
        }


        private static boolean isImmutable(Type<?> type) {
            return
                    TekstiPalanen.class.isAssignableFrom(type.getRawType())
                            || type.isPrimitiveWrapper()
                            || type.isEnum()
                            || type.isPrimitive()
                            || type.isString();
        }

    }

    @SuppressWarnings("unchecked")
    public static <T> T unproxy(T entity) {
        if (entity instanceof HibernateProxy) {
            Hibernate.initialize(entity);
            entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer()
                    .getImplementation();
        }
        return entity;
    }
}