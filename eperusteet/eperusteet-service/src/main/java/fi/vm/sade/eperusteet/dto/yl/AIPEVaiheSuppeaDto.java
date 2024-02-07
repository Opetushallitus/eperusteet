package fi.vm.sade.eperusteet.dto.yl;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AIPEVaiheSuppeaDto extends AIPEVaiheBaseDto {
    private Optional<LokalisoituTekstiDto> nimi;
}
