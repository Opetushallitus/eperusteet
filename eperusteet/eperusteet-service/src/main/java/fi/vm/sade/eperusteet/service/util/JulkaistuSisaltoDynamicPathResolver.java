package fi.vm.sade.eperusteet.service.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Mirrors external API dynamic path behaviour previously implemented with
 * {@code jsonb_path_query(jsonb_lower_keys(data), ...)}: object field names are matched
 * case-insensitively; numeric path segments select an array element whose {@code id}
 * matches (case-insensitive key name).
 */
public final class JulkaistuSisaltoDynamicPathResolver {

    private JulkaistuSisaltoDynamicPathResolver() {
    }

    public static JsonNode resolve(ObjectNode root, List<String> pathSegments) {
        if (root == null) {
            return null;
        }
        JsonNode current = root;
        for (String element : pathSegments) {
            if (current == null || current.isNull() || current.isMissingNode()) {
                return null;
            }
            String trimmed = element.trim();
            if (current.isObject()) {
                current = getObjectFieldCaseInsensitive(current, trimmed.toLowerCase(Locale.ROOT));
            } else if (current.isArray()) {
                current = findArrayElementById(current, trimmed);
            } else {
                return null;
            }
        }
        return current;
    }

    private static JsonNode getObjectFieldCaseInsensitive(JsonNode node, String lowerFieldName) {
        if (!node.isObject()) {
            return null;
        }
        Iterator<String> names = node.fieldNames();
        while (names.hasNext()) {
            String key = names.next();
            if (key.toLowerCase(Locale.ROOT).equals(lowerFieldName)) {
                return node.get(key);
            }
        }
        return null;
    }

    private static JsonNode findArrayElementById(JsonNode node, String idStr) {
        if (!node.isArray()) {
            return null;
        }
        for (JsonNode item : node) {
            if (!item.isObject()) {
                continue;
            }
            JsonNode idNode = getObjectFieldCaseInsensitive(item, "id");
            if (idNode != null && !idNode.isNull() && !idNode.isMissingNode() && idMatches(idNode, idStr)) {
                return item;
            }
        }
        return null;
    }

    private static boolean idMatches(JsonNode idNode, String idStr) {
        if (idNode.isTextual()) {
            return idStr.equals(idNode.asText());
        }
        if (idNode.isIntegralNumber()) {
            try {
                return idNode.longValue() == Long.parseLong(idStr);
            } catch (NumberFormatException e) {
                return idStr.equals(idNode.asText());
            }
        }
        if (idNode.isNumber()) {
            try {
                return idNode.decimalValue().compareTo(new BigDecimal(idStr)) == 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }
}
