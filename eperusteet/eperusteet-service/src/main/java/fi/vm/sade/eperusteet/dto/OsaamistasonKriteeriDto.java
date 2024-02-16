package fi.vm.sade.eperusteet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsaamistasonKriteeriDto {
    @JsonProperty("_osaamistaso")
    private Reference osaamistaso;

    @JsonProperty("osaamistaso")
    private OsaamistasoDto osaamistasoDto;
    private List<LokalisoituTekstiDto> kriteerit;
}
