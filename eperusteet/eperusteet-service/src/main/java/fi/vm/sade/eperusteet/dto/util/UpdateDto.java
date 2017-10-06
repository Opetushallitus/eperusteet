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

package fi.vm.sade.eperusteet.dto.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import lombok.Setter;

/**
 * @author jhyoty
 */
@Getter
@Setter
public class UpdateDto<T> {

    @JsonUnwrapped
    private T dto;
    private MetaData metadata;

    @Getter
    public static class MetaData {

        @JsonCreator
        public MetaData(@JsonProperty("kommentti") String kommentti) {
            this.kommentti = kommentti;
        }

        private final String kommentti;
    }

    public UpdateDto() {
        this(null, null);
    }

    public UpdateDto(T dto) {
        this(dto, null);
    }

    public UpdateDto(T dto, MetaData metadata) {
        this.dto = dto;
        this.metadata = metadata;
    }

    @JsonIgnore
    public MetaData getMetadataOrEmpty() {
        return metadata == null ? EMPTY_METADATA : metadata;
    }

    private static final MetaData EMPTY_METADATA = new MetaData(null);
}
