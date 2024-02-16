package fi.vm.sade.eperusteet.dto.julkinen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class YlopsOrganisaatioDto {
    private Map<String, String> nimi;
    private List<String> tyypit;
}
