package fi.vm.sade.eperusteet.dto.arviointi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.vm.sade.eperusteet.dto.OsaamistasonKriteeriDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArvioinninKohdeDto {
    private LokalisoituTekstiDto otsikko;
    private LokalisoituTekstiDto selite;
    @JsonProperty("_arviointiAsteikko")
    private Reference arviointiAsteikko;
    @JsonProperty("arviointiAsteikko")
    private ArviointiAsteikkoDto arviointiAsteikkoDto;
    private Set<OsaamistasonKriteeriDto> osaamistasonKriteerit;
}
