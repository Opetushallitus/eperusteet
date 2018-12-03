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

package fi.vm.sade.eperusteet.resource.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import fi.vm.sade.eperusteet.dto.Reference;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * JSON-kenttien nimeämisstrategia.
 * <p>
 * Nimeää EntitiReference -tyyppiä olevat kentät muotoon _kentännimi ja käyttää muissa tapauksissa oletusnimeämistä.
 */
public class ReferenceNamingStrategy extends PropertyNamingStrategy {

    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return getName(config, method.getType(), defaultName);
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return getName(config, method.getParameter(0).getType(), defaultName);
    }

    private String getName(MapperConfig<?> config, Type type, String defaultName) {
        final JavaType et = config.getTypeFactory().constructType(Reference.class);
        final JavaType ot = config.getTypeFactory().constructReferenceType(Optional.class, et);
        // Todo: Use only java.util.Optional
        final JavaType googleOt = config.getTypeFactory()
                .constructReferenceType(com.google.common.base.Optional.class, et);
        final JavaType t = config.getTypeFactory().constructType(type);

        if (et.equals(t) || ot.equals(t) || googleOt.equals(t)) {
            return "_" + defaultName;
        }

        return defaultName;
    }

}
