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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

/**
 *
 * @author harrik
 */
class KoodistoConverter {
    public static final Converter<String, Date> TO_DATE = new CustomConverter<String, Date>() {

        @Override
        public Date convert(String source, Type<? extends Date> destinationType, MappingContext mappingContext) {
            Date paivays = null;
            SimpleDateFormat koodistoDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                paivays = koodistoDateFormat.parse(source);
            } catch (ParseException ex) {
                Logger.getLogger(KoodistoConverter.class.getName()).log(Level.SEVERE, "Koodiston päiväyksen muunnos epäonnistui", ex);
            }
            return paivays;
        }

    };
}
