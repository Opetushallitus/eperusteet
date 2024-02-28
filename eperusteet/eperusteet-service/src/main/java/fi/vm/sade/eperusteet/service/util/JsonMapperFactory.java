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

package fi.vm.sade.eperusteet.service.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import fi.vm.sade.eperusteet.config.ReferenceNamingStrategy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

/**
 * User: tommiratamaa
 * Date: 16.11.2015
 * Time: 15.16
 */
@Component
public class JsonMapperFactory implements FactoryBean<JsonMapper> {
    @Override
    public JsonMapper getObject() throws Exception {
        return ObjectMapperJsonMapperAdapter.of(
                Jackson2ObjectMapperBuilder
                        .json()
                        .featuresToEnable(
                                SerializationFeature.WRITE_ENUMS_USING_TO_STRING,
                                DeserializationFeature.READ_ENUMS_USING_TO_STRING
                        )
                        .modules(new Jdk8Module())
                        .propertyNamingStrategy(new ReferenceNamingStrategy())
                        .build()
        );
    }

    @Override
    public Class<?> getObjectType() {
        return JsonMapper.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
