package fi.vm.sade.eperusteet.service.mapping;

import fi.vm.sade.eperusteet.domain.Koulutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.KoulutusalaDto;
import fi.vm.sade.eperusteet.dto.OpintoalaDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KoodistoMapperConfig {

    @Autowired
    private KoodistoClient koodistoClient;

    @Bean
    @Koodisto
    public DtoMapper koodistoMapper() {
        DefaultMapperFactory factory = new DefaultMapperFactory.Builder()
            .build();
        factory.getConverterFactory().registerConverter("metadataConverter", KoodistoMetadataConverter.TO_MAP);
        factory.getConverterFactory().registerConverter("metadataToTekstipalanenConverter", KoodistoMetadataConverter.TO_TEKSTIPALANEN);
        factory.getConverterFactory().registerConverter("koodistoPaivaysConverter", KoodistoConverter.TO_DATE);

        factory.classMap(KoodistoKoodiDto.class, KoulutusalaDto.class)
                .field("koodiUri", "koodi")
                .fieldMap("metadata", "nimi").converter("metadataConverter").add()
                .byDefault()
                .register();

        factory.classMap(KoodistoKoodiDto.class, Peruste.class)
                //.fieldMap("voimassaAlkuPvm", "paivays").converter("koodistoPaivaysConverter").add()
                .fieldMap("metadata", "nimi").converter("metadataToTekstipalanenConverter").add()
                .byDefault()
                .register();

        factory.classMap(KoodistoKoodiDto.class, Koulutus.class)
                //.fieldMap("voimassaAlkuPvm", "paivays").converter("koodistoPaivaysConverter").add()
                .fieldMap("metadata", "nimi").converter("metadataToTekstipalanenConverter").add()
                .byDefault()
                .register();

        factory.classMap(KoodistoKoodiDto.class, OpintoalaDto.class)
                .field("koodiUri", "koodi")
                .fieldMap("metadata", "nimi").converter("metadataConverter").add()
                .byDefault()
                .register();


        return new DtoMapperImpl(factory.getMapperFacade());
    }


}
