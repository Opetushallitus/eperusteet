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
