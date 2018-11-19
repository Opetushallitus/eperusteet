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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

/**
 *
 * @author jhyoty
 */
public class MappingModule extends SimpleModule {

    public MappingModule() {
        super(MappingModule.class.getSimpleName());
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.setMixInAnnotations(Page.class, PageMixin.class);
        context.setMixInAnnotations(PageImpl.class, PageMixin.class);
    }

    @JsonIgnoreProperties(value = {"numberOfElements", "firstPage", "lastPage", "sort", "first", "last"})
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
