package fi.vm.sade.eperusteet.dto.yl;

import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaajaalainenOsaaminenDto implements ReferenceableDto, AIPEHasId {
    private Long id;
    private UUID tunniste;
    private Optional<LokalisoituTekstiDto> nimi;
    private Optional<LokalisoituTekstiDto> kuvaus;
    private Date muokattu;
}
