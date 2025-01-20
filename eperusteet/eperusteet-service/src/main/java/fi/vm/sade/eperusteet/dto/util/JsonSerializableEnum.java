package fi.vm.sade.eperusteet.dto.util;

import com.fasterxml.jackson.annotation.JsonValue;

public interface JsonSerializableEnum {
    @JsonValue
    default String value() {
        return ((Enum<?>) this).name();
    }
}