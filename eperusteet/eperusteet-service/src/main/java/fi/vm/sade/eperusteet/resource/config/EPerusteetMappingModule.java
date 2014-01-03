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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.LokalisoituTeksti;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

/**
 *
 * @author jhyoty
 */
public class EPerusteetMappingModule extends SimpleModule {

    public EPerusteetMappingModule() {
        super(EPerusteetMappingModule.class.getSimpleName());
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);

        context.setMixInAnnotations(Page.class, PageMixin.class);
        SimpleSerializers s = new SimpleSerializers();
        s.addSerializer(TekstiPalanen.class, new TekstiPalanenSerializer());
        SimpleDeserializers d = new SimpleDeserializers();
        d.addDeserializer(TekstiPalanen.class, new TekstiPalanenDeserializer());
        context.addSerializers(s);
        context.addDeserializers(d);
    }

    @JsonIgnoreProperties(value = {"numberOfElements", "firstPage", "lastPage", "sort"})
    public static abstract class PageMixin {

        @JsonProperty("data")
        abstract List<?> getContent();

        @JsonProperty("sivu")
        abstract int getNumber();

        @JsonProperty("sivuja")
        abstract int getTotalPages();

        @JsonProperty("kokonaismäärä")
        abstract int getTotalElements();

        @JsonProperty("sivukoko")
        abstract int getSize();

    }

    public static class TekstiPalanenSerializer extends StdSerializer<TekstiPalanen> {

        public TekstiPalanenSerializer() {
            super(TekstiPalanen.class);
        }

        @Override
        public void serialize(TekstiPalanen value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
                JsonGenerationException {
            jgen.writeStartObject();
            for (LokalisoituTeksti t : value.getTeksti()) {
                provider.defaultSerializeField(t.getKieli().toString(), t.getTeksti(), jgen);
            }
            jgen.writeEndObject();
        }

    }

    public static class TekstiPalanenDeserializer extends StdDeserializer<TekstiPalanen> {

        public TekstiPalanenDeserializer() {
            super(TekstiPalanen.class);
        }

        @Override
        public TekstiPalanen deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
                JsonProcessingException {

            LOG.info("deserialize");
            Map<Kieli, LokalisoituTeksti> tekstit = new EnumMap<>(Kieli.class);

            if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                while (jp.nextValue() != JsonToken.END_OBJECT) {
                    final Kieli k = Kieli.of(jp.getCurrentName());
                    tekstit.put(k, new LokalisoituTeksti(k, jp.getText()));
                }
                TekstiPalanen t = new TekstiPalanen(tekstit);
                return t;
            }

            throw new JsonParseException("Virheellinen " + TekstiPalanen.class.getSimpleName(), jp.getCurrentLocation());
        }

        private static final Logger LOG = LoggerFactory.getLogger(TekstiPalanenDeserializer.class);

    }

}
