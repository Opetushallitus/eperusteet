package fi.vm.sade.eperusteet.service.mapping;

import fi.vm.sade.eperusteet.domain.Koodistokoodi;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KoodistokoodiConverter extends BidirectionalConverter<Koodistokoodi, String> {

    @Override
    public String convertTo(Koodistokoodi source, Type<String> destinationType, MappingContext mappingContext) {
        log.info("KoodistokoodiConverter convertTo kutsuttu luokalla: " + destinationType.getRawType());
        return source.getKoodi();
    }

    @Override
    public Koodistokoodi convertFrom(String source, Type<Koodistokoodi> destinationType, MappingContext mappingContext) {
        return null;
    }

}
