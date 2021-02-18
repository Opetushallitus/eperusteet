package fi.vm.sade.eperusteet.service.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Optional;
import org.springframework.http.converter.GenericHttpMessageConverter;

public interface JsonMapper {
    JsonNode readTree(byte[] bytes) throws IOException;

    <T> byte[] writeValueAsBytes(T data) throws IOException;

    default <T> T deserialize(Class<T> t, String from) throws IOException {
        return deserialize(t, new StringReader(from));
    }

    <T> T deserialize(Class<T> t, Reader from) throws IOException;

    default <T> String serialize(T obj) throws IOException {
        StringWriter writer = new StringWriter();
        serialize(obj, writer);
        return writer.getBuffer().toString();
    }

    <T> JsonNode toJson(T obj) throws IOException;

    <T> JsonNode readTree(String str) throws IOException;

    <T> void serialize(T obj, Writer to) throws IOException;

    default Optional<? extends GenericHttpMessageConverter<?>> messageConverter() {
        return Optional.empty();
    }

    default <T> Optional<T> unwrap(Class<T> clzz) {
        return Optional.empty();
    }
}
