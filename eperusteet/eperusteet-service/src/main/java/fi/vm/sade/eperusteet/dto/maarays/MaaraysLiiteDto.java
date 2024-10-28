package fi.vm.sade.eperusteet.dto.maarays;

import fi.vm.sade.eperusteet.domain.maarays.MaaraysLiiteTyyppi;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaaraysLiiteDto {
    private UUID id;
    private LokalisoituTekstiDto nimi;
    private String tiedostonimi;
    private MaaraysLiiteTyyppi tyyppi;
    private String fileB64;
}
