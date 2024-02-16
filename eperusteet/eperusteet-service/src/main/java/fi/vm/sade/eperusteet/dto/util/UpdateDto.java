package fi.vm.sade.eperusteet.dto.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.Getter;

@Data
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
        this(null,null);
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
