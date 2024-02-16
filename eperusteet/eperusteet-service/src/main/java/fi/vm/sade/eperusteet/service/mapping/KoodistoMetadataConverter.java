package fi.vm.sade.eperusteet.service.mapping;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

public class KoodistoMetadataConverter {

    public static final Converter<KoodistoMetadataDto[], LokalisoituTekstiDto> TO_MAP = new CustomConverter<KoodistoMetadataDto[], LokalisoituTekstiDto>() {
        @Override
        public LokalisoituTekstiDto convert(KoodistoMetadataDto[] source, Type<? extends LokalisoituTekstiDto> destinationType, MappingContext mappingContext) {
            Map<Kieli, String> nimi = new EnumMap<>(Kieli.class);
            for (KoodistoMetadataDto metadata : source) {
                nimi.put(Kieli.of(metadata.getKieli()), metadata.getNimi());
            }

            return new LokalisoituTekstiDto(null, nimi);
        }
    };

    public static final Converter<KoodistoMetadataDto[], TekstiPalanen> TO_TEKSTIPALANEN = new CustomConverter<KoodistoMetadataDto[], TekstiPalanen>() {
        @Override
        public TekstiPalanen convert(KoodistoMetadataDto[] source, Type<? extends TekstiPalanen> destinationType, MappingContext mappingContext) {
            Map <Kieli, String> tekstit = new HashMap<>();
            for (KoodistoMetadataDto metadata : source) {
                tekstit.put(Kieli.of(metadata.getKieli()),  metadata.getNimi());
            }

            TekstiPalanen nimi = TekstiPalanen.of(tekstit);
            return nimi;
        }
    };

}
