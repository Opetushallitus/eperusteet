package fi.vm.sade.eperusteet.dto.yl;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpetuksenKohdealueDto implements ReferenceableDto {
    public Long id;
    public Optional<LokalisoituTekstiDto> nimi;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Optional<LokalisoituTekstiDto> kuvaus;
}
