package fi.vm.sade.eperusteet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "osaamistaso")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneerisenArvioinninOsaamistasonKriteeriKaikkiDto {
    private OsaamistasoDto osaamistaso;
    private List<LokalisoituTekstiDto> kriteerit = new ArrayList<>();

    private Long _osaamistaso;

    @JsonProperty("_osaamistaso")
    public Reference osaamistasoRef() {
        return Reference.of(_osaamistaso);
    }
}
