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
import com.fasterxml.jackson.databind.module.SimpleModule;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import java.util.List;
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
        addDeserializer(AbstractRakenneOsaDto.class, new AbstractRakenneOsaDeserializer());
        context.setMixInAnnotations(Page.class, PageMixin.class);
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

}
