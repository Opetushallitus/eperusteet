package fi.vm.sade.eperusteet.dto.yl;

import fi.vm.sade.eperusteet.dto.Reference;
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
public class AIPEKurssiDto extends AIPEKurssiSuppeaDto {
    private Optional<LokalisoituTekstiDto> kuvaus;
    private Set<Reference> tavoitteet;
}
