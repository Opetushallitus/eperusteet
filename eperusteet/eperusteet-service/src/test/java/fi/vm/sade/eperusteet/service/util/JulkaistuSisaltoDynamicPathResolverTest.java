package fi.vm.sade.eperusteet.service.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JulkaistuSisaltoDynamicPathResolverTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void resolvesNestedFieldCaseInsensitive() {
        ObjectNode root = objectMapper.createObjectNode();
        root.putObject("TutkinnonOsat").put("koodi", "x");

        JsonNode out = JulkaistuSisaltoDynamicPathResolver.resolve(root, List.of("tutkinnonosat", "koodi"));
        assertThat(out.isTextual()).isTrue();
        assertThat(out.asText()).isEqualTo("x");
    }

    @Test
    public void resolvesArrayElementByNumericId() {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode items = root.putArray("items");
        items.addObject().put("id", 10).put("nimi", "a");
        items.addObject().put("id", 20).put("nimi", "b");

        JsonNode out = JulkaistuSisaltoDynamicPathResolver.resolve(root, List.of("items", "20", "nimi"));
        assertThat(out.asText()).isEqualTo("b");
    }

    @Test
    public void resolvesArrayElementByStringId() {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode items = root.putArray("items");
        items.addObject().put("id", "ab").put("x", 1);

        JsonNode out = JulkaistuSisaltoDynamicPathResolver.resolve(root, List.of("items", "ab", "x"));
        assertThat(out.asInt()).isEqualTo(1);
    }

    @Test
    public void idFieldNameCaseInsensitive() {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode items = root.putArray("items");
        items.addObject().put("ID", 5).put("v", "hit");

        JsonNode out = JulkaistuSisaltoDynamicPathResolver.resolve(root, List.of("items", "5", "v"));
        assertThat(out.asText()).isEqualTo("hit");
    }

    @Test
    public void emptyPathReturnsRoot() {
        ObjectNode root = objectMapper.createObjectNode().put("a", 1);
        JsonNode out = JulkaistuSisaltoDynamicPathResolver.resolve(root, List.of());
        assertThat(out).isSameAs(root);
    }

    @Test
    public void missingFieldReturnsNull() {
        ObjectNode root = objectMapper.createObjectNode();
        assertThat(JulkaistuSisaltoDynamicPathResolver.resolve(root, List.of("none"))).isNull();
    }
}
