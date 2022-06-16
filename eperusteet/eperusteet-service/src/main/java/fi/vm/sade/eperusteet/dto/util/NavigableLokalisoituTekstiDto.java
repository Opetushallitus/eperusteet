package fi.vm.sade.eperusteet.dto.util;


import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

public class NavigableLokalisoituTekstiDto extends LokalisoituTekstiDto{

    @Setter
    @Getter
    private NavigationNodeDto navigationNode;

    public NavigableLokalisoituTekstiDto(Long id, Map<Kieli, String> values, NavigationNodeDto navigationNode) {
        super(id, values);
        this.navigationNode = navigationNode;
    }

    public NavigableLokalisoituTekstiDto(Long id, UUID tunniste, Map<Kieli, String> values) {
        super(id, tunniste, values);
    }

    public NavigableLokalisoituTekstiDto(Map<String, String> values) {
        super(values);
    }

    public NavigableLokalisoituTekstiDto(Long id, Map<Kieli, String> values) {
        super(id, values);
    }

    @Override
    @JsonValue
    public Map<String, Object> asMap() {
        Map<String, Object> map = super.asMap();
        if(navigationNode != null) {
                map.put("navigationNode", navigationNode);
        }

        return map;
    }
}
