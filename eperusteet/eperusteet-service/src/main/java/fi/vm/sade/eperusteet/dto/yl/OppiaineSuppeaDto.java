package fi.vm.sade.eperusteet.dto.yl;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OppiaineSuppeaDto extends OppiaineBaseDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Set<OppiaineSuppeaDto> oppimaarat;
    private String koodiArvo;
    private Set<OppiaineenVuosiluokkaKokonaisuusSuppeaDto> vuosiluokkakokonaisuudet;
    private Optional<LokalisoituTekstiDto> pakollinenKurssiKuvaus;
    private Optional<LokalisoituTekstiDto> syventavaKurssiKuvaus;
    private Optional<LokalisoituTekstiDto> soveltavaKurssiKuvaus;
}
